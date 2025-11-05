package mg.razherana.banking.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Data Transfer Object for deposit account types.
 * Corresponds to the .NET TypeCompteDepotDTO from banking-depot service.
 */
public class TypeCompteDepotDTO {

  private Integer id;

  private String nom;

  @JsonProperty("tauxInteret")
  private BigDecimal tauxInteret;

  // Default constructor
  public TypeCompteDepotDTO() {
  }

  // Getters and setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public BigDecimal getTauxInteret() {
    return tauxInteret;
  }

  public void setTauxInteret(BigDecimal tauxInteret) {
    this.tauxInteret = tauxInteret;
  }

  @Override
  public String toString() {
    return "TypeCompteDepotDTO{" +
        "id=" + id +
        ", nom='" + nom + '\'' +
        ", tauxInteret=" + tauxInteret +
        '}';
  }
}