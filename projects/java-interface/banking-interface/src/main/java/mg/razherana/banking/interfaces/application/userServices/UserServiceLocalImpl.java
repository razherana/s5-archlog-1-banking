package mg.razherana.banking.interfaces.application.userServices;

import mg.razherana.banking.interfaces.entities.User;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

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
public class UserServiceLocalImpl implements UserServiceLocal {

  private static final Logger LOG = Logger.getLogger(UserServiceLocalImpl.class.getName());

  @PersistenceContext(unitName = "userPU")
  private EntityManager entityManager;

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
    user.setPassword(password); // TODO: Hash password in production
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
      user.setPassword(password); // TODO: Hash password in production
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
}