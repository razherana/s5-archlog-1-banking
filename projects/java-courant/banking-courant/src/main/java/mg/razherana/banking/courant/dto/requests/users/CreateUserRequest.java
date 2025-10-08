package mg.razherana.banking.courant.dto.requests.users;

/**
 * Request DTO for creating a new user in the banking system.
 * 
 * <p>Contains all required information to create a new user account.
 * All fields are mandatory and will be validated by the service layer.</p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.User
 * @see mg.razherana.banking.courant.api.UserResource#createUser(CreateUserRequest)
 * @see mg.razherana.banking.courant.application.UserService#create(mg.razherana.banking.courant.entities.User)
 */
public class CreateUserRequest {
    /** Full name of the user to create */
    private String name;
    
    /** Email address of the user (must be unique) */
    private String email;
    
    /** Password for the user account */
    private String password;

    /**
     * Gets the full name of the user.
     * 
     * @return the user's full name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
