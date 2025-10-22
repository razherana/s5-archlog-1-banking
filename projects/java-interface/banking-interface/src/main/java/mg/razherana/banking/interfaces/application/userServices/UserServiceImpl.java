package mg.razherana.banking.interfaces.application.userServices;

import mg.razherana.banking.common.entities.ActionRole;
import mg.razherana.banking.common.entities.User;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.userServices.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateful;
import jakarta.ejb.StatefulTimeout;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.NoResultException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
@Stateful
@StatefulTimeout(unit = TimeUnit.MINUTES, value = 30)
public class UserServiceImpl implements UserService {

  private static final Logger LOG = Logger.getLogger(UserServiceImpl.class.getName());

  private static final String BACKEND_DEPOT_URL = "http://127.0.0.4:8080/api";

  @EJB
  private CompteCourantService compteCourantService;

  @PersistenceContext(unitName = "userPU")
  private EntityManager entityManager;

  @EJB
  private ComptePretService comptePretService;

  @PostConstruct
  public void init() {
    adminInfos = getAllUserAdminsWithDepAndRoles(null);
  }

  @PreDestroy
  public void cleanup() {
    System.out.println("Stateful EJB destroyed: " + this);
  }

  @Override
  public User findUserById(UserAdmin userAdmin, Integer userId) {
    LOG.info("Finding user by ID: " + userId);

    if (!hasAuthorization(userAdmin, "READ", "users")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read users");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read users");
    }

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }
    return entityManager.find(User.class, userId);
  }

  @Override
  public UserAdmin findUserAdminByEmail(String email) {
    LOG.info("Finding user admin by email: " + email);
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }

    try {
      TypedQuery<UserAdmin> query = entityManager.createQuery(
          "SELECT u FROM UserAdmin u WHERE u.email = :email", UserAdmin.class);
      query.setParameter("email", email.trim().toLowerCase());
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<User> getAllUsers(UserAdmin userAdmin) {
    LOG.info("Retrieving all users");

    if (!hasAuthorization(userAdmin, "READ", "users")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read users");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read users");
    }

    TypedQuery<User> query = entityManager.createQuery(
        "SELECT u FROM User u ORDER BY u.createdAt DESC", User.class);
    List<User> users = query.getResultList();
    LOG.info("Found " + users.size() + " users");
    return users;
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public User createUser(UserAdmin userAdmin, String name) {
    LOG.info("Creating user with name: " + name);

    if (!hasAuthorization(userAdmin, "CREATE", "users")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create users");
      throw new IllegalStateException("Unauthorized access: User does not have permission to create users");
    }

    // Validation
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }

    // Create new user
    User user = new User();
    user.setName(name.trim());
    user.setCreatedAt(LocalDateTime.now());

    entityManager.persist(user);
    entityManager.flush();

    LOG.info("User created successfully with ID: " + user.getId());
    return user;
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public User updateUser(UserAdmin userAdmin, Integer userId, String name) {
    LOG.info("Updating user with ID: " + userId);

    if (!hasAuthorization(userAdmin, "UPDATE", "users")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to update users");
      throw new IllegalStateException("Unauthorized access: User does not have permission to update users");
    }

    User user = findUserById(userAdmin, userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    // Validation
    if (name != null && !name.trim().isEmpty()) {
      user.setName(name.trim());
    }

    entityManager.merge(user);
    entityManager.flush();

    LOG.info("User updated successfully");
    return user;
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void deleteUser(UserAdmin userAdmin, Integer userId) {
    LOG.info("Deleting user with ID: " + userId);

    if (!hasAuthorization(userAdmin, "DELETE", "users")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to delete users");
      throw new IllegalStateException("Unauthorized access: User does not have permission to delete users");
    }

    User user = findUserById(userAdmin, userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    entityManager.remove(user);
    entityManager.flush();

    LOG.info("User deleted successfully");
  }

  @Override
  public BigDecimal calculateTotalBalanceAcrossModules(UserAdmin userAdmin, Integer userId, String actionDateTime) {
    LOG.info("Calculating total balance across all modules for user: " + userId + " at " + actionDateTime);

    if (!hasAuthorization(userAdmin, "READ", "users")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read user balances");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read user balances");
    }

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    // Verify user exists
    User user = findUserById(userAdmin, userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found: " + userId);
    }

    BigDecimal totalBalance = BigDecimal.ZERO;

    // 1. Get balance from Current Accounts module (java-courant)
    try {
      BigDecimal currentAccountBalance = getCurrentAccountBalance(userAdmin, userId, actionDateTime);
      totalBalance = totalBalance.add(currentAccountBalance);
      LOG.info("Current accounts balance for user " + userId + ": " + currentAccountBalance);
    } catch (Exception e) {
      LOG.warning("Failed to get current account balance for user " + userId + ": " + e.getMessage());
      // Continue with other modules even if one fails
    }

    // 2. Get balance from Loan module (java-pret) - this represents debt (negative
    // balance)
    try {
      BigDecimal loanBalance = getLoanBalance(userAdmin, userId, actionDateTime);
      // Loan balance represents debt, so we subtract it from total
      totalBalance = totalBalance.subtract(loanBalance);
      LOG.info("Loan balance (debt) for user " + userId + ": " + loanBalance);
    } catch (Exception e) {
      LOG.warning("Failed to get loan balance for user " + userId + ": " + e.getMessage());
      // Continue with other modules even if one fails
    }

    // 3. Get balance from Deposit module (dotnet-depot)
    try {
      BigDecimal depositBalance = getDepositBalance(userAdmin, userId, actionDateTime);
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
  public BigDecimal getCurrentAccountBalance(UserAdmin userAdmin, Integer userId, String actionDateTimeStr) {
    if (!hasAuthorization(userAdmin, "READ", "compte_courants")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read current account balances");
      throw new IllegalStateException(
          "Unauthorized access: User does not have permission to read current account balances");
    }

    LocalDateTime actionDateTime = null;
    if (actionDateTimeStr != null && !actionDateTimeStr.trim().isEmpty()) {
      actionDateTime = LocalDateTime.parse(actionDateTimeStr);
    }
    return compteCourantService.getAccountBalanceByUserId(userAdmin, userId, actionDateTime);
  }

  /**
   * Get loan balance from java-pret module via REST API
   */
  @Override
  public BigDecimal getLoanBalance(UserAdmin userAdmin, Integer userId, String actionDateTimeStr) {
    if (!hasAuthorization(userAdmin, "READ", "compte_prets")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read loan balances");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read loan balances");
    }

    LocalDateTime actionDateTime = null;
    if (actionDateTimeStr != null && !actionDateTimeStr.trim().isEmpty()) {
      actionDateTime = LocalDateTime.parse(actionDateTimeStr);
    }
    return comptePretService.getLoanBalanceByUserId(userAdmin, userId, actionDateTime);
  }

  /**
   * Get deposit balance from dotnet-depot module via REST API
   */
  @Override
  public BigDecimal getDepositBalance(UserAdmin userAdmin, Integer userId, String actionDateTime) {
    if (!hasAuthorization(userAdmin, "READ", "compte_depots")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read deposit balances");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read deposit balances");
    }
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

  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public UserAdmin authenticateUserAdmin(String email, String password) {
    LOG.info("Authenticating user admin with email: " + email);

    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }
    if (password == null || password.trim().isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    try {
      TypedQuery<UserAdmin> query = entityManager.createQuery(
          "SELECT ua FROM UserAdmin ua WHERE ua.email = :email", UserAdmin.class);
      query.setParameter("email", email.trim().toLowerCase());
      UserAdmin userAdmin = query.getSingleResult();

      if (userAdmin != null && password.equals(userAdmin.getPassword())) {
        LOG.info("Authentication successful for user admin: " + email);
        return userAdmin;
      }
    } catch (NoResultException e) {
      LOG.info("User admin not found: " + email);
    }

    LOG.info("Authentication failed for user admin: " + email);
    return null;
  }

  @Override
  public List<ActionRole> getActionRoleByRole(Integer role) {
    TypedQuery<ActionRole> query = entityManager.createQuery(
        "SELECT ar FROM ActionRole ar WHERE ar.role = :role", ActionRole.class);
    query.setParameter("role", role);
    return query.getResultList();
  }

  @Override
  public UserAdmin findUserAdminById(UserAdmin userAdmin, Integer id) {
    if (!hasAuthorization(userAdmin, "READ", "user_admins")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read user admins");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read user admins");
    }

    if (id == null) {
      throw new IllegalArgumentException("UserAdmin ID cannot be null");
    }
    return entityManager.find(UserAdmin.class, id);
  }

  @Override
  public UserAdmin createUserAdmin(UserAdmin userAdmin, String email, String password, int role) {
    if (!hasAuthorization(userAdmin, "CREATE", "user_admins")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create user admins");
      throw new IllegalStateException("Unauthorized access: User does not have permission to create user admins");
    }

    // Checks
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }
    if (password == null || password.trim().isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    UserAdmin newUserAdmin = new UserAdmin();
    newUserAdmin.setEmail(email.trim().toLowerCase());
    newUserAdmin.setPassword(password);
    newUserAdmin.setRole(role);

    entityManager.persist(newUserAdmin);
    return newUserAdmin;
  }

  @Override
  public Map<Integer, String> getAllUsersForDropdown(UserAdmin userAdmin) {
    LOG.info("Getting all users for dropdown");

    if (!hasAuthorization(userAdmin, "READ", "users")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read users for dropdown");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read users for dropdown");
    }

    List<User> users = getAllUsers(userAdmin);
    Map<Integer, String> userMap = new HashMap<>();

    for (User user : users) {
      userMap.put(user.getId(), user.getId() + " : " + user.getName());
    }

    return userMap;
  }

  private HashMap<Integer, List<ActionRole>> getRoleWithActionRole() {
    HashMap<Integer, List<ActionRole>> result = new HashMap<>();

    TypedQuery<Integer> query = entityManager.createQuery(
        "SELECT DISTINCT ua.role FROM UserAdmin ua", Integer.class);
    List<Integer> roles = query.getResultList();

    for (Integer role : roles) {
      List<ActionRole> actionRoles = getActionRoleByRole(role);
      result.put(role, actionRoles);
    }

    return result;
  }

  @Override
  public HashMap<UserAdmin, List<ActionRole>> getAllUserAdminsWithDepAndRoles(UserAdmin userAdmin) {
    if (!hasAuthorization(userAdmin, "READ", "user_admins")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read user admins with roles");
      throw new IllegalStateException(
          "Unauthorized access: User does not have permission to read user admins with roles");
    }
    HashMap<UserAdmin, List<ActionRole>> result = new HashMap<>();

    var roleMap = getRoleWithActionRole();

    TypedQuery<UserAdmin> query = entityManager.createQuery(
        "SELECT ua FROM UserAdmin ua", UserAdmin.class);
    List<UserAdmin> userAdmins = query.getResultList();

    for (UserAdmin admin : userAdmins) {
      List<ActionRole> actionRoles = roleMap.getOrDefault(admin.getRole(), List.of());
      result.put(admin, actionRoles);
    }

    return result;
  }

  private HashMap<UserAdmin, List<ActionRole>> adminInfos = null;

  @Override
  public boolean hasAuthorization(UserAdmin userAdmin, String operationType, String tableName) {
    if (userAdmin == null)
      return true;

    if (adminInfos == null) {
      LOG.info("Loading admin infos for authorization checks");
      adminInfos = getAllUserAdminsWithDepAndRoles(null);
    }

    var actionRoles = adminInfos.get(userAdmin);

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