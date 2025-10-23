package mg.razherana.banking.change.services.changeServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class ChangeRemoteServiceImpl implements ChangeRemoteService {
  @EJB
  private ChangeService changeService;

  @Override
  public BigDecimal getChange(String currency, LocalDateTime dateTime) {
    return changeService.getChange(currency, dateTime);
  }
}
