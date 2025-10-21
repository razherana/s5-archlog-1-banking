package mg.razherana.banking.pret.application.comptePretService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import mg.razherana.banking.pret.dto.PaymentStatusDTO;
import mg.razherana.banking.pret.entities.ComptePret;
import mg.razherana.banking.pret.entities.Echeance;
import mg.razherana.banking.pret.entities.TypeComptePret;
import mg.razherana.banking.pret.entities.User;

@Stateless
public class ComptePretServiceRemoteImpl implements ComptePretServiceRemote {

  @EJB
  private ComptePretService comptePretService;

  @Override
  public User findUser(Integer userId) {
    return comptePretService.findUser(userId);
  }

  @Override
  public ComptePret createLoan(Integer userId, Integer typeComptePretId, BigDecimal montant, LocalDateTime dateDebut,
      LocalDateTime dateFin) {
    return comptePretService.createLoan(userId, typeComptePretId, montant, dateDebut, dateFin);
  }

  @Override
  public TypeComptePret findLoanTypeById(Integer id) {
    return comptePretService.findLoanTypeById(id);
  }

  @Override
  public List<TypeComptePret> getAllLoanTypes() {
    return comptePretService.getAllLoanTypes();
  }

  @Override
  public ComptePret findById(Integer id) {
    return comptePretService.findById(id);
  }

  @Override
  public List<ComptePret> findAllLoans() {
    return comptePretService.findAllLoans();
  }

  @Override
  public List<ComptePret> getLoansByUserId(Integer userId) {
    return comptePretService.getLoansByUserId(userId);
  }

  @Override
  public BigDecimal calculateTotalSoldeByUserId(Integer userId, LocalDateTime actionDateTime) {
    return comptePretService.calculateTotalSoldeByUserId(userId, actionDateTime);
  }

  @Override
  public BigDecimal calculateMonthlyPayment(ComptePret loan) {
    return comptePretService.calculateMonthlyPayment(loan);
  }

  @Override
  public List<Echeance> getPaymentHistory(Integer compteId) {
    return comptePretService.getPaymentHistory(compteId);
  }

  @Override
  public BigDecimal calculateTotalPaid(Integer compteId) {
    return comptePretService.calculateTotalPaid(compteId);
  }

  @Override
  public BigDecimal calculateTotalPaidAtDate(Integer compteId, LocalDateTime actionDateTime) {
    return comptePretService.calculateTotalPaidAtDate(compteId, actionDateTime);
  }

  @Override
  public BigDecimal calculateExpectedPaidByDate(ComptePret loan, LocalDateTime actionDateTime) {
    return comptePretService.calculateExpectedPaidByDate(loan, actionDateTime);
  }

  @Override
  public PaymentStatusDTO getPaymentStatus(Integer compteId, LocalDateTime actionDateTime) {
    return comptePretService.getPaymentStatus(compteId, actionDateTime);
  }

  @Override
  public Echeance makePayment(Integer compteId, BigDecimal amount, LocalDateTime actionDateTime) {
    return comptePretService.makePayment(compteId, amount, actionDateTime);
  }
}
