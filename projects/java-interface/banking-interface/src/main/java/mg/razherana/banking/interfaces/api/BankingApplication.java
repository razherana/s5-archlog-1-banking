package mg.razherana.banking.interfaces.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration for Banking Interface API.
 * 
 * <p>This class configures the REST API endpoints for the banking interface system.
 * All API endpoints will be available under the '/api' path.</p>
 * 
 * <p><strong>Available Endpoints:</strong></p>
 * <ul>
 *   <li>/api/users - User management</li>
 * </ul>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.interfaces.api.UserResource
 */
@ApplicationPath("/api")
public class BankingApplication extends Application {
}