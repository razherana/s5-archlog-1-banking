package mg.razherana.banking.courant.application.compteCourantService;

import jakarta.ejb.Remote;

/**
 * Remote interface for managing current account (Compte Courant) operations.
 * 
 * <p>
 * This remote interface allows other applications to access current account services
 * via EJB remote calls. It extends the local CompteCourantService interface to
 * provide the same functionality remotely.
 * </p>
 * 
 * <p>
 * This interface is annotated with @Remote to enable remote EJB access from
 * other applications in the distributed banking system.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see CompteCourantService
 * @see CompteCourantRemoteServiceImpl
 */
@Remote
public interface CompteCourantRemoteService extends CompteCourantService {
  // This interface inherits all methods from CompteCourantService
  // and makes them available for remote EJB calls
}
