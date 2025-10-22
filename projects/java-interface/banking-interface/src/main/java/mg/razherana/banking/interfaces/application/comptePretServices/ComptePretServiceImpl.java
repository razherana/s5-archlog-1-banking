package mg.razherana.banking.interfaces.application.comptePretServices;

import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.interfaces.dto.comptePret.*;
import mg.razherana.banking.pret.application.comptePretService.ComptePretServiceRemote;
import mg.razherana.banking.pret.entities.ComptePret;
import mg.razherana.banking.pret.entities.Echeance;
import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.remoteServices.EJBLookupService;
import mg.razherana.banking.courant.entities.TransactionCourant;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service implementation for communicating with the banking-pret service.
 */
@Stateless
public class ComptePretServiceImpl implements ComptePretService {

  private static final Logger LOG = Logger.getLogger(ComptePretServiceImpl.class.getName());

  private EJBLookupService remotePret;

  private ComptePretServiceRemote comptePretRemoteService;

  @EJB
  private CompteCourantService compteCourantService;

  public ComptePretServiceImpl() {
    try {
      this.remotePret = new EJBLookupService("127.0.0.3");
      this.comptePretRemoteService = remotePret.lookupStatefulBean(
          "global/ComptePretServiceRemoteImpl!mg.razherana.banking.pret.application.comptePretService.ComptePretServiceRemote",
          ComptePretServiceRemote.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to initialize EJBLookupService", e);
    }
  }

  @Override
  public List<TypeComptePretDTO> getAllLoanTypes(UserAdmin userAdmin) {
    if (!comptePretRemoteService.hasAuthorization(userAdmin, "READ", "type_compte_prets")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read loan types");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read loan types");
    }
    var loanTypes = comptePretRemoteService.getAllLoanTypes();
    return loanTypes.stream()
        .map(type -> {
          TypeComptePretDTO dto = new TypeComptePretDTO();
          dto.setId(type.getId());
          dto.setNom(type.getNom());
          dto.setInteret(type.getInteret());
          return dto;
        })
        .toList();
  }

  private ComptePretDTO convertToDTO(ComptePret comptePret, BigDecimal monthlyPayment) {
    ComptePretDTO dto = new ComptePretDTO();
    dto.setId(comptePret.getId());
    dto.setUserId(comptePret.getUserId());
    dto.setTypeComptePretId(comptePret.getTypeComptePretId());
    dto.setMontant(comptePret.getMontant());
    dto.setDateDebut(comptePret.getDateDebut());
    dto.setDateFin(comptePret.getDateFin());

    // Calculate monthly payment
    dto.setMonthlyPayment(monthlyPayment);
    return dto;
  }

