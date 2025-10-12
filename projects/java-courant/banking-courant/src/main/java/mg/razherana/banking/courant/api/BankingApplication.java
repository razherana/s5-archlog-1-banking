package mg.razherana.banking.courant.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application class for the Banking Current Account System.
 * 
 * <p>This class configures the REST API endpoints for the banking system.
 * All API endpoints are available under the "/api" path.</p>
 * 
 * <p><strong>Available Resources:</strong></p>
 * <ul>
 *   <li>/api/users - User management operations</li>
 *   <li>/api/comptes - Current account management</li>
 *   <li>/api/transactions - Transaction operations</li>
 * </ul>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.api.CompteCourantResource
 * @see mg.razherana.banking.courant.api.TransactionResource
 */
@ApplicationPath("/api")
public class BankingApplication extends Application {
}
