package mg.razherana.banking.courant.application.virementService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import mg.razherana.banking.courant.application.compteCourantService.CompteCourantService;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.ConfigurationFrais;
import mg.razherana.banking.courant.entities.TransactionCourant;
import mg.razherana.banking.courant.entities.TransactionCourant.SpecialAction;
import mg.razherana.banking.courant.entities.TransactionEtat;
import mg.razherana.banking.courant.entities.TransactionEtat.TransactionEtatEnum;

@Stateless
public class VirementServiceImpl implements VirementService {
  private static final Logger LOG = Logger.getLogger(VirementService.class.getName());
  private static final String TYPE_COMPTE_COURANT = "courant";

  @PersistenceContext(unitName = "userPU")
  private EntityManager entityManager;

  @EJB
  private CompteCourantService compteCourantService;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public TransactionCourant createVirement(Integer userAdminId, Integer compteSourceId, Integer compteDestinationId,
      String montantStr, String change, LocalDateTime actionDateTime) {
    if (userAdminId == null)
      throw new IllegalArgumentException("User admin ID cannot be null");

    if (compteSourceId == null || compteDestinationId == null)
      throw new IllegalArgumentException("Source and destination account IDs cannot be null");

    if (compteSourceId.equals(compteDestinationId))
      throw new IllegalArgumentException("Source and destination comptes must be different");

    var montant = parseMontant(montantStr);
    BigDecimal frais = calculateFrais(montant);

    if (frais.compareTo(montant) > 0)
      throw new IllegalArgumentException("Les frais dépassent le montant du virement");

    var netMontant = montant.add(frais);

    if (netMontant.compareTo(BigDecimal.ZERO) <= 0)
      throw new IllegalArgumentException("Le montant net doit être positif");

    var effectiveDateTime = actionDateTime != null ? actionDateTime : LocalDateTime.now();

    if (change == null || change.isBlank()) {
      change = "MGA";
    }

    CompteCourant compteSource = compteCourantService.findById(compteSourceId);

    if (compteSource == null)
      throw new IllegalArgumentException("Compte source introuvable");

    CompteCourant compteDestination = compteCourantService.findById(compteDestinationId);

    if (compteDestination == null)
      throw new IllegalArgumentException("Compte destination introuvable");

    ensureTaxesPaid(compteSource, effectiveDateTime);
    ensureSufficientBalance(compteSource, netMontant);
    ensureDailyLimit(compteSource, netMontant, effectiveDateTime);

    CompteCourant managedSource = entityManager.getReference(CompteCourant.class, compteSourceId);
    CompteCourant managedDestination = entityManager.getReference(CompteCourant.class, compteDestinationId);

    TransactionCourant virement = new TransactionCourant();
    virement.setSender(managedSource);
    virement.setReceiver(managedDestination);
    virement.setMontant(netMontant);
    virement.setChange(change);
    virement.setSpecialAction(null);
    virement.setDate(effectiveDateTime);
    virement.setValidationDate(null);

    entityManager.persist(virement);
    entityManager.flush();

    if (frais.compareTo(BigDecimal.ZERO) > 0) {
      TransactionCourant fraisTransaction = new TransactionCourant();
      fraisTransaction.setSender(null);
      fraisTransaction.setReceiver(null);
      fraisTransaction.setMontant(frais);
      fraisTransaction.setChange(virement.getChange());
      fraisTransaction.setSpecialAction(SpecialAction.FRAIS.getDatabaseName());
      fraisTransaction.setDate(effectiveDateTime);
      fraisTransaction.setValidationDate(effectiveDateTime);
      fraisTransaction.setJsonData(null);

      entityManager.persist(fraisTransaction);
      entityManager.flush();

      virement.setJsonData(buildVirementJsonData(fraisTransaction.getId()));
      entityManager.flush();
    }

    TransactionEtat etatInitial = new TransactionEtat();
    etatInitial.setTransaction(virement);
    etatInitial.setEtat(TransactionEtatEnum.EN_ATTENTE.getCode());
    etatInitial.setUserAdminId(userAdminId);
    etatInitial.setDate(effectiveDateTime);

    entityManager.persist(etatInitial);
    entityManager.flush();

    LOG.info("Virement created with ID: " + virement.getId());
    return virement;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public TransactionCourant validateVirement(Integer userAdminId, Integer virementId, Integer etat,
      LocalDateTime actionDateTime) {
    if (userAdminId == null) {
      throw new IllegalArgumentException("User admin ID cannot be null");
    }
    if (virementId == null) {
      throw new IllegalArgumentException("Transaction ID cannot be null");
    }
    if (etat == null) {
      throw new IllegalArgumentException("Etat code cannot be null");
    }

    TransactionCourant transaction = entityManager.find(TransactionCourant.class, virementId);
    if (transaction == null) {
      throw new IllegalArgumentException("Le virement n'existe pas");
    }

    TransactionEtatEnum newEtatEnum = resolveEtatEnum(etat);
    if (newEtatEnum == null) {
      throw new IllegalArgumentException("Etat inconnu: " + etat);
    }

    var effectiveDateTime = actionDateTime != null ? actionDateTime : LocalDateTime.now();

    List<TransactionEtat> existingEtats = entityManager.createQuery(
        "SELECT e FROM TransactionEtat e WHERE e.transaction = :transaction ORDER BY e.date ASC, e.id ASC",
        TransactionEtat.class)
        .setParameter("transaction", transaction)
        .getResultList();

    TransactionEtatEnum etatAtActionDate = null;
    for (TransactionEtat existing : existingEtats) {
      if (existing.getDate().isAfter(effectiveDateTime))
        break;
      etatAtActionDate = existing.getEtatEnum();
    }

    if (etatAtActionDate == TransactionEtatEnum.VALIDEE) {
      throw new IllegalStateException("Le virement est déjà validé à cette date");
    }

    TransactionEtat nouveauEtat = new TransactionEtat();
    nouveauEtat.setTransaction(transaction);
    nouveauEtat.setEtat(newEtatEnum.getCode());
    nouveauEtat.setUserAdminId(userAdminId);
    nouveauEtat.setDate(effectiveDateTime);

    entityManager.persist(nouveauEtat);

    if (newEtatEnum == TransactionEtatEnum.VALIDEE) {
      transaction.setValidationDate(effectiveDateTime);
    } else if (transaction.getValidationDate() != null) {
      transaction.setValidationDate(null);
    }

    entityManager.flush();

    LOG.info("Transaction " + virementId + " changée en etat " + newEtatEnum.name());
    return transaction;
  }

  private BigDecimal parseMontant(String montantStr) {
    if (montantStr == null || montantStr.isBlank()) {
      throw new IllegalArgumentException("Montant invalide");
    }
    try {
      BigDecimal montant = new BigDecimal(montantStr.trim());
      if (montant.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Le montant doit être positif");
      }
      return montant.setScale(2, RoundingMode.HALF_UP);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Montant invalide", e);
    }
  }

  private void ensureTaxesPaid(CompteCourant compte, LocalDateTime actionDateTime) {
    if (!compteCourantService.isTaxPaid(compte, actionDateTime)) {
      BigDecimal amount = compteCourantService.getTaxToPay(compte, actionDateTime);
      throw new IllegalArgumentException(
          "Taxes must be paid before making a transaction, please pay the amount of " + amount + " MGA");
    }
  }

  private void ensureSufficientBalance(CompteCourant compte, BigDecimal montant) {
    BigDecimal solde = compteCourantService.calculateSolde(compte);
    if (solde.compareTo(montant) < 0) {
      throw new IllegalArgumentException("Solde insuffisant");
    }
  }

  private void ensureDailyLimit(CompteCourant compte, BigDecimal montant, LocalDateTime actionDateTime) {
    var virementsToday = compteCourantService.getVirementToday(compte, actionDateTime.toLocalDate());
    BigDecimal totalToday = virementsToday.stream()
        .map(TransactionCourant::getMontant)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .add(montant);

    if (totalToday.compareTo(compte.getLimiteVirementJournalier()) > 0)
      throw new IllegalArgumentException(
          "Limite de virement journalier dépassée de " + compte.getLimiteVirementJournalier() + " MGA");
  }

  private BigDecimal calculateFrais(BigDecimal montant) {
    TypedQuery<ConfigurationFrais> query = entityManager.createQuery(
        "SELECT cf FROM ConfigurationFrais cf WHERE cf.typeCompte = :typeCompte "
            + "AND :montant BETWEEN cf.montantMinimum AND cf.montantMaximum",
        ConfigurationFrais.class);
    query.setParameter("typeCompte", TYPE_COMPTE_COURANT);
    query.setParameter("montant", montant);

    List<ConfigurationFrais> configurations = query.getResultList();

    BigDecimal total = BigDecimal.ZERO;
    for (ConfigurationFrais configuration : configurations) {
      if (configuration.getFraisMontant() != null)
        total = total.add(configuration.getFraisMontant());

      if (configuration.getFraisPourcentage() != null) {
        BigDecimal pourcentage = configuration.getFraisPourcentage();
        BigDecimal fraisPourcentage = montant.multiply(pourcentage)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        total = total.add(fraisPourcentage);
      }
    }

    return total.setScale(2, RoundingMode.HALF_UP);
  }

  private TransactionEtatEnum resolveEtatEnum(Integer etatCode) {
    return TransactionEtat.fromCode(etatCode);
  }

  private String buildVirementJsonData(Integer fraisTransactionId) {
    return "{\"fraisTransactionId\":" + fraisTransactionId + "}";
  }

  @Override
  public List<TransactionEtat> getEtatsByTransaction(Integer transactionId) {
    if (transactionId == null) {
      throw new IllegalArgumentException("Transaction ID cannot be null");
    }

    TransactionCourant transaction = entityManager.find(TransactionCourant.class, transactionId);
    if (transaction == null) {
      throw new IllegalArgumentException("Transaction introuvable");
    }

    return entityManager.createQuery(
        "SELECT etat FROM TransactionEtat etat WHERE etat.transaction.id = :transactionId ORDER BY etat.date DESC, etat.id DESC",
        TransactionEtat.class)
        .setParameter("transactionId", transactionId)
        .getResultList();
  }
}
