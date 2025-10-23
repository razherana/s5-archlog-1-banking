package mg.razherana.banking.change.services.changeServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import mg.razherana.banking.change.entities.Change;

@Stateless
public class ChangeServiceImpl implements ChangeService {
  private static LocalDateTime p(String date) {
    return LocalDateTime.parse(date);
  }

  private Map<String, List<Change>> changes;

  @PostConstruct
  public void initialize() {
    changes = Map.ofEntries(
        Map.entry("USD", List.of(
            new Change("USD",
                4500,
                p("2025-01-01T00:00:00"),
                p("2025-12-31T23:59:59")))),
        Map.entry("MGA", List.of(
            new Change("MGA",
                1,
                p("1950-01-01T00:00:00"),
                p("2100-06-30T23:59:59")))));
  }

  @Override
  public BigDecimal getChange(String currency, LocalDateTime dateTime) {
    List<Change> currencyChanges = changes.get(currency);

    if (currencyChanges != null) {
      for (Change change : currencyChanges) {
        if (!dateTime.isBefore(change.getDateStart()) && !dateTime.isAfter(change.getDateEnd())) {
          return new BigDecimal(change.getValue());
        }
      }
    }

    return BigDecimal.ONE;
  }
}