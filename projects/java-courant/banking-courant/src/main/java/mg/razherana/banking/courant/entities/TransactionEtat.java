package mg.razherana.banking.courant.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "transaction_etats")
@Data
public class TransactionEtat implements Serializable {
  public static enum TransactionEtatEnum {
    EN_ATTENTE(0),
    EN_COURS_DE_TRAITEMENT(1),

    // Can add more states as needed

    VALIDEE(10);

    private final int code;

    private TransactionEtatEnum(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id", nullable = false)
  private TransactionCourant transaction;

  @Column(name = "etat", nullable = false)
  private Integer etat;

  @Column(name = "user_admin_id", nullable = false)
  private Integer userAdminId;

  @Column(name = "date", nullable = false)
  private LocalDateTime date;

  public String getEtatLabel() {
    for (TransactionEtatEnum e : TransactionEtatEnum.values())
      if (e.getCode() == this.etat)
        return e.name();
    return "UNKNOWN";
  }

  public TransactionEtatEnum getEtatEnum() {
    for (TransactionEtatEnum e : TransactionEtatEnum.values())
      if (e.getCode() == this.etat)
        return e;
    return null;
  }

  public static TransactionEtatEnum fromCode(int code) {
    for (TransactionEtatEnum e : TransactionEtatEnum.values()) {
      if (e.getCode() == code) {
        return e;
      }
    }
    return null;
  }
}
