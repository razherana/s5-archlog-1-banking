package mg.razherana.banking.courant.application.transactionService;

import jakarta.ejb.Remote;

/**
 * Remote interface for managing transaction operations.
 * 
 * <p>
 * This remote interface allows other applications to access transaction services
 * via EJB remote calls. It extends the local TransactionService interface to
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
 * @see TransactionService
 * @see TransactionRemoteServiceImpl
 */
@Remote
public interface TransactionRemoteService extends TransactionService {
    // This interface inherits all methods from TransactionService
    // and makes them available for remote EJB calls
}