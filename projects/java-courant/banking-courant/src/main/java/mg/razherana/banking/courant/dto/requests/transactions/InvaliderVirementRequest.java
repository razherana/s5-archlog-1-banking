package mg.razherana.banking.courant.dto.requests.transactions;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InvaliderVirementRequest {
  @NotNull(message = "L'id du virement est obligatoire")
  @Positive(message = "L'id du virement est invalide")
  private int id;
}