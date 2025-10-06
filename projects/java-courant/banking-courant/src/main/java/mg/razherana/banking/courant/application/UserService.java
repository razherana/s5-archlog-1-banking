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

@Stateless
public class UserService {
  private static final Logger LOG = Logger.getLogger(UserService.class.getName());

  @PersistenceContext(unitName = "userPU")
  private EntityManager entityManager;

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

  public List<User> getUsers() {
    LOG.info("Retrieving all users");
    TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u", User.class);
    List<User> users = query.getResultList();
    LOG.info("Found " + users.size() + " users");
    return users;
  }

  public User findById(Integer id) {
    LOG.info("Finding user by ID: " + id);
    if (id == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }
    return entityManager.find(User.class, id);
  }

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
