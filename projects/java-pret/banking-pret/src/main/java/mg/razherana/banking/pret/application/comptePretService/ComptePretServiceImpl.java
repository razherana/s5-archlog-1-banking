package mg.razherana.banking.pret.application.comptePretService;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import mg.razherana.banking.pret.entities.ComptePret;
import mg.razherana.banking.pret.entities.TypeComptePret;
import mg.razherana.banking.pret.entities.Echeance;
import mg.razherana.banking.pret.entities.User;
import mg.razherana.banking.pret.dto.PaymentStatusDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of the ComptePretService interface.
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
public class ComptePretServiceImpl implements ComptePretService {
  private static final Logger LOG = Logger.getLogger(ComptePretServiceImpl.class.getName());

  // Hardcoded URL for java-interface REST API
  private static final String USER_SERVICE_BASE_URL = "http://127.0.0.2:8080/api";

  @PersistenceContext(unitName = "pretPU")
  private EntityManager entityManager;

  /**
   * Find a user by ID using REST API call to java-interface.
   * 
   * @param userId the user ID
   * @return User object with the specified ID or null if not found
   * @throws IllegalArgumentException if userId is null
   */
  @Override
  public User findUser(Integer userId) {
    LOG.info("Finding user by ID: " + userId);
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    Client client = ClientBuilder.newClient();
    try {
      WebTarget target = client.target(USER_SERVICE_BASE_URL + "/users/" + userId);
      Response response = target.request(MediaType.APPLICATION_JSON).get();

      if (response.getStatus() == 200) {
        // java-interface returns UserDTO, so we need to parse it and map to our User
        // entity
        String jsonResponse = response.readEntity(String.class);
        LOG.info("Received JSON response: " + jsonResponse);

        // Parse the UserDTO JSON response
        JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
        JsonObject userDto = jsonReader.readObject();
        jsonReader.close();

        // Map UserDTO fields to User entity
        User user = new User();
        user.setId(userDto.getInt("id"));
        user.setName(userDto.getString("name"));
        user.setEmail(userDto.getString("email"));
        user.setPassword(""); // Password not returned by UserDTO for security

        LOG.info("Successfully retrieved and mapped user from REST API: " + user.getId());
        return user;
      }

      return null; // User not found
    } catch (Exception e) {
      LOG.severe("Error calling REST UserService: " + e.getMessage());
      throw new IllegalArgumentException(e.getMessage());
    } finally {
      client.close();
    }
  }

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
  @Override
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

    // Verify user exists by calling the user service
    User user = findUser(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found: " + userId);
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
  @Override
  public TypeComptePret findLoanTypeById(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("Loan type ID cannot be null");
    }
    return entityManager.find(TypeComptePret.class, id);
  }

  /**
   * Returns all available loan types.
   */
  @Override
  public List<TypeComptePret> getAllLoanTypes() {
    TypedQuery<TypeComptePret> query = entityManager.createQuery(
        "SELECT t FROM TypeComptePret t", TypeComptePret.class);
    return query.getResultList();
  }

  /**
   * Finds a loan account by ID.
   */
  @Override
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
  @Override
  public List<ComptePret> findAllLoans() {
    TypedQuery<ComptePret> query = entityManager.createQuery(
        "SELECT c FROM ComptePret c", ComptePret.class);
    return query.getResultList();
  }

  /**
   * Gets all loan accounts for a user.
   */
  @Override
  public List<ComptePret> getLoansByUserId(Integer userId) {
    LOG.info("Finding loans for userId: " + userId);
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    // Verify user exists by calling the user service
    User user = findUser(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found: " + userId);
    }

    TypedQuery<ComptePret> query = entityManager.createQuery(
        "SELECT c FROM ComptePret c WHERE c.userId = :userId", ComptePret.class);
    query.setParameter("userId", userId);
    return query.getResultList();
  }

  /**
   * Calculate the total remaining balance for all loans of a user.
   * Balance = Sum of (original loan amount - total payments made)
   * 
   * @param userId the user ID
   * @return total remaining balance across all user's loans
   */
  @Override
  public BigDecimal calculateTotalSoldeByUserId(Integer userId) {
    LOG.info("Calculating total loan balance for userId: " + userId);
    
    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    // Verify user exists (will throw exception if not found)
    findUser(userId);
    
    // Get all user's loans
    List<ComptePret> loans = getLoansByUserId(userId);
    
    // Calculate total remaining balance
    BigDecimal totalBalance = BigDecimal.ZERO;
    for (ComptePret loan : loans) {
      BigDecimal originalAmount = loan.getMontant();
      BigDecimal totalPaid = calculateTotalPaid(loan.getId());
      BigDecimal remainingBalance = originalAmount.subtract(totalPaid);
      
      // Only add positive balances (loans not overpaid)
      if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
        totalBalance = totalBalance.add(remainingBalance);
      }
      
      LOG.info("Loan " + loan.getId() + " - Original: " + originalAmount + 
               ", Paid: " + totalPaid + ", Remaining: " + remainingBalance);
    }
    
