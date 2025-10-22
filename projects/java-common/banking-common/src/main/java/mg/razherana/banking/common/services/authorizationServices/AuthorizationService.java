package mg.razherana.banking.common.services.authorizationServices;

import mg.razherana.banking.common.entities.UserAdmin;

public interface AuthorizationService {
  /**
   * Check if the user has authorization for a specific operation.
   * 
   * <p>
   * When <code>userAdmin</code> is <code>null</code>, the method should return true.
   * This allows to bypass authorization checks in contexts where we need to.
   * </p>
   * 
   * @param operationType
   * @param tableName
   * @return <code>true</code> if authorized, <code>false</code> otherwise
   */
  boolean hasAuthorization(UserAdmin userAdmin, String operationType, String tableName);
}
