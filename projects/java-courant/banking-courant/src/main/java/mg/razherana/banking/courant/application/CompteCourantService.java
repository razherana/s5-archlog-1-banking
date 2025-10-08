package mg.razherana.banking.courant.application;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.User;
import mg.razherana.banking.courant.entities.TransactionCourant.SpecialAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class CompteCourantService {
  private static final Logger LOG = Logger.getLogger(CompteCourantService.class.getName());

  @PersistenceContext(unitName = "userPU")
  private EntityManager entityManager;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public CompteCourant create(User user, BigDecimal taxe) {
    LOG.info("Creating compte courant for user: " + user);
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    CompteCourant compte = new CompteCourant();
    compte.setUser(user);
    compte.setTaxe(taxe); // Default taxe, can be updated later
    compte.setCreatedAt(LocalDateTime.now());

    entityManager.persist(compte);
    entityManager.flush();
    LOG.info("Compte courant created successfully with ID: " + compte.getId());
    return compte;
  }

  public List<CompteCourant> getComptes() {
    LOG.info("Retrieving all comptes courants");
    TypedQuery<CompteCourant> query = entityManager.createQuery(
        "SELECT c FROM CompteCourant c", CompteCourant.class);
    List<CompteCourant> comptes = query.getResultList();
    LOG.info("Found " + comptes.size() + " comptes");
    return comptes;
  }

  public CompteCourant findById(Integer id) {
    LOG.info("Finding compte courant by ID: " + id);
    if (id == null) {
      throw new IllegalArgumentException("Compte ID cannot be null");
    }
    return entityManager.find(CompteCourant.class, id);
  }

  public List<CompteCourant> getComptesByUser(User user) {
    LOG.info("Finding comptes for user: " + user.getId());

    TypedQuery<CompteCourant> query = entityManager.createQuery(
        "SELECT c FROM CompteCourant c WHERE c.user = :user", CompteCourant.class);
    query.setParameter("user", user);

    return query.getResultList();
  }

  /**
   * Calculate the balance (solde) of a compte courant by summing transactions
   * Balance = (sum of received amounts) - (sum of sent amounts)
   */
  public BigDecimal calculateSolde(CompteCourant compte) {
    LOG.info("Calculating solde for compte ID: " + compte.getId());

    // Sum of incoming transactions (where this compte is receiver)
    TypedQuery<BigDecimal> incomingQuery = entityManager.createQuery(
        "SELECT COALESCE(SUM(t.montant), 0) FROM TransactionCourant t WHERE t.receiver = :compte",
        BigDecimal.class);
    incomingQuery.setParameter("compte", compte);
    BigDecimal incoming = incomingQuery.getSingleResult();

    // Sum of outgoing transactions (where this compte is sender)
    TypedQuery<BigDecimal> outgoingQuery = entityManager.createQuery(
        "SELECT COALESCE(SUM(t.montant), 0) FROM TransactionCourant t WHERE t.sender = :compte",
        BigDecimal.class);
    outgoingQuery.setParameter("compte", compte);
    BigDecimal outgoing = outgoingQuery.getSingleResult();

    BigDecimal solde = incoming.subtract(outgoing);
    LOG.info("Calculated solde: " + solde + " (incoming: " + incoming + ", outgoing: " + outgoing + ")");
    return solde;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void updateTaxe(CompteCourant compte, BigDecimal nouvelleTaxe) {
    LOG.info("Updating taxe for compte " + compte.getId() + " to " + nouvelleTaxe);
    if (compte == null || compte.getId() == null) {
      throw new IllegalArgumentException("Compte and Compte ID cannot be null");
    }
    if (nouvelleTaxe == null) {
      throw new IllegalArgumentException("Nouvelle taxe cannot be null");
    }

    compte.setTaxe(nouvelleTaxe);
    entityManager.merge(compte);
    entityManager.flush();
    LOG.info("Taxe updated successfully");
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void delete(Integer id) {
    LOG.info("Deleting compte courant with ID: " + id);
    if (id == null) {
      throw new IllegalArgumentException("Compte ID cannot be null");
    }

    CompteCourant compte = entityManager.find(CompteCourant.class, id);
    if (compte != null) {
      entityManager.remove(compte);
      entityManager.flush();
      LOG.info("Compte courant deleted successfully");
    }
  }

  // Taxes application logic

  public BigDecimal getTaxPaidTotal(CompteCourant compte) {
    if (compte == null) {
      throw new IllegalArgumentException("Compte cannot be null");
    }

    TypedQuery<BigDecimal> query = entityManager.createQuery(
        "SELECT COALESCE(SUM(t.montant), 0) FROM TransactionCourant t WHERE t.sender = :compte AND t.specialAction = :action",
        BigDecimal.class);

    query.setParameter("compte", compte);
    query.setParameter("action", SpecialAction.TAXE.getDatabaseName());

    return query.getSingleResult();
  }

  public BigDecimal getTaxPaidDate(CompteCourant compte, LocalDateTime actionDateTime) {
    if (compte == null) {
      throw new IllegalArgumentException("Compte cannot be null");
    }

    if (actionDateTime == null) {
      throw new IllegalArgumentException("Action date cannot be null");
    }

    TypedQuery<BigDecimal> query = entityManager.createQuery(
        "SELECT COALESCE(SUM(t.montant), 0) FROM TransactionCourant t WHERE t.sender = :compte AND t.specialAction = :action AND t.date <= :actionDateTime",
        BigDecimal.class);

    query.setParameter("compte", compte);
    query.setParameter("action", SpecialAction.TAXE.getDatabaseName());
    query.setParameter("actionDateTime", actionDateTime);

    return query.getSingleResult();
  }

  public boolean isTaxPaid(CompteCourant compte, LocalDateTime actionDateTime) {
    return getTaxToPay(compte, actionDateTime).compareTo(BigDecimal.ZERO) == 0;
  }

  // Taxes to pay for the month of the actionDateTime
  // If already paid, return 0
  // Sum up with old unpaid taxes if any in previous months
  // This is always superior or equal to 0
  public BigDecimal getTaxToPay(CompteCourant compte, LocalDateTime actionDateTime) {
    if (compte == null) {
      throw new IllegalArgumentException("Compte cannot be null");
    }

    BigDecimal singleTaxe = compte.getTaxe();

    // Now get the number of months since account creation to datetime
    LocalDateTime creationDateTime = compte.getCreatedAt();
    if (creationDateTime == null || actionDateTime == null) {
      throw new IllegalArgumentException("Creation date and action date cannot be null");
    }

    int monthsBetween = (actionDateTime.getYear() - creationDateTime.getYear()) * 12
        + actionDateTime.getMonthValue() - creationDateTime.getMonthValue();

    // Include the current month by adding 1
    int totalMonths = monthsBetween + 1;

    BigDecimal totalTaxToPay = singleTaxe.multiply(BigDecimal.valueOf(totalMonths));

    BigDecimal taxPaid = getTaxPaidDate(compte, actionDateTime);

    return totalTaxToPay.subtract(taxPaid).max(BigDecimal.ZERO);
  }
}
