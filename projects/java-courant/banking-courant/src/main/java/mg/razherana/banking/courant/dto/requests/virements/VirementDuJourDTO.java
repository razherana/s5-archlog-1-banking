package mg.razherana.banking.courant.dto.requests.virements;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VirementDuJourDTO {
  @NotNull(message = "L'id du compte est obligatoire")
  @Min(value = 1, message = "L'id du compte est invalide")
  private int id;

  private LocalDate date;
}
