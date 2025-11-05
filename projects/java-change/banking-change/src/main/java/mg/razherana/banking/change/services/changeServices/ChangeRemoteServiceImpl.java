package mg.razherana.banking.change.services.changeServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import mg.razherana.banking.change.entities.Change;

@Stateless
public class ChangeRemoteServiceImpl implements ChangeRemoteService {
  @EJB
  private ChangeService changeService;

  @Override
  public BigDecimal getChange(String currency, LocalDateTime dateTime) {
    return changeService.getChange(currency, dateTime);
  }

  @Override
  public List<Change> getAllChanges() {
    return changeService.getAllChanges();
  }
}
