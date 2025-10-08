package mg.razherana.banking.courant.application;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import mg.razherana.banking.courant.entities.User;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for managing user operations in the banking system.
 * 
 * <p>This service provides business logic for user management including creation,
 * retrieval, update, and deletion operations. All operations are transactional
 * and include proper validation and logging.</p>
 * 
 * <p>This is a stateless EJB that can be injected into other components
 * using the {@code @EJB} annotation.</p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.User
 * @see mg.razherana.banking.courant.api.UserResource
 * @see mg.razherana.banking.courant.dto.requests.users.CreateUserRequest
 * @see mg.razherana.banking.courant.dto.requests.users.UpdateUserRequest
 */
@Stateless
public class UserService {
  /** Logger for this service class */
  private static final Logger LOG = Logger.getLogger(UserService.class.getName());

  /** Entity manager for database operations using the userPU persistence unit */
  @PersistenceContext(unitName = "userPU")
  private EntityManager entityManager;

  /**
   * Creates a new user in the system.
   * 
   * <p>Validates all required fields and persists the user to the database.
   * The operation is transactional and will be rolled back if any error occurs.</p>
   * 
   * @param user the user to create (must not be null)
   * @throws IllegalArgumentException if user is null or has invalid fields
   * @see User
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void create(User user) {
    LOG.info("Creating user: " + user);
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }
    if (user.getName() == null || user.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("User name cannot be null or empty");
    }
    if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
      throw new IllegalArgumentException("User email cannot be null or empty");
    }
    if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
      throw new IllegalArgumentException("User password cannot be null or empty");
    }

    entityManager.persist(user);
    entityManager.flush();
    LOG.info("User created successfully with ID: " + user.getId());
  }

  /**
   * Retrieves all users from the system.
   * 
   * @return a list of all users, empty list if no users exist
   */
  public List<User> getUsers() {
    LOG.info("Retrieving all users");
    TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u", User.class);
    List<User> users = query.getResultList();
    LOG.info("Found " + users.size() + " users");
    return users;
  }

  /**
   * Finds a user by their unique identifier.
   * 
   * @param id the user ID to search for
   * @return the user if found, null otherwise
   * @throws IllegalArgumentException if id is null
   */
  public User findById(Integer id) {
    LOG.info("Finding user by ID: " + id);
    if (id == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }
    return entityManager.find(User.class, id);
  }

  /**
   * Finds a user by their email address.
   * 
   * <p>Email addresses are expected to be unique in the system.</p>
   * 
   * @param email the email address to search for
   * @return the user if found, null otherwise
   * @throws IllegalArgumentException if email is null or empty
   */
  public User findByEmail(String email) {
    LOG.info("Finding user by email: " + email);
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }

    TypedQuery<User> query = entityManager.createQuery(
        "SELECT u FROM User u WHERE u.email = :email", User.class);
    query.setParameter("email", email);

    List<User> users = query.getResultList();
    return users.isEmpty() ? null : users.get(0);
  }

  /**
   * Updates an existing user in the system.
   * 
   * <p>The user must have a valid ID and exist in the database.
   * This operation is transactional and will be rolled back if any error occurs.</p>
   * 
   * @param user the user to update (must not be null and must have an ID)
   * @throws IllegalArgumentException if user is null or has no ID
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void update(User user) {
    LOG.info("Updating user: " + user);
    if (user == null || user.getId() == null) {
      throw new IllegalArgumentException("User and User ID cannot be null");
    }
    entityManager.merge(user);
    entityManager.flush();
    LOG.info("User updated successfully");
  }

  /**
   * Deletes a user from the system by their ID.
   * 
   * <p>This operation is transactional and will be rolled back if any error occurs.
   * If the user doesn't exist, no error is thrown.</p>
   * 
   * @param id the ID of the user to delete
   * @throws IllegalArgumentException if id is null
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void delete(Integer id) {
    LOG.info("Deleting user with ID: " + id);
    if (id == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    User user = entityManager.find(User.class, id);
    if (user != null) {
      entityManager.remove(user);
      entityManager.flush();
      LOG.info("User deleted successfully");
    }
  }
}
