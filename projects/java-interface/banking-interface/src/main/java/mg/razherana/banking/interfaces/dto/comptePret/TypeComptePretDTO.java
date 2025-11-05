package mg.razherana.banking.interfaces.dto.comptePret;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class TypeComptePretDTO {
  @JsonProperty("id")
  private Integer id;

  @JsonProperty("nom")
  private String nom;

  @JsonProperty("interet")
  private BigDecimal interet;

  // Default constructor
  public TypeComptePretDTO() {
  }

  // Constructor with parameters
  public TypeComptePretDTO(Integer id, String nom, BigDecimal interet) {
    this.id = id;
    this.nom = nom;
    this.interet = interet;
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

  public BigDecimal getInteret() {
    return interet;
  }

  public void setInteret(BigDecimal interet) {
    this.interet = interet;
  }

  // Helper method to get interest rate as percentage
  public String getInteretPercentage() {
    if (interet != null) {
      return interet.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%";
    }
    return "0%";
  }

  @Override
  public String toString() {
    return "TypeComptePretDTO{" +
        "id=" + id +
        ", nom='" + nom + '\'' +
        ", interet=" + interet +
        '}';
  }
}