    LOG.info("Total loan balance for user " + userId + ": " + totalBalance);
    return totalBalance;
  }

  /**
   * Calculates the monthly payment for a loan using the standard amortization
   * formula.
   * Formula: M = [C × i] / [1 - (1 + i)^(-n)]
   * Where: M = monthly payment, C = capital (amount), i = monthly interest rate,
   * n = number of months
   */
  @Override
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
    // Add 1 to include the starting month
    long totalMonths = monthPassed(loan.getDateDebut(), loan.getDateFin()) + 1;
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

    System.out.println("-------------- Variables calculateMonthlyPayment: -----------------------");

    System.out.println("Principal (C): " + principal);
    System.out.println("Annual Rate: " + annualRate);
    System.out.println("Monthly Rate (i): " + monthlyRate);
    System.out.println("Total Months (n): " + totalMonths);

    System.out.println("Date debut: " + loan.getDateDebut());
    System.out.println("Date fin: " + loan.getDateFin());

    System.out.println("C * i: " + numerator);
    System.out.println("1 + i: " + onePlusRate);

    System.out.println("(1 + i) ^ -n: " + powerTerm);

    System.out.println("1 - (1 + i) ^ -n: " + denominator);

    System.out.println("M = C * i / [1 - (1 + i) ^ -n] : " + numerator.divide(denominator, 6, RoundingMode.HALF_UP));

    System.out.println("---------------- End Variables -----------------------");

    // Final calculation: M = numerator / denominator
    return numerator.divide(denominator, 6, RoundingMode.HALF_UP);
  }

  /**
   * Gets all payments for a loan account.
   */
  @Override
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
  @Override
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
   * Calculates the total expected amount to be paid over the life of the loan.
   */
  private BigDecimal getExpectedTotal(ComptePret loan) {
    if (loan == null) {
      throw new IllegalArgumentException("Loan cannot be null");
    }

    // Calculate number of months between start and end date
    // Add 1 to include the starting month
    long totalMonths = monthPassed(loan.getDateDebut(), loan.getDateFin()) + 1;
    if (totalMonths <= 0) {
      throw new IllegalArgumentException("Invalid loan duration");
    }

    BigDecimal monthlyPayment = calculateMonthlyPayment(loan);
    return monthlyPayment.multiply(BigDecimal.valueOf(totalMonths));
  }

  /**
   * Calculates the expected amount to be paid by a specific date.
   */
  @Override
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
      return getExpectedTotal(loan);
    }

    // Calculate months elapsed since loan start
    long monthsElapsed = monthPassed(loan.getDateDebut(), actionDateTime) + 1;
    if (monthsElapsed < 0) {
      monthsElapsed = 0;
    }

    BigDecimal monthlyPayment = calculateMonthlyPayment(loan);
    return monthlyPayment.multiply(BigDecimal.valueOf(monthsElapsed));
  }

  private long monthPassed(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end date cannot be null");
    }

    // If end date is before start, return 0
    if (end.isBefore(start)) {
      return 0;
    }

    // Calculate months elapsed since start
    long calendarMonths = (end.getYear() - start.getYear()) * 12 +
        (end.getMonthValue() - start.getMonthValue());
    return calendarMonths < 0 ? 0 : calendarMonths;
  }

  /**
   * Gets payment status for a loan at a specific date.
   */
  @Override
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
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public Echeance makePayment(Integer compteId, BigDecimal amount, LocalDateTime actionDateTime) {
    if (compteId == null)
      throw new IllegalArgumentException("Loan account ID cannot be null");
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
      throw new IllegalArgumentException("Payment amount must be positive");
    if (actionDateTime == null)
      actionDateTime = LocalDateTime.now();

    ComptePret loan = findById(compteId);
    if (loan == null) {
      throw new IllegalArgumentException("Loan account not found: " + compteId);
    }

    // Get current payment status
    PaymentStatusDTO status = getPaymentStatus(compteId, actionDateTime);

    // Check if loan is already fully paid
    if (status.isFullyPaid()) {
      throw new IllegalArgumentException("Loan is already fully paid");
    }

    // Calculate total amount that would be paid after this payment
    BigDecimal currentTotalPaid = calculateTotalPaid(compteId);
    BigDecimal totalAfterPayment = currentTotalPaid.add(amount);
    BigDecimal loanAmount = getExpectedTotal(loan);

    // Check for overpayment
    if (totalAfterPayment.compareTo(loanAmount) > 0) {
      BigDecimal maxPossiblePayment = loanAmount.subtract(currentTotalPaid);
      BigDecimal excessAmount = amount.subtract(maxPossiblePayment);

      String errorMessage = String.format(
          "Payment exceeds remaining loan balance. " +
              "Reason: Overpayment detected. " +
              "Amount attempted: %s MGA. " +
              "Maximum possible payment: %s MGA. " +
              "Excess amount: %s MGA.",
          amount.toString(),
          maxPossiblePayment.toString(),
          excessAmount.toString());

      throw new IllegalArgumentException(errorMessage);
    }

    // Create payment record
    Echeance payment = new Echeance(compteId, amount, actionDateTime);
    entityManager.persist(payment);
    entityManager.flush();

    LOG.info("Payment of " + amount + " made for loan " + compteId);
    return payment;
  }
}