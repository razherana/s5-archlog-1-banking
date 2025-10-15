package mg.razherana.banking.pret.application;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import mg.razherana.banking.pret.entities.ComptePret;
import mg.razherana.banking.pret.entities.User;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for managing loan account (Compte Pret) operations.
 * 
 * <p>
 * This service provides business logic for loan account management including
 * creation, retrieval, and user integration with the java-interface service.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Stateless
public class ComptePretService {
  private static final Logger LOG = Logger.getLogger(ComptePretService.class.getName());

  // Hardcoded URL for java-interface REST API
  private static final String USER_SERVICE_BASE_URL = "http://127.0.0.2:8080/api";

  @PersistenceContext(unitName = "pretPU")
  private EntityManager entityManager;

  /**
   * Find a user by ID using REST API call to java-interface.
   * 
   * @param userId the user ID
   * @return User object with the specified ID
   * @throws IllegalArgumentException if userId is null or user not found
   */
  public User findUser(Integer userId) {
    LOG.info("Finding user by ID: " + userId);
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    Client client = ClientBuilder.newClient();
    try {
      WebTarget target = client.target(USER_SERVICE_BASE_URL + "/users/" + userId);
      Response response = target.request(MediaType.APPLICATION_JSON).get();

      if (response.getStatus() == 200) {
        // java-interface returns UserDTO, so we need to parse it and map to our User
        // entity
        String jsonResponse = response.readEntity(String.class);
        LOG.info("Received JSON response: " + jsonResponse);

        // Parse the UserDTO JSON response
        JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
        JsonObject userDto = jsonReader.readObject();
        jsonReader.close();

        // Map UserDTO fields to User entity
        User user = new User();
        user.setId(userDto.getInt("id"));
        user.setName(userDto.getString("name"));
        user.setEmail(userDto.getString("email"));
        user.setPassword(""); // Password not returned by UserDTO for security

        LOG.info("Successfully retrieved and mapped user from REST API: " + user.getId());
        return user;
      } else {
        LOG.warning("User with ID " + userId + " not found. Response status: " + response.getStatus());
        throw new IllegalArgumentException("User with ID " + userId + " not found");
      }
    } catch (Exception e) {
      LOG.severe("Error calling REST UserService: " + e.getMessage());
      throw new IllegalArgumentException("Error calling REST UserService: " + e.getMessage());
    } finally {
      client.close();
    }
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public ComptePret create(User user) {
    LOG.info("Creating compte pret for user: " + user);
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    ComptePret compte = new ComptePret();
    compte.setUserId(user.getId());

    entityManager.persist(compte);
    entityManager.flush();
    LOG.info("Compte pret created successfully with ID: " + compte.getId());
    return compte;
  }

  public List<ComptePret> getComptes() {
    LOG.info("Retrieving all comptes prets");
    TypedQuery<ComptePret> query = entityManager.createQuery(
        "SELECT c FROM ComptePret c", ComptePret.class);
    List<ComptePret> comptes = query.getResultList();
    LOG.info("Found " + comptes.size() + " comptes");
    return comptes;
  }

  public ComptePret findById(Integer id) {
    LOG.info("Finding compte pret by ID: " + id);
    if (id == null) {
      throw new IllegalArgumentException("Compte ID cannot be null");
    }
    return entityManager.find(ComptePret.class, id);
  }

  public List<ComptePret> getComptesByUserId(Integer userId) {
    LOG.info("Finding comptes for userId: " + userId);
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    // Verify user exists first
    findUser(userId);

    TypedQuery<ComptePret> query = entityManager.createQuery(
        "SELECT c FROM ComptePret c WHERE c.userId = :userId", ComptePret.class);
    query.setParameter("userId", userId);

    return query.getResultList();
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void delete(Integer id) {
    LOG.info("Deleting compte pret with ID: " + id);
    if (id == null) {
      throw new IllegalArgumentException("Compte ID cannot be null");
    }

    ComptePret compte = entityManager.find(ComptePret.class, id);
    if (compte != null) {
      entityManager.remove(compte);
      entityManager.flush();
      LOG.info("Compte pret deleted successfully");
    }
  }
}