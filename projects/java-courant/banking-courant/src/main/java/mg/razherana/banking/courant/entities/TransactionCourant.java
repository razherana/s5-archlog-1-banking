package mg.razherana.banking.courant.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_courants")
public class TransactionCourant {
  public static enum SpecialAction {
    DEPOSIT("deposit"), WITHDRAWAL("withdrawal"), TAXE("taxe");

    private String databaseName;

    SpecialAction(String databaseName) {
      this.databaseName = databaseName;
    }

    public String getDatabaseName() {
      return databaseName;
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  // This field can be used to denote special actions from SpecialAction enum
  @Column(name = "special_action", nullable = true)
  private String specialAction;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = true)
  private CompteCourant sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = true)
  private CompteCourant receiver;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal montant;

  @Column(name = "date", nullable = false)
  private LocalDateTime date = LocalDateTime.now();

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public CompteCourant getSender() {
    return sender;
  }

  public void setSender(CompteCourant sender) {
    this.sender = sender;
  }

  public CompteCourant getReceiver() {
    return receiver;
  }

  public void setReceiver(CompteCourant receiver) {
    this.receiver = receiver;
  }

  public BigDecimal getMontant() {
    return montant;
  }

  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  @Override
  public String toString() {
    return "TransactionCourant{" +
        "id=" + id +
        ", montant=" + montant +
        ", date=" + date +
        '}';
  }

  public String getSpecialAction() {
    return specialAction;
  }

  public void setSpecialAction(String specialAction) {
    this.specialAction = specialAction;
  }

  public SpecialAction getSpecialActionEnum() {
    if (specialAction == null) {
      return null;
    }

    for (SpecialAction action : SpecialAction.values())
      if (action.getDatabaseName().equals(specialAction))
        return action;

    return null;
  }
}
