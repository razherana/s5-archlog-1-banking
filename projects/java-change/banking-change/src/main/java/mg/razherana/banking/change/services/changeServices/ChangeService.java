package mg.razherana.banking.change.services.changeServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ChangeService {
  public BigDecimal getChange(String currency, LocalDateTime dateTime);
}
