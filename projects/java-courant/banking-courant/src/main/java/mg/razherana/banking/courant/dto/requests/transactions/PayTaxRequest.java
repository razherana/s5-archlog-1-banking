package mg.razherana.banking.courant.dto.requests.transactions;

import java.time.LocalDateTime;

public class PayTaxRequest {
    private Integer compteId;
    private String description;
    private LocalDateTime actionDateTime;

    public Integer getCompteId() {
        return compteId;
    }

    public void setCompteId(Integer compteId) {
        this.compteId = compteId;
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