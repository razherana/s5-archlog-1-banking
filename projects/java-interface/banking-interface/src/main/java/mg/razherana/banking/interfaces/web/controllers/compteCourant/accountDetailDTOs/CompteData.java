package mg.razherana.banking.interfaces.web.controllers.compteCourant.accountDetailDTOs;

import java.math.BigDecimal;

public class CompteData {
  public Integer id;
  public Integer userId;
  public BigDecimal solde;

  public CompteData(Integer id, Integer userId, BigDecimal solde) {
    this.id = id;
    this.userId = userId;
    this.solde = solde;
  }

  public Integer getId() {
    return id;
  }

  public Integer getUserId() {
    return userId;
  }

  public BigDecimal getSolde() {
    return solde;
  }
}