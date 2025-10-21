package mg.razherana.banking.interfaces.web.controllers.compteCourant.accountStatusDTOs;

import java.time.format.DateTimeFormatter;

import mg.razherana.banking.courant.entities.TransactionCourant;

public class TransactionDTO extends TransactionCourant {
  public TransactionDTO(TransactionCourant transaction) {
    this.setId(transaction.getId());
    this.setSender(transaction.getSender());
    this.setReceiver(transaction.getReceiver());
    this.setMontant(transaction.getMontant());
    this.setDate(transaction.getDate());
    this.setSpecialAction(transaction.getSpecialAction());
  }

  public boolean isPositiveForAccount(Integer accountId) {
    if (this.getReceiver() != null && this.getReceiver().getId().equals(accountId)) {
      return true;
    }
    return false;
  }

  public boolean isNegativeForAccount(Integer accountId) {
    if (this.getSender() != null && this.getSender().getId().equals(accountId)) {
      return true;
    }
    return false;
  }

  public Integer getReceiverId() {
    return this.getReceiver() != null ? this.getReceiver().getId() : null;
  }

  public Integer getSenderId() {
    return this.getSender() != null ? this.getSender().getId() : null;
  }

  public String getTransactionType() {
    String specialAction = getSpecialAction();
    if (specialAction == null) {
      return "Transfert";
    }

    // Map database values to enum constants
    if (TransactionCourant.SpecialAction.DEPOSIT.getDatabaseName().equals(specialAction)) {
      return "Dépôt";
    } else if (TransactionCourant.SpecialAction.WITHDRAWAL.getDatabaseName().equals(specialAction)) {
      return "Retrait";
    } else if (TransactionCourant.SpecialAction.TAXE.getDatabaseName().equals(specialAction)) {
      return "Taxe";
    }

    return "Transfert";
  }

  public String getFormattedAmount() {
    return String.format("%,.2f MGA", this.getMontant());
  }

  final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

  public String getFormattedDate() {
    return this.getDate().format(formatter);
  }
}