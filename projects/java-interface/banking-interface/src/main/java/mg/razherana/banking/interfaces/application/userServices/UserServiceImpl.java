package mg.razherana.banking.interfaces.application.userServices;

import mg.razherana.banking.interfaces.entities.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.NoResultException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.comptePretServices.ComptePretService;

/**
 * Local EJB implementation for User management operations.
 * 
 * <p>
 * This service provides local access to user operations within the
 * banking-interface application. It's used by REST API endpoints and
 * web controllers that run in the same JVM.
 * </p>
 * 
 * <p>
 * All operations are transactional and include proper validation and
 * error handling. Local calls are more efficient as they don't require
 * serialization.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Stateless
public class UserServiceImpl implements UserService {

  private static final Logger LOG = Logger.getLogger(UserServiceImpl.class.getName());

  private static final String BACKEND_DEPOT_URL = "http://127.0.0.4:8080/api";

  @EJB
  private CompteCourantService compteCourantService;

  @PersistenceContext(unitName = "userPU")
  private EntityManager entityManager;

  @EJB
  private ComptePretService comptePretService;

  @Override
  public User findUserById(Integer userId) {
    LOG.info("Finding user by ID: " + userId);
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }
    return entityManager.find(User.class, userId);
  }

  @Override
  public User findUserByEmail(String email) {
    LOG.info("Finding user by email: " + email);
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }

    try {
      TypedQuery<User> query = entityManager.createQuery(
          "SELECT u FROM User u WHERE u.email = :email", User.class);
      query.setParameter("email", email.trim().toLowerCase());
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<User> getAllUsers() {
    LOG.info("Retrieving all users");
    TypedQuery<User> query = entityManager.createQuery(
        "SELECT u FROM User u ORDER BY u.createdAt DESC", User.class);
    List<User> users = query.getResultList();
    LOG.info("Found " + users.size() + " users");
    return users;
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public User createUser(String name, String email, String password) {
    LOG.info("Creating user with email: " + email);

    // Validation
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }
    if (password == null || password.trim().isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    // Check if email already exists
    User existingUser = findUserByEmail(email);
    if (existingUser != null) {
      throw new IllegalArgumentException("Email already exists: " + email);
    }

    // Create new user
    User user = new User();
    user.setName(name.trim());
    user.setEmail(email.trim().toLowerCase());
    user.setPassword(password);
    user.setCreatedAt(LocalDateTime.now());

    entityManager.persist(user);
    entityManager.flush();

    LOG.info("User created successfully with ID: " + user.getId());
    return user;
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public User updateUser(Integer userId, String name, String email, String password) {
    LOG.info("Updating user with ID: " + userId);

    User user = findUserById(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    // Validation
    if (name != null && !name.trim().isEmpty()) {
      user.setName(name.trim());
    }

    if (email != null && !email.trim().isEmpty()) {
      // Check if new email already exists (for different user)
      User existingUser = findUserByEmail(email);
      if (existingUser != null && !existingUser.getId().equals(userId)) {
        throw new IllegalArgumentException("Email already exists: " + email);
      }
      user.setEmail(email.trim().toLowerCase());
    }

    if (password != null && !password.trim().isEmpty()) {
      user.setPassword(password);
    }

    entityManager.merge(user);
    entityManager.flush();

    LOG.info("User updated successfully");
    return user;
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void deleteUser(Integer userId) {
    LOG.info("Deleting user with ID: " + userId);

    User user = findUserById(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    entityManager.remove(user);
    entityManager.flush();

    LOG.info("User deleted successfully");
  }

  @Override
  public User authenticateUser(String email, String password) {
    LOG.info("Authenticating user with email: " + email);

    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }
    if (password == null || password.trim().isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    User user = findUserByEmail(email);
    if (user != null && password.equals(user.getPassword())) {
      LOG.info("Authentication successful for user: " + email);
      return user;
    }

    LOG.info("Authentication failed for user: " + email);
    return null;
  }

  @Override
  public BigDecimal calculateTotalBalanceAcrossModules(Integer userId, String actionDateTime) {
    LOG.info("Calculating total balance across all modules for user: " + userId + " at " + actionDateTime);

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    // Verify user exists
    User user = findUserById(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found: " + userId);
    }

    BigDecimal totalBalance = BigDecimal.ZERO;

    // 1. Get balance from Current Accounts module (java-courant)
    try {
      BigDecimal currentAccountBalance = getCurrentAccountBalance(userId, actionDateTime);
      totalBalance = totalBalance.add(currentAccountBalance);
      LOG.info("Current accounts balance for user " + userId + ": " + currentAccountBalance);
    } catch (Exception e) {
      LOG.warning("Failed to get current account balance for user " + userId + ": " + e.getMessage());
      // Continue with other modules even if one fails
    }

    // 2. Get balance from Loan module (java-pret) - this represents debt (negative
    // balance)
    try {
      BigDecimal loanBalance = getLoanBalance(userId, actionDateTime);
      // Loan balance represents debt, so we subtract it from total
      totalBalance = totalBalance.subtract(loanBalance);
      LOG.info("Loan balance (debt) for user " + userId + ": " + loanBalance);
    } catch (Exception e) {
      LOG.warning("Failed to get loan balance for user " + userId + ": " + e.getMessage());
      // Continue with other modules even if one fails
    }

    // 3. Get balance from Deposit module (dotnet-depot)
    try {
      BigDecimal depositBalance = getDepositBalance(userId, actionDateTime);
      totalBalance = totalBalance.add(depositBalance);
      LOG.info("Deposit balance for user " + userId + ": " + depositBalance);
    } catch (Exception e) {
      LOG.warning("Failed to get deposit balance for user " + userId + ": " + e.getMessage());
      // Continue with other modules even if one fails
    }

    LOG.info("Total balance across all modules for user " + userId + ": " + totalBalance);
    return totalBalance;
  }

  /**
   * Get current account balance from java-courant module via REST API
   */
  @Override
  public BigDecimal getCurrentAccountBalance(Integer userId, String actionDateTimeStr) {
    LocalDateTime actionDateTime = null;
    if (actionDateTimeStr != null && !actionDateTimeStr.trim().isEmpty()) {
      actionDateTime = LocalDateTime.parse(actionDateTimeStr);
    }
    return compteCourantService.getAccountBalanceByUserId(userId, actionDateTime);
  }

  /**
   * Get loan balance from java-pret module via REST API
   */
  @Override
  public BigDecimal getLoanBalance(Integer userId, String actionDateTimeStr) {
    LocalDateTime actionDateTime = null;
    if (actionDateTimeStr != null && !actionDateTimeStr.trim().isEmpty()) {
      actionDateTime = LocalDateTime.parse(actionDateTimeStr);
    }
    return comptePretService.getLoanBalanceByUserId(userId, actionDateTime);
  }

  /**
   * Get deposit balance from dotnet-depot module via REST API
   */
  @Override
  public BigDecimal getDepositBalance(Integer userId, String actionDateTime) {
    java.net.http.HttpClient httpClient = java.net.http.HttpClient.newHttpClient();
    try {
      String url = BACKEND_DEPOT_URL + "/ComptesDepots/solde/user/" + userId;
      if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
        url += "?actionDateTime=" + java.net.URLEncoder.encode(actionDateTime, "UTF-8");
      }

      java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
          .uri(java.net.URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      java.net.http.HttpResponse<String> response = httpClient.send(request,
          java.net.http.HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        String jsonResponse = response.body();
        // Parse JSON response {"userId": 1, "totalSolde": 2500.75}
        jakarta.json.JsonReader jsonReader = jakarta.json.Json.createReader(new java.io.StringReader(jsonResponse));
        jakarta.json.JsonObject json = jsonReader.readObject();
        jsonReader.close();

        return new BigDecimal(json.getJsonNumber("totalSolde").toString());
      } else {
        throw new RuntimeException("Deposit service returned status: " + response.statusCode());
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to call deposit service: " + e.getMessage(), e);
    }
  }
}