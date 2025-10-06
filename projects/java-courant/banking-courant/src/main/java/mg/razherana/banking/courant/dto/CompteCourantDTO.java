package mg.razherana.banking.courant.dto;

import mg.razherana.banking.courant.entities.CompteCourant;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CompteCourantDTO {
    private Integer id;
    private BigDecimal taxe;
    private LocalDateTime createdAt;
    private Integer userId;
    private String userName;
    private String userEmail;
    private BigDecimal solde; // Calculated field

    public CompteCourantDTO() {}

    public CompteCourantDTO(CompteCourant compte, BigDecimal solde) {
        this.id = compte.getId();
        this.taxe = compte.getTaxe();
        this.createdAt = compte.getCreatedAt();
        this.solde = solde;
        if (compte.getUser() != null) {
            this.userId = compte.getUser().getId();
            this.userName = compte.getUser().getName();
            this.userEmail = compte.getUser().getEmail();
        }
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getTaxe() {
        return taxe;
    }

    public void setTaxe(BigDecimal taxe) {
        this.taxe = taxe;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
