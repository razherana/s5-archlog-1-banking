package mg.razherana.banking.pret;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration for the Pret Banking API.
 * 
 * <p>
 * This class configures the REST API endpoints to be available under the /api
 * path.
 * All REST resources in the mg.razherana.banking.pret.api package will be
 * automatically
 * discovered and registered.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@ApplicationPath("/api")
public class BankingApplication extends Application {
  // JAX-RS will automatically discover and register all @Path annotated classes
  // No additional configuration needed
}