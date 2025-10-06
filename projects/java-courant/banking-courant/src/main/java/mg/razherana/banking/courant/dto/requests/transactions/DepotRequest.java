package mg.razherana.banking.courant.dto.requests.transactions;

import java.math.BigDecimal;

public class DepotRequest {
    private Integer compteId;
    private BigDecimal montant;
    private String description;

    public Integer getCompteId() { 
        return compteId; 
    }
    
    public void setCompteId(Integer compteId) { 
        this.compteId = compteId; 
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
}
