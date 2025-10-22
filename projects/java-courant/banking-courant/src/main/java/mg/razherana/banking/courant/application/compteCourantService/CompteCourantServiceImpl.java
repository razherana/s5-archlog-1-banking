package mg.razherana.banking.courant.application.compteCourantService;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateful;
import jakarta.ejb.StatefulTimeout;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import mg.razherana.banking.common.entities.ActionRole;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.courant.application.remoteServices.UserServiceWrapper;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.User;
import mg.razherana.banking.courant.entities.TransactionCourant.SpecialAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Service class for managing current account (Compte Courant) operations.
 * 
 * <p>
 * This service provides business logic for current account management including
 * creation, balance calculation, tax management, and account operations. The
 * service
 * implements the core banking business rule where account balances are
 * calculated
 * dynamically by summing all related transactions.
 * </p>
 * 
 * <p>
 * <strong>Key Features:</strong>
 * </p>
 * <ul>
 * <li>Dynamic balance calculation (no stored balance field)</li>
 * <li>Monthly tax calculation and validation</li>
 * <li>Transaction-based account operations</li>
 * <li>Tax payment enforcement before withdrawals/transfers</li>
 * </ul>
 * 
 * <p>
 * This is a stateless EJB that can be injected into other components
 * using the {@code @EJB} annotation.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.CompteCourant
 * @see mg.razherana.banking.courant.entities.TransactionCourant
 * @see mg.razherana.banking.courant.application.transactionService.TransactionService
 * @see mg.razherana.banking.courant.api.CompteCourantResource
 */
@Stateful
@StatefulTimeout(unit = TimeUnit.MINUTES, value = 30)
public class CompteCourantServiceImpl implements CompteCourantService {
  private static final Logger LOG = Logger.getLogger(CompteCourantService.class.getName());

  @PersistenceContext(unitName = "userPU")
  private EntityManager entityManager;

  @EJB
  private UserServiceWrapper userServiceWrapper;

