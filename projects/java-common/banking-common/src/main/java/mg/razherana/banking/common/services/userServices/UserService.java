package mg.razherana.banking.common.services.userServices;

import mg.razherana.banking.common.entities.User;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.authorizationServices.AuthorizationService;
import mg.razherana.banking.common.entities.ActionRole;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EJB interface for User management services.
 * 
 * <p>
 * This interface is used for local EJB calls within the same application
 * (banking-interface). It provides access to user services for REST API
 * endpoints and web controllers.
 * </p>
 * 
 * <p>
 * Local interfaces are more efficient than remote interfaces as they
 * don't require serialization and can pass objects by reference.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
public interface UserService extends AuthorizationService {

  /**
   * Find a user by ID.
   * 
   * @param userAdmin the authenticated user admin
   * @param userId the user ID
   * @return User entity or null if not found
   */
  User findUserById(UserAdmin userAdmin, Integer userId);

  /**
   * Get all users in the system.
   * 
   * @param userAdmin the authenticated user admin
   * @return List of all users
   */
  List<User> getAllUsers(UserAdmin userAdmin);

  /**
   * Create a new user.
   * 
   * @param userAdmin the authenticated user admin
   * @param name the user's full name
   * @return created User entity
   * @throws IllegalArgumentException if validation fails or email already exists
   */
  User createUser(UserAdmin userAdmin, String name);

  /**
   * Update an existing user.
   * 
   * @param userAdmin the authenticated user admin
   * @param userId the ID of the user to update
   * @param name   the new name (optional, can be null to keep existing)
   * @return updated User entity
   * @throws IllegalArgumentException if user not found or validation fails
   */
  User updateUser(UserAdmin userAdmin, Integer userId, String name);

  /**
   * Delete a user by ID.
   * 
   * @param userAdmin the authenticated user admin
   * @param userId the ID of the user to delete
   * @throws IllegalArgumentException if user not found
   */
  void deleteUser(UserAdmin userAdmin, Integer userId);

  /**
   * Authenticate a user admin with email and password.
   * 
   * @param email    the user admin's email
   * @param password the user admin's password
   * @return UserAdmin entity if authentication successful, null otherwise
   */
  UserAdmin authenticateUserAdmin(String email, String password);

  /**
   * Find a user by email address.
   * 
   * @param email the user's email
   * @return User entity or null if not found
   */
  UserAdmin findUserAdminByEmail(String email);

  /**
   * Find a user admin by ID.
   * 
   * @param userAdmin the authenticated user admin
   * @param userAdminId the user admin ID
   * @return UserAdmin entity or null if not found
   */
  UserAdmin findUserAdminById(UserAdmin userAdmin, Integer userAdminId);

  /**
   * Get action roles by role number.
   * 
   * @param role the role number
   * @return List of ActionRole entities for the given role
   */
  List<ActionRole> getActionRoleByRole(Integer role);

  /**
   * Calculate the total balance across all banking modules for a user.
   * This method makes REST API calls to:
   * - Current accounts module (java-courant)
   * - Loan module (java-pret)
   * - Deposit module (dotnet-depot)
   * 
   * @param userAdmin the authenticated user admin
   * @param userId         the user ID
   * @param actionDateTime optional date time for calculation (ISO format:
   *                       yyyy-MM-ddTHH:mm:ss)
   * @return total balance across all modules
   * @throws IllegalArgumentException if user not found or API calls fail
   */
  BigDecimal calculateTotalBalanceAcrossModules(UserAdmin userAdmin, Integer userId, String actionDateTime);

  /**
   * Get current account balance for a user from the java-courant module.
   * 
   * @param userAdmin the authenticated user admin
   * @param userId         the user ID
   * @param actionDateTime optional date time for calculation (ISO format:
   *                       yyyy-MM-ddTHH:mm:ss)
   * @return current account balance
   * @throws RuntimeException if API call fails
   */
  BigDecimal getCurrentAccountBalance(UserAdmin userAdmin, Integer userId, String actionDateTime);

  /**
   * Get loan balance for a user from the java-pret module.
   * 
   * @param userAdmin the authenticated user admin
   * @param userId         the user ID
   * @param actionDateTime optional date time for calculation (ISO format:
   *                       yyyy-MM-ddTHH:mm:ss)
   * @return loan balance (remaining debt)
   * @throws RuntimeException if API call fails
   */
  BigDecimal getLoanBalance(UserAdmin userAdmin, Integer userId, String actionDateTime);

  /**
   * Get deposit balance for a user from the dotnet-depot module.
   * 
   * @param userAdmin the authenticated user admin
   * @param userId         the user ID
   * @param actionDateTime optional date time for calculation (ISO format:
   *                       yyyy-MM-ddTHH:mm:ss)
   * @return deposit balance
   * @throws RuntimeException if API call fails
   */
  BigDecimal getDepositBalance(UserAdmin userAdmin, Integer userId, String actionDateTime);

  /**
   * Create a new user admin.
   * 
   * @param userAdmin the authenticated user admin
   * @param email    the email of the user admin
   * @param password the password of the user admin
   * @param role     the role of the user admin
   * @return the created UserAdmin entity
   */
  UserAdmin createUserAdmin(UserAdmin userAdmin, String email, String password, int role);

  /**
   * Get all users formatted for dropdown display as "id : name".
   * 
   * @param userAdmin the authenticated user admin
   * @return Map where key is user ID and value is formatted display string "id :
   *         name"
   */
  Map<Integer, String> getAllUsersForDropdown(UserAdmin userAdmin);

  /**
   * Get all user admins in the system + infos.
   * 
   * @param userAdmin the authenticated user admin
   * @return HashMap of UserAdmin to their list of ActionRoles.
   */
  HashMap<UserAdmin, List<ActionRole>> getAllUserAdminsWithDepAndRoles(UserAdmin userAdmin);
}