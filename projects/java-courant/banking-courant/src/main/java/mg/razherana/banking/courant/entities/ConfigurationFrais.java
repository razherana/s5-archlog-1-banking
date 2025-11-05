package mg.razherana.banking.courant.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "configuration_frais")
@Data
public class ConfigurationFrais implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "type_compte", nullable = false, length = 50)
  private String typeCompte;

  @Column(name = "frais_montant", precision = 10, scale = 2)
  private BigDecimal fraisMontant;

  @Column(name = "frais_pourcentage", precision = 5, scale = 2)
  private BigDecimal fraisPourcentage;

  @Column(name = "montant_minimum", nullable = false, precision = 15, scale = 2)
  private BigDecimal montantMinimum;

  @Column(name = "montant_maximum", nullable = false, precision = 15, scale = 2)
  private BigDecimal montantMaximum;
}