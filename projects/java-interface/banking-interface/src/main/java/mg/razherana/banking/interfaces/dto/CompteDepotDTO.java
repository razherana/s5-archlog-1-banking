package mg.razherana.banking.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for deposit accounts.
 * Corresponds to the .NET CompteDepotDTO from banking-depot service.
 */
public class CompteDepotDTO {

  private Integer id;

  @JsonProperty("typeCompteDepotId")
  private Integer typeCompteDepotId;

  @JsonProperty("typeCompteDepotNom")
  private String typeCompteDepotNom;

  @JsonProperty("tauxInteret")
  private BigDecimal tauxInteret;

  @JsonProperty("userId")
  private Integer userId;

  @JsonProperty("dateOuverture")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime dateOuverture;

  @JsonProperty("dateEcheance")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime dateEcheance;

  private BigDecimal montant;

  @JsonProperty("estRetire")
  private Boolean estRetire;

  @JsonProperty("dateRetire")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime dateRetire;

  @JsonProperty("interetCalcule")
  private BigDecimal interetCalcule;

  @JsonProperty("montantTotal")
  private BigDecimal montantTotal;

  // Default constructor
  public CompteDepotDTO() {
  }

  // Getters and setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getTypeCompteDepotId() {
    return typeCompteDepotId;
  }

  public void setTypeCompteDepotId(Integer typeCompteDepotId) {
    this.typeCompteDepotId = typeCompteDepotId;
  }

  public String getTypeCompteDepotNom() {
    return typeCompteDepotNom;
  }

  public void setTypeCompteDepotNom(String typeCompteDepotNom) {
    this.typeCompteDepotNom = typeCompteDepotNom;
  }

  public BigDecimal getTauxInteret() {
    return tauxInteret;
  }

  public void setTauxInteret(BigDecimal tauxInteret) {
    this.tauxInteret = tauxInteret;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public LocalDateTime getDateOuverture() {
    return dateOuverture;
  }

  public void setDateOuverture(LocalDateTime dateOuverture) {
    this.dateOuverture = dateOuverture;
  }

  public LocalDateTime getDateEcheance() {
    return dateEcheance;
  }

  public void setDateEcheance(LocalDateTime dateEcheance) {
    this.dateEcheance = dateEcheance;
  }

  public BigDecimal getMontant() {
    return montant;
  }

  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  public Boolean getEstRetire() {
    return estRetire;
  }

  public void setEstRetire(Boolean estRetire) {
    this.estRetire = estRetire;
  }

  public LocalDateTime getDateRetire() {
    return dateRetire;
  }

  public void setDateRetire(LocalDateTime dateRetire) {
    this.dateRetire = dateRetire;
  }

  public BigDecimal getInteretCalcule() {
    return interetCalcule;
  }

  public void setInteretCalcule(BigDecimal interetCalcule) {
    this.interetCalcule = interetCalcule;
  }

  public BigDecimal getMontantTotal() {
    return montantTotal;
  }

  public void setMontantTotal(BigDecimal montantTotal) {
    this.montantTotal = montantTotal;
  }

  @Override
  public String toString() {
    return "CompteDepotDTO{" +
        "id=" + id +
        ", typeCompteDepotNom='" + typeCompteDepotNom + '\'' +
        ", montant=" + montant +
        ", dateEcheance=" + dateEcheance +
        ", estRetire=" + estRetire +
        '}';
  }
}