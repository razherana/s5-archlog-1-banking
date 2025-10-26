package mg.razherana.banking.interfaces.application.changeServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.Stateless;
import mg.razherana.banking.change.services.changeServices.ChangeRemoteService;
import mg.razherana.banking.interfaces.application.remoteServices.EJBLookupService;
import mg.razherana.banking.interfaces.tests.JNDITreeLister;

@Stateless
public class ChangeServiceImpl implements ChangeService {
  private static final Logger LOG = Logger.getLogger(ChangeServiceImpl.class.getName());

  private EJBLookupService ejbLookupService;
  private ChangeRemoteService changeRemoteService;

  public ChangeServiceImpl() {
    try {
      JNDITreeLister.list("localhost:8080");
      this.ejbLookupService = new EJBLookupService("localhost:8080");

      this.changeRemoteService = ejbLookupService.lookupStatefulBean(
          "global/ChangeRemoteServiceImpl!mg.razherana.banking.change.services.changeServices.ChangeRemoteService",
          ChangeRemoteService.class);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed to initialize remote services", e);
      throw new RuntimeException("Failed to initialize remote services", e);
    }
  }

  @Override
  public BigDecimal getChange(String currency, LocalDateTime dateTime) {
    var result = changeRemoteService.getChange(currency, dateTime);

    if (result.compareTo(BigDecimal.ZERO) <= 0)
      throw new IllegalStateException("Change not found for currency: " + currency + " at " + dateTime);

    return result;
  }
}