  @Override
  public List<ComptePretDTO> getLoansByUserId(UserAdmin userAdmin, Integer userId) {
    if (!comptePretRemoteService.hasAuthorization(userAdmin, "READ", "compte_prets")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read loans");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read loans");
    }
    var loans = comptePretRemoteService.getLoansByUserId(userId);
    List<ComptePretDTO> loanDTOs = new ArrayList<>();
    for (var loan : loans) {
      // Calculate monthly payment
      BigDecimal monthlyPayment = comptePretRemoteService.calculateMonthlyPayment(loan);

      ComptePretDTO dto = convertToDTO(loan, monthlyPayment);
      loanDTOs.add(dto);
    }
    return loanDTOs;
  }

  @Override
  public ComptePretDTO getLoanById(UserAdmin userAdmin, Integer loanId) {
    if (!comptePretRemoteService.hasAuthorization(userAdmin, "READ", "compte_prets")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read loans");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read loans");
    }
    var loan = comptePretRemoteService.findById(loanId);
    if (loan == null) {
      return null;
    }

    // Calculate monthly payment
    BigDecimal monthlyPayment = comptePretRemoteService.calculateMonthlyPayment(loan);

    return convertToDTO(loan, monthlyPayment);
  }

  @Override
  public ComptePretDTO createLoan(UserAdmin userAdmin, CreateComptePretRequest request) {
    if (!comptePretRemoteService.hasAuthorization(userAdmin, "CREATE", "compte_prets")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create loans");
      throw new IllegalStateException("Unauthorized access: User does not have permission to create loans");
    }
    var createdLoanObj = comptePretRemoteService.createLoan(
        request.getUserId(),
        request.getTypeComptePretId(),
        request.getMontant(),
        request.getDateDebut(),
        request.getDateFin());

    if (createdLoanObj == null)
      throw new IllegalStateException("Loan creation failed");

    var monthlyPayment = comptePretRemoteService.calculateMonthlyPayment(createdLoanObj);

    ComptePretDTO createdLoan = convertToDTO(createdLoanObj, monthlyPayment);

    LOG.info("Loan created successfully with ID: " + createdLoan.getId());

    // Now deposit the loan amount to the specified current account
    if (request.getCompteCourantId() != null) {
      try {
        String description = "Prêt #" + createdLoan.getId() + " - Versement du montant emprunté";
        TransactionCourant depositTransaction = compteCourantService.makeDeposit(
            userAdmin,
            request.getCompteCourantId(),
            request.getMontant(),
            description,
            request.getDateDebut());

        if (depositTransaction != null) {
          LOG.info("Loan amount deposited successfully to current account " + request.getCompteCourantId() +
              ", transaction ID: " + depositTransaction.getId());
        } else {
          LOG.severe("Failed to deposit loan amount to current account - transaction returned null");
        }
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "Error depositing loan amount to current account: " + e.getMessage(), e);
      }
    }

    return createdLoan;
  }

  @Override
  public EcheanceDTO makePayment(UserAdmin userAdmin, MakePaymentRequest request) {
    if (!comptePretRemoteService.hasAuthorization(userAdmin, "CREATE", "echeances")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to create payments");
      throw new IllegalStateException("Unauthorized access: User does not have permission to create payments");
    }
    Echeance payment = comptePretRemoteService.makePayment(
        request.getCompteId(),
        request.getMontant(),
        request.getActionDateTime());

    if (payment != null) {
      EcheanceDTO dto = new EcheanceDTO();
      dto.setId(payment.getId());
      dto.setCompteId(payment.getCompteId());
      dto.setMontant(payment.getMontant());
      dto.setDateEcheance(payment.getDateEcheance());
      return dto;
    }

    throw new IllegalStateException("Payment processing failed");
  }

  @Override
  public PaymentStatusDTO getPaymentStatus(UserAdmin userAdmin, Integer loanId) {
    if (!comptePretRemoteService.hasAuthorization(userAdmin, "READ", "echeances")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read payment status");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read payment status");
    }
    return getPaymentStatus(userAdmin, loanId, LocalDateTime.now());
  }

  @Override
  public PaymentStatusDTO getPaymentStatus(UserAdmin userAdmin, Integer loanId, LocalDateTime actionDateTime) {
    if (!comptePretRemoteService.hasAuthorization(userAdmin, "READ", "echeances")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read payment status");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read payment status");
    }
    var ogStatus = comptePretRemoteService.getPaymentStatus(loanId, actionDateTime);
    if (ogStatus == null)
      throw new IllegalStateException("Failed to retrieve payment status");

    var status = new PaymentStatusDTO();
    status.setAmountDue(ogStatus.getAmountDue());
    status.setFullyPaid(ogStatus.isFullyPaid());
    status.setMonthlyPayment(ogStatus.getMonthlyPayment());
    status.setTotalExpected(ogStatus.getTotalExpected());
    status.setTotalPaid(ogStatus.getTotalPaid());

    return status;
  }

  @Override
  public List<EcheanceDTO> getPaymentHistory(UserAdmin userAdmin, Integer loanId) {
    if (!comptePretRemoteService.hasAuthorization(userAdmin, "READ", "echeances")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read payment history");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read payment history");
    }
    var payments = comptePretRemoteService.getPaymentHistory(loanId);
    return payments.stream()
        .map(payment -> {
          EcheanceDTO dto = new EcheanceDTO();
          dto.setId(payment.getId());
          dto.setCompteId(payment.getCompteId());
          dto.setMontant(payment.getMontant());
          dto.setDateEcheance(payment.getDateEcheance());
          return dto;
        })
        .toList();
  }

  @Override
  public List<EcheanceDTO> getPaymentHistory(UserAdmin userAdmin, Integer loanId, LocalDateTime actionDateTime) {
    if (!comptePretRemoteService.hasAuthorization(userAdmin, "READ", "echeances")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read payment history");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read payment history");
    }
    return getPaymentHistory(userAdmin, loanId).stream()
        .filter(payment -> !payment.getDateEcheance().isAfter(actionDateTime))
        .toList();
  }

  @Override
  public BigDecimal getLoanBalanceByUserId(UserAdmin userAdmin, Integer userId, LocalDateTime actionDateTime) {
    if (!comptePretRemoteService.hasAuthorization(userAdmin, "READ", "compte_prets")) {
      LOG.warning("User " + userAdmin.getEmail() + " does not have authorization to read loan balances");
      throw new IllegalStateException("Unauthorized access: User does not have permission to read loan balances");
    }
    return comptePretRemoteService.calculateTotalSoldeByUserId(userId, actionDateTime);
  }
}