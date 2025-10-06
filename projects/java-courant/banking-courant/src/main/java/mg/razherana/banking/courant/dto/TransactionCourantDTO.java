package mg.razherana.banking.courant.dto;

import mg.razherana.banking.courant.entities.TransactionCourant;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionCourantDTO {
    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private BigDecimal montant;
    private LocalDateTime date;

    public TransactionCourantDTO() {}

    public TransactionCourantDTO(TransactionCourant transaction) {
        this.id = transaction.getId();
        this.senderId = transaction.getSender() != null ? transaction.getSender().getId() : null;
        this.receiverId = transaction.getReceiver() != null ? transaction.getReceiver().getId() : null;
        this.montant = transaction.getMontant();
        this.date = transaction.getDate();
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
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
}