  /**
   * Find a user by ID using REST API call to java-interface.
   * 
   * @param userId the user ID
   * @return User object with the specified ID
   * @throws IllegalArgumentException if userId is null or user not found
   */
  @Override
  public User findUser(Integer userId) {
    var ogUser = userServiceWrapper.getUserRemoteService().findUserById(null, userId);

    if (ogUser == null) {
      throw new IllegalArgumentException("User with ID " + userId + " not found");
    }

    var user = new User();

    user.setId(ogUser.getId());
    user.setName(ogUser.getName());

    return user;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public CompteCourant create(User user, BigDecimal taxe, LocalDateTime actionDateTime) {
    LOG.info("Creating compte courant for user: " + user);
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    CompteCourant compte = new CompteCourant();
    compte.setUser(user);
    compte.setUserId(user.getId());
    compte.setTaxe(taxe);
    compte.setCreatedAt(
        actionDateTime != null ? actionDateTime : LocalDateTime.now()); // Use provided datetime or current time

    entityManager.persist(compte);
    entityManager.flush();
    LOG.info("Compte courant created successfully with ID: " + compte.getId());
    return compte;
  }

  @Override
  public List<CompteCourant> getComptes() {
    LOG.info("Retrieving all comptes courants");
    TypedQuery<CompteCourant> query = entityManager.createQuery(
        "SELECT c FROM CompteCourant c", CompteCourant.class);
    List<CompteCourant> comptes = query.getResultList();
    LOG.info("Found " + comptes.size() + " comptes");
    return comptes;
  }

  @Override
  public CompteCourant findById(Integer id) {
    LOG.info("Finding compte courant by ID: " + id);
    if (id == null) {
      throw new IllegalArgumentException("Compte ID cannot be null");
    }

    CompteCourant compte = entityManager.find(CompteCourant.class, id);

    if (compte != null) {
      // Fetch complete user information from the user service
      try {
        User completeUser = findUser(compte.getUserId());
        compte.setUser(completeUser);
        LOG.info("Successfully enriched compte with complete user information");
      } catch (Exception e) {
        LOG.warning("Could not fetch complete user information for compte " + id + ": " + e.getMessage());
        // Continue with the existing user information from the entity
      }
    }

    return compte;
  }

  @Override
  public List<CompteCourant> getComptesByUser(User user) {
    LOG.info("Finding comptes for user: " + user.getId());

    TypedQuery<CompteCourant> query = entityManager.createQuery(
        "SELECT c FROM CompteCourant c WHERE c.userId = :userId", CompteCourant.class);
    query.setParameter("userId", user.getId());

    return query.getResultList();
  }

  @Override
  public List<CompteCourant> getComptesByUserId(Integer userId) {
    LOG.info("Finding comptes for userId: " + userId);
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    // Use findUser method and delegate to existing method
    User user = findUser(userId);
    return getComptesByUser(user);
  }

  @Override
  public BigDecimal calculateSolde(CompteCourant compte) {
    return calculateSolde(compte, null);
  }

  /**
   * Calculate the balance (solde) of a compte courant by summing transactions
   * Balance = (sum of received amounts) - (sum of sent amounts)
   */
  @Override
  public BigDecimal calculateSolde(CompteCourant compte, LocalDateTime actionDateTime) {
    LOG.info("Calculating solde for compte ID: " + compte.getId());

    if (actionDateTime == null) {
      System.out.println("ETo eeeee");
      actionDateTime = LocalDateTime.now();
    }

    System.out.println("calculateSolde = " + actionDateTime);

    // Sum of incoming transactions (where this compte is receiver)
    TypedQuery<BigDecimal> incomingQuery = entityManager.createQuery(
        "SELECT COALESCE(SUM(t.montant), 0) FROM TransactionCourant t WHERE t.receiver = :compte AND t.date <= :actionDateTime",
        BigDecimal.class);
    incomingQuery.setParameter("compte", compte);
    incomingQuery.setParameter("actionDateTime", actionDateTime);
    BigDecimal incoming = incomingQuery.getSingleResult();

    // Sum of outgoing transactions (where this compte is sender)
    TypedQuery<BigDecimal> outgoingQuery = entityManager.createQuery(
        "SELECT COALESCE(SUM(t.montant), 0) FROM TransactionCourant t WHERE t.sender = :compte AND t.date <= :actionDateTime",
        BigDecimal.class);
    outgoingQuery.setParameter("compte", compte);
    outgoingQuery.setParameter("actionDateTime", actionDateTime);
    BigDecimal outgoing = outgoingQuery.getSingleResult();

    BigDecimal solde = incoming.subtract(outgoing);
    LOG.info("Calculated solde: " + solde + " (incoming: " + incoming + ", outgoing: " + outgoing + ")");
    return solde;
  }

  /**
   * Calculate the total balance for all accounts of a user.
   * 
   * @param userId         the user ID
   * @param actionDateTime optional date time for calculation (defaults to now)
   * @return total balance across all user's current accounts
   */
  @Override
  public BigDecimal calculateTotalSoldeByUserId(Integer userId, LocalDateTime actionDateTime) {
    LOG.info("Calculating total solde for userId: " + userId + " at " + actionDateTime);

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    // Verify user exists (will throw exception if not found)
    findUser(userId);

    // Get all user's accounts
    List<CompteCourant> comptes = getComptesByUserId(userId);

    // Calculate total balance
    BigDecimal totalSolde = BigDecimal.ZERO;
    for (CompteCourant compte : comptes) {
      BigDecimal compteSolde = calculateSolde(compte, actionDateTime);
      System.out.println("Compte : " + compte + " - " + compteSolde);
      totalSolde = totalSolde.add(compteSolde);
      LOG.info("Account " + compte.getId() + " balance: " + compteSolde);
    }

    LOG.info("Total solde for user " + userId + " at " + actionDateTime + ": " + totalSolde);
    return totalSolde;
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
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
  @Override
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

  @Override
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

  @Override
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

  @Override
  public boolean isTaxPaid(CompteCourant compte, LocalDateTime actionDateTime) {
    return getTaxToPay(compte, actionDateTime).compareTo(BigDecimal.ZERO) == 0;
  }

  // Taxes to pay for the month of the actionDateTime
  // If already paid, return 0
  // Sum up with old unpaid taxes if any in previous months
  // This is always superior or equal to 0
  @Override
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

  private HashMap<UserAdmin, List<ActionRole>> adminInfos = null;

  @PostConstruct
  protected void init() {
    LOG.info("CompteCourantServiceImpl initialized");
  }

  @Override
  public boolean hasAuthorization(UserAdmin userAdmin, String operationType, String tableName) {
    if (adminInfos == null) {
      LOG.info("Loading admin infos for authorization checks");
      adminInfos = userServiceWrapper.getUserRemoteService().getAllUserAdminsWithDepAndRoles(null);
    }

    var actionRoles = adminInfos.get(userAdmin);

    System.out.println(actionRoles);

    if (actionRoles == null) {
      LOG.warning("No action roles found for user admin: " + userAdmin.getId());
      return false;
    }

    for (ActionRole actionRole : actionRoles) {
      if (actionRole.getAction().equals(operationType)
          && actionRole.getTableName().equals(tableName))
        return true;
    }

    return false;
  }

}
