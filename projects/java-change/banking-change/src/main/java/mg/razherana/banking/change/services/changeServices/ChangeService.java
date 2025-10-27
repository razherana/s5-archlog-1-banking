package mg.razherana.banking.change.services.changeServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import mg.razherana.banking.change.entities.Change;

public interface ChangeService {
  public BigDecimal getChange(String currency, LocalDateTime dateTime);
  public List<Change> getAllChanges();
}
