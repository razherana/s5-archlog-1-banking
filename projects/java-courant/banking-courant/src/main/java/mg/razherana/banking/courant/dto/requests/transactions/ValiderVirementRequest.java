package mg.razherana.banking.courant.dto.requests.transactions;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ValiderVirementRequest {
  @NotNull(message = "L'id du virement est obligatoire")
  @Pattern(regexp = "^[0-9]+$", message = "L'id du virement doit être un nombre entier valide")
  private int id;

  @NotNull(message = "La date de validation est obligatoire")
  private LocalDateTime dateValidation;  
}
