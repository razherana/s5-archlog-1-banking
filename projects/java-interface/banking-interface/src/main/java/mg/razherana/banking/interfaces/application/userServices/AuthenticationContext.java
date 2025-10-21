package mg.razherana.banking.interfaces.application.userServices;

import mg.razherana.banking.common.entities.UserAdmin;

/**
 * Thread-local authentication context for storing and accessing the current authenticated UserAdmin.
 * 
 * <p>
 * This utility class provides a clean way to pass authentication context across method calls
 * within the same thread without modifying method signatures. It's particularly useful for
 * accessing the current authenticated user in authorization checks and logging.
 * </p>
 * 
 * <p>
 * The context is thread-safe as each thread maintains its own copy of the UserAdmin.
 * Make sure to call {@link #clear()} when the request processing is complete to prevent
 * memory leaks.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
public class AuthenticationContext {
    
    private static final ThreadLocal<UserAdmin> currentUserAdmin = new ThreadLocal<>();
    
    /**
     * Set the current authenticated UserAdmin for this thread.
     * 
     * @param userAdmin the authenticated UserAdmin to store in context
     */
    public static void setCurrentUserAdmin(UserAdmin userAdmin) {
        currentUserAdmin.set(userAdmin);
    }
    
    /**
     * Get the current authenticated UserAdmin for this thread.
     * 
     * @return the current UserAdmin or null if no user is authenticated
     */
    public static UserAdmin getCurrentUserAdmin() {
        return currentUserAdmin.get();
    }
    
    /**
     * Get the ID of the current authenticated UserAdmin.
     * 
     * @return the current UserAdmin ID or null if no user is authenticated
     */
    public static Integer getCurrentUserAdminId() {
        UserAdmin user = getCurrentUserAdmin();
        return user != null ? user.getId() : null;
    }
    
    /**
     * Check if there is a currently authenticated user in this thread.
     * 
     * @return true if a UserAdmin is set in the context, false otherwise
     */
    public static boolean hasCurrentUser() {
        return getCurrentUserAdmin() != null;
    }
    
    /**
     * Clear the authentication context for this thread.
     * This should be called when request processing is complete to prevent memory leaks.
     */
    public static void clear() {
        currentUserAdmin.remove();
    }
}