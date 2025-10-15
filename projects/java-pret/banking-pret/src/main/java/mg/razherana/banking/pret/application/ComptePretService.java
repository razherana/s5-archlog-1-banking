package mg.razherana.banking.pret.application;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import mg.razherana.banking.pret.entities.ComptePret;
import mg.razherana.banking.pret.entities.TypeComptePret;
import mg.razherana.banking.pret.entities.Echeance;
import mg.razherana.banking.pret.dto.PaymentStatusDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for managing loan account (Compte Pret) operations.
 * 
 * <p>
 * This service provides business logic for loan account management including
 * creation, retrieval, payment processing, and amortization calculations.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Stateless
public class ComptePretService {
  private static final Logger LOG = Logger.getLogger(ComptePretService.class.getName());

  @PersistenceContext(unitName = "pretPU")
  private EntityManager entityManager;

  /**
   * Creates a new loan account.
   * 
   * @param userId           the user ID
   * @param typeComptePretId the loan type ID
   * @param montant          the loan amount
   * @param dateDebut        the loan start date
   * @param dateFin          the loan end date
   * @return the created loan account
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public ComptePret createLoan(Integer userId, Integer typeComptePretId, BigDecimal montant,
      LocalDateTime dateDebut, LocalDateTime dateFin) {
    LOG.info("Creating loan for user: " + userId);

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }
    if (typeComptePretId == null) {
      throw new IllegalArgumentException("Loan type ID cannot be null");
    }
    if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Loan amount must be positive");
    }
    if (dateDebut == null) {
      throw new IllegalArgumentException("Start date cannot be null");
    }
    if (dateFin == null) {
      throw new IllegalArgumentException("End date cannot be null");
    }
    if (dateFin.isBefore(dateDebut)) {
      throw new IllegalArgumentException("End date must be after start date");
    }

    // Verify loan type exists
    TypeComptePret loanType = findLoanTypeById(typeComptePretId);
    if (loanType == null) {
      throw new IllegalArgumentException("Loan type not found: " + typeComptePretId);
    }

    ComptePret compte = new ComptePret(userId, typeComptePretId, montant, dateDebut, dateFin);
    entityManager.persist(compte);
    entityManager.flush();

    LOG.info("Loan created successfully with ID: " + compte.getId());
    return compte;
  }

  /**
   * Finds a loan type by ID.
   */
  public TypeComptePret findLoanTypeById(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("Loan type ID cannot be null");
    }
    return entityManager.find(TypeComptePret.class, id);
  }

  /**
   * Gets all loan types.
   */
  public List<TypeComptePret> getAllLoanTypes() {
    TypedQuery<TypeComptePret> query = entityManager.createQuery(
        "SELECT t FROM TypeComptePret t", TypeComptePret.class);
    return query.getResultList();
  }

  /**
   * Finds a loan account by ID.
   */
  public ComptePret findById(Integer id) {
    LOG.info("Finding loan account by ID: " + id);
    if (id == null) {
      throw new IllegalArgumentException("Loan account ID cannot be null");
    }
    return entityManager.find(ComptePret.class, id);
  }

  /**
   * Find all loans
   */
  public List<ComptePret> findAllLoans() {
    TypedQuery<ComptePret> query = entityManager.createQuery(
        "SELECT c FROM ComptePret c", ComptePret.class);
    return query.getResultList();
  }

  /**
   * Gets all loan accounts for a user.
   */
  public List<ComptePret> getLoansByUserId(Integer userId) {
    LOG.info("Finding loans for userId: " + userId);
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    TypedQuery<ComptePret> query = entityManager.createQuery(
        "SELECT c FROM ComptePret c WHERE c.userId = :userId", ComptePret.class);
    query.setParameter("userId", userId);
    return query.getResultList();
  }

  /**
   * Calculates the monthly payment for a loan using amortization formula.
   * Formula: M = [C × i] / [1 - (1 + i)^(-n)]
   * 
   * @param loan the loan account
   * @return the monthly payment amount
   */
  public BigDecimal calculateMonthlyPayment(ComptePret loan) {
    if (loan == null) {
      throw new IllegalArgumentException("Loan cannot be null");
    }

    TypeComptePret loanType = findLoanTypeById(loan.getTypeComptePretId());
    if (loanType == null) {
      throw new IllegalArgumentException("Loan type not found");
    }

    BigDecimal principal = loan.getMontant(); // C
    BigDecimal annualRate = loanType.getInteret(); // Annual interest rate

    // Calculate number of months between start and end date
    long totalMonths = ChronoUnit.MONTHS.between(loan.getDateDebut(), loan.getDateFin());
    if (totalMonths <= 0) {
      throw new IllegalArgumentException("Invalid loan duration");
    }

    // Convert annual rate to monthly rate: i = annual/12
    BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

    // If interest rate is 0, simple division
    if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
      return principal.divide(BigDecimal.valueOf(totalMonths), 2, RoundingMode.HALF_UP);
    }

    // Calculate (1 + i)^(-n)
    BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
    BigDecimal powerTerm = BigDecimal.ONE.divide(
        onePlusRate.pow((int) totalMonths), 10, RoundingMode.HALF_UP);

    // Calculate denominator: [1 - (1 + i)^(-n)]
    BigDecimal denominator = BigDecimal.ONE.subtract(powerTerm);

    // Calculate numerator: [C × i]
    BigDecimal numerator = principal.multiply(monthlyRate);

    // Final calculation: M = numerator / denominator
    return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
  }

  /**
   * Gets all payments for a loan account.
   */
  public List<Echeance> getPaymentHistory(Integer compteId) {
    if (compteId == null) {
      throw new IllegalArgumentException("Loan account ID cannot be null");
    }

    TypedQuery<Echeance> query = entityManager.createQuery(
        "SELECT e FROM Echeance e WHERE e.compteId = :compteId ORDER BY e.dateEcheance",
        Echeance.class);
    query.setParameter("compteId", compteId);
    return query.getResultList();
  }

  /**
   * Calculates total amount paid for a loan.
   */
  public BigDecimal calculateTotalPaid(Integer compteId) {
    if (compteId == null) {
      throw new IllegalArgumentException("Loan account ID cannot be null");
    }

    TypedQuery<BigDecimal> query = entityManager.createQuery(
        "SELECT COALESCE(SUM(e.montant), 0) FROM Echeance e WHERE e.compteId = :compteId",
        BigDecimal.class);
    query.setParameter("compteId", compteId);

    BigDecimal result = query.getSingleResult();
    return result != null ? result : BigDecimal.ZERO;
  }

  /**
   * Calculates the expected amount to be paid by a specific date.
   */
  public BigDecimal calculateExpectedPaidByDate(ComptePret loan, LocalDateTime actionDateTime) {
    if (loan == null || actionDateTime == null) {
      throw new IllegalArgumentException("Loan and action date cannot be null");
    }

    // If action date is before loan start, nothing is expected
    if (actionDateTime.isBefore(loan.getDateDebut())) {
      return BigDecimal.ZERO;
    }

    // If action date is after loan end, full loan amount is expected
    if (actionDateTime.isAfter(loan.getDateFin())) {
      return loan.getMontant();
    }

    // Calculate months elapsed since loan start
    long monthsElapsed = ChronoUnit.MONTHS.between(loan.getDateDebut(), actionDateTime);
    if (monthsElapsed < 0) {
      monthsElapsed = 0;
    }

    BigDecimal monthlyPayment = calculateMonthlyPayment(loan);
    return monthlyPayment.multiply(BigDecimal.valueOf(monthsElapsed));
  }

  /**
   * Gets payment status for a loan at a specific date.
   */
  public PaymentStatusDTO getPaymentStatus(Integer compteId, LocalDateTime actionDateTime) {
    if (compteId == null) {
      throw new IllegalArgumentException("Loan account ID cannot be null");
    }
    if (actionDateTime == null) {
      actionDateTime = LocalDateTime.now();
    }

    ComptePret loan = findById(compteId);
    if (loan == null) {
      throw new IllegalArgumentException("Loan account not found: " + compteId);
    }

    BigDecimal totalPaid = calculateTotalPaid(compteId);
    BigDecimal totalExpected = calculateExpectedPaidByDate(loan, actionDateTime);
    BigDecimal monthlyPayment = calculateMonthlyPayment(loan);

    // Amount due is the difference between expected and paid
    BigDecimal amountDue = totalExpected.subtract(totalPaid);
    if (amountDue.compareTo(BigDecimal.ZERO) < 0) {
      amountDue = BigDecimal.ZERO; // Can't have negative due amount
    }

    // Check if loan is fully paid
    boolean isFullyPaid = totalPaid.compareTo(loan.getMontant()) >= 0;

    return new PaymentStatusDTO(totalPaid, totalExpected, amountDue, isFullyPaid, monthlyPayment);
  }

  /**
   * Makes a payment for a loan.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Echeance makePayment(Integer compteId, BigDecimal amount, LocalDateTime actionDateTime) {
    if (compteId == null) {
      throw new IllegalArgumentException("Loan account ID cannot be null");
    }
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Payment amount must be positive");
    }
    if (actionDateTime == null) {
      actionDateTime = LocalDateTime.now();
    }

    ComptePret loan = findById(compteId);
    if (loan == null) {
      throw new IllegalArgumentException("Loan account not found: " + compteId);
    }

    // Check if loan is already fully paid
    PaymentStatusDTO status = getPaymentStatus(compteId, actionDateTime);
    if (status.isFullyPaid()) {
      throw new IllegalArgumentException("Loan is already fully paid");
    }

    // Create payment record
    Echeance payment = new Echeance(compteId, amount, actionDateTime);
    entityManager.persist(payment);
    entityManager.flush();

    LOG.info("Payment of " + amount + " made for loan " + compteId);
    return payment;
  }
}