package mg.razherana.banking.courant.dto.requests.transactions;

import java.math.BigDecimal;

public class TransfertRequest {
    private Integer compteSourceId;
    private Integer compteDestinationId;
    private BigDecimal montant;
    private String description;

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
}
