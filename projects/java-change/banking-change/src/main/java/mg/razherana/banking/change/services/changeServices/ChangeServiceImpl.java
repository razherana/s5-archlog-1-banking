package mg.razherana.banking.change.services.changeServices;

import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import mg.razherana.banking.change.entities.Change;

@Stateless
public class ChangeServiceImpl implements ChangeService {
  private static LocalDateTime p(String date) {
    return LocalDateTime.parse(date);
  }

  private Map<String, List<Change>> changes;

  private static final String DATA = """
      {
        "USD": [
          {
            "value": 4500,
            "dateStart": "2025-01-01T00:00:00",
            "dateEnd": "2025-12-31T23:59:59"
          }
        ],
        "MGA": [
          {
            "value": 1,
            "dateStart": "1950-01-01T00:00:00",
            "dateEnd": "2100-06-30T23:59:59"
          }
        ]
      }
              """;

  @PostConstruct
  public void initialize() {
    changes = new HashMap<>();

    try (Reader reader = new StringReader(DATA); JsonReader jsonReader = Json.createReader(reader)) {

      JsonObject jsonObject = jsonReader.readObject();

      for (String currency : jsonObject.keySet()) {
        JsonArray currencyArray = jsonObject.getJsonArray(currency);
        List<Change> currencyChanges = new ArrayList<>();

        for (int i = 0; i < currencyArray.size(); i++) {
          JsonObject changeObject = currencyArray.getJsonObject(i);

          double value = changeObject.getJsonNumber("value").doubleValue();
          LocalDateTime dateStart = LocalDateTime.parse(changeObject.getString("dateStart"));
          LocalDateTime dateEnd = LocalDateTime.parse(changeObject.getString("dateEnd"));

          currencyChanges.add(new Change(currency, value, dateStart, dateEnd));
        }

        changes.put(currency, currencyChanges);
      }

    } catch (Exception e) {
      // Fallback to hardcoded values if JSON file cannot be read
      e.printStackTrace();
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

  @Override
  public List<Change> getAllChanges() {
    return changes.values().stream().flatMap(List::stream).toList();
  }
}