package mg.razherana.banking.interfaces.application.userServices;

import mg.razherana.banking.interfaces.entities.User;
import jakarta.ejb.Remote;
import java.util.List;

/**
 * Remote EJB interface for User management services.
 * 
 * <p>This interface can be used by other Java services (like java-courant, 
 * java-depot, java-pret) to access user data through EJB remote calls.</p>
 * 
 * <p>This provides a clean way for Java-to-Java communication between
 * different banking services without going through HTTP REST calls.
 * Remote interfaces require serialization and are used for inter-application
 * communication.</p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Remote
public interface UserServiceRemote {
    
    /**
     * Find a user by ID.
     * 
     * @param userId the user ID
     * @return User entity or null if not found
     */
    User findUserById(Integer userId);
    
    /**
     * Find a user by email address.
     * 
     * @param email the user's email
     * @return User entity or null if not found
     */
    User findUserByEmail(String email);
    
    /**
     * Get all users in the system.
     * 
     * @return List of all users
     */
    List<User> getAllUsers();
    
    /**
     * Create a new user.
     * 
     * @param name the user's full name
     * @param email the user's email (must be unique)
     * @param password the user's password
     * @return created User entity
     * @throws IllegalArgumentException if validation fails or email already exists
     */
    User createUser(String name, String email, String password);
    
    /**
     * Update an existing user.
     * 
     * @param userId the ID of the user to update
     * @param name the new name (optional, can be null to keep existing)
     * @param email the new email (optional, can be null to keep existing)
     * @param password the new password (optional, can be null to keep existing)
     * @return updated User entity
     * @throws IllegalArgumentException if user not found or validation fails
     */
    User updateUser(Integer userId, String name, String email, String password);
    
    /**
     * Delete a user by ID.
     * 
     * @param userId the ID of the user to delete
     * @throws IllegalArgumentException if user not found
     */
    void deleteUser(Integer userId);
    
    /**
     * Authenticate a user with email and password.
     * 
     * @param email the user's email
     * @param password the user's password
     * @return User entity if authentication successful, null otherwise
     */
    User authenticateUser(String email, String password);
}