package mg.razherana.banking.interfaces.application.userServices;

import mg.razherana.banking.interfaces.entities.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.logging.Logger;

/**
 * Remote EJB implementation for User management operations.
 * 
 * <p>
 * This service provides remote access to user operations for other
 * Java applications (like java-courant, java-depot, java-pret).
 * It delegates to the local implementation to avoid code duplication.
 * </p>
 * 
 * <p>
 * Remote calls require serialization and are used for inter-application
 * communication. This implementation acts as a facade for the local service.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Stateless
public class UserServiceRemoteImpl implements UserServiceRemote {

  private static final Logger LOG = Logger.getLogger(UserServiceRemoteImpl.class.getName());

  @EJB
  private UserServiceLocal userServiceLocal;

  @Override
  public User findUserById(Integer userId) {
    LOG.info("Remote call: Finding user by ID: " + userId);
    return userServiceLocal.findUserById(userId);
  }

  @Override
  public User findUserByEmail(String email) {
    LOG.info("Remote call: Finding user by email: " + email);
    return userServiceLocal.findUserByEmail(email);
  }

  @Override
  public List<User> getAllUsers() {
    LOG.info("Remote call: Retrieving all users");
    return userServiceLocal.getAllUsers();
  }

  @Override
  public User createUser(String name, String email, String password) {
    LOG.info("Remote call: Creating user with email: " + email);
    return userServiceLocal.createUser(name, email, password);
  }

  @Override
  public User updateUser(Integer userId, String name, String email, String password) {
    LOG.info("Remote call: Updating user with ID: " + userId);
    return userServiceLocal.updateUser(userId, name, email, password);
  }

  @Override
  public void deleteUser(Integer userId) {
    LOG.info("Remote call: Deleting user with ID: " + userId);
    userServiceLocal.deleteUser(userId);
  }

  @Override
  public User authenticateUser(String email, String password) {
    LOG.info("Remote call: Authenticating user with email: " + email);
    return userServiceLocal.authenticateUser(email, password);
  }
}