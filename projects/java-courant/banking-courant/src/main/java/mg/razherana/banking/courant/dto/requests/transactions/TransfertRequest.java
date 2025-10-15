package mg.razherana.banking.courant.dto.requests.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransfertRequest {
  private Integer compteSourceId;
  private Integer compteDestinationId;
  private BigDecimal montant;
  private String description;
  private LocalDateTime actionDateTime;

  public Integer getCompteSourceId() {
    return compteSourceId;
  }

  public void setCompteSourceId(Integer compteSourceId) {
    this.compteSourceId = compteSourceId;
  }

  public Integer getCompteDestinationId() {
    return compteDestinationId;
  }

  public void setCompteDestinationId(Integer compteDestinationId) {
    this.compteDestinationId = compteDestinationId;
  }

  public BigDecimal getMontant() {
    return montant;
  }

  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getActionDateTime() {
    return actionDateTime;
  }

  public void setActionDateTime(LocalDateTime actionDateTime) {
    this.actionDateTime = actionDateTime;
  }
}
