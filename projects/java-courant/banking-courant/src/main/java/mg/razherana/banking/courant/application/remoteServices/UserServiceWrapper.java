package mg.razherana.banking.courant.application.remoteServices;

import jakarta.ejb.Stateless;
import mg.razherana.banking.common.services.userServices.UserRemoteService;

@Stateless
public class UserServiceWrapper {
  private UserRemoteService userRemoteService;
  private EJBLookupService lookupService;

  public UserServiceWrapper() {
    try {
      this.lookupService = new EJBLookupService("127.0.0.2");
      this.userRemoteService = lookupService.lookupStatefulBean(
          "global/UserRemoteServiceImpl!mg.razherana.banking.common.services.userServices.UserRemoteService",
          UserRemoteService.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize UserRemoteService", e);
    }
  }

  public UserRemoteService getUserRemoteService() {
    return userRemoteService;
  }
}
