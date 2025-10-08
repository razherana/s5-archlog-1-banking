package mg.razherana.banking.courant.dto.requests;

import java.math.BigDecimal;

public class UpdateTaxeRequest {
    private BigDecimal taxe;

    public UpdateTaxeRequest() {}

    public UpdateTaxeRequest(BigDecimal taxe) {
        this.taxe = taxe;
    }

    public BigDecimal getTaxe() {
        return taxe;
    }

    public void setTaxe(BigDecimal taxe) {
        this.taxe = taxe;
    }
}