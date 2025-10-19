package mg.razherana.banking.interfaces.application.compteCourantServices;

import mg.razherana.banking.interfaces.application.userServices.UserService;
import mg.razherana.banking.interfaces.dto.CompteCourantDTO;
import mg.razherana.banking.interfaces.dto.TransactionCourantDTO;
import mg.razherana.banking.interfaces.dto.UserDTO;
import mg.razherana.banking.interfaces.entities.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service implementation for communicating with the banking-courant service.
 */
@Stateless
public class CompteCourantServiceImpl implements CompteCourantService {

  private static final Logger LOG = Logger.getLogger(CompteCourantServiceImpl.class.getName());
  private static final String BANKING_COURANT_BASE_URL = "http://localhost:8080/api";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  @EJB
  private UserService userService;

  public CompteCourantServiceImpl() {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  /**
   * Extracts error message from API response body.
   * Attempts to parse JSON error response, falls back to raw response if parsing
   * fails.
   */
  private String extractErrorMessage(String responseBody) {
    try {
      // Try to parse as JSON error response
      if (responseBody != null && responseBody.trim().startsWith("{")) {
        var jsonNode = objectMapper.readTree(responseBody);
        if (jsonNode.has("message")) {
          return jsonNode.get("message").asText();
        }
        if (jsonNode.has("error")) {
          return jsonNode.get("error").asText();
        }
      }
      // If not JSON or no error field, return the raw response
      return responseBody != null ? responseBody : "Erreur inconnue";
    } catch (Exception e) {
      // If JSON parsing fails, return raw response
      return responseBody != null ? responseBody : "Erreur inconnue";
    }
  }

  @Override
  public List<CompteCourantDTO> getAccountsByUserId(Integer userId) {
    try {
      String url = BANKING_COURANT_BASE_URL + "/comptes/user/" + userId;

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      LOG.info("Fetching accounts for user " + userId + " from: " + url);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        CompteCourantDTO[] accounts = objectMapper.readValue(
            response.body(), CompteCourantDTO[].class);
        return Arrays.asList(accounts);
      } else {
        LOG.warning("Failed to fetch accounts. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return new ArrayList<>();
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching accounts for user " + userId, e);
      return new ArrayList<>();
    }
  }

  @Override
  public CompteCourantDTO createAccount(Integer userId, Double taxe, String actionDateTime) {
    try {
      String url = BANKING_COURANT_BASE_URL + "/comptes/user/" + userId;

      // Only add taxe parameter (actionDateTime not supported by banking-courant for
      // account creation)
      if (taxe != null && taxe > 0) {
        url += "?taxe=" + taxe;
      }

      if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
        url += (url.contains("?") ? "&" : "?") + "actionDateTime=" + actionDateTime;
      }

      LOG.info("REQUEST_URL: Create Account URL: " + url);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString("{}"))
          .build();

      LOG.info("Creating account for user " + userId + " at: " + url);
      if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
        LOG.warning(
            "actionDateTime specified for account creation but not supported by banking-courant service. Using current time.");
      }

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        CompteCourantDTO account = objectMapper.readValue(
            response.body(), CompteCourantDTO.class);
        LOG.info("Account created successfully with ID: " + account.getId());
        return account;
      } else {
        LOG.warning("Failed to create account. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error creating account for user " + userId, e);
      return null;
    }
  }

  @Override
  public CompteCourantDTO getAccountById(Integer accountId) {
    try {
      String url = BANKING_COURANT_BASE_URL + "/comptes/" + accountId;

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      LOG.info("Fetching account " + accountId + " from: " + url);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        CompteCourantDTO account = objectMapper.readValue(
            response.body(), CompteCourantDTO.class);
        LOG.info("Fetched account successfully: " + account);
        LOG.info("RESPONSE_BODY: Fetched account successfully: " + response.body());
        return account;
      } else {
        LOG.warning("Failed to fetch account. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching account " + accountId, e);
      return null;
    }
  }

  public static class TaxToPayWrapper {
    public Double taxToPay;
  }

  @Override
  public Double getTaxToPay(Integer accountId) {
    try {
      String url = BANKING_COURANT_BASE_URL + "/comptes/" + accountId + "/tax-to-pay";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      LOG.info("Fetching tax to pay for account " + accountId + " from: " + url);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), TaxToPayWrapper.class).taxToPay;
      } else {
        LOG.warning("Failed to fetch tax to pay. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return 0.0;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching tax to pay for account " + accountId, e);
      return 0.0;
    }
  }

  @Override
  public String makeDeposit(Integer accountId, Double montant, String description, String actionDateTime) {
    try {
      String url = BANKING_COURANT_BASE_URL + "/transactions/depot";

      // Build request body
      StringBuilder requestBodyBuilder = new StringBuilder();
      requestBodyBuilder.append("{\"compteId\":").append(accountId)
          .append(",\"montant\":").append(montant)
          .append(",\"description\":\"").append(description != null ? description : "Deposit").append("\"");

      if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
        requestBodyBuilder.append(",\"actionDateTime\":\"").append(actionDateTime).append("\"");
      }

      requestBodyBuilder.append("}");
      String requestBody = requestBodyBuilder.toString();

      LOG.info("REQUEST_BODY: Make Deposit: " + requestBody);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      LOG.info("Making deposit to account " + accountId + " amount: " + montant);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        LOG.info("Deposit successful");
        return null; // Success
      } else {
        String errorMessage = extractErrorMessage(response.body());
        LOG.warning("Failed to make deposit. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return errorMessage;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error making deposit", e);
      return "Erreur de communication avec le service bancaire: " + e.getMessage();
    }
  }

  @Override
  public String makeWithdrawal(Integer accountId, Double montant, String description, String actionDateTime) {
    try {
      String url = BANKING_COURANT_BASE_URL + "/transactions/retrait";

      // Build request body
      StringBuilder requestBodyBuilder = new StringBuilder();
      requestBodyBuilder.append("{\"compteId\":").append(accountId)
          .append(",\"montant\":").append(montant)
          .append(",\"description\":\"").append(description != null ? description : "Withdrawal").append("\"");

      if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
        requestBodyBuilder.append(",\"actionDateTime\":\"").append(actionDateTime).append("\"");
      }

      requestBodyBuilder.append("}");
      String requestBody = requestBodyBuilder.toString();

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      LOG.info("Making withdrawal from account " + accountId + " amount: " + montant);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        LOG.info("Withdrawal successful");
        return null; // Success
      } else {
        String errorMessage = extractErrorMessage(response.body());
        LOG.warning("Failed to make withdrawal. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return errorMessage;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error making withdrawal", e);
      return "Erreur de communication avec le service bancaire: " + e.getMessage();
    }
  }

  @Override
  public String payTax(Integer accountId, String description, String actionDateTime) {
    try {
      String url = BANKING_COURANT_BASE_URL + "/transactions/pay-tax";

      // Build request body
      StringBuilder requestBodyBuilder = new StringBuilder();
      requestBodyBuilder.append("{\"compteId\":").append(accountId)
          .append(",\"description\":\"").append(description != null ? description : "Tax payment").append("\"");

      if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
        requestBodyBuilder.append(",\"actionDateTime\":\"").append(actionDateTime).append("\"");
      }

      requestBodyBuilder.append("}");
      String requestBody = requestBodyBuilder.toString();

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      LOG.info("Paying tax for account " + accountId);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        LOG.info("Tax payment successful");
        return null; // Success
      } else {
        String errorMessage = extractErrorMessage(response.body());
        LOG.warning("Failed to pay tax. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return errorMessage;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error paying tax", e);
      return "Erreur de communication avec le service bancaire: " + e.getMessage();
    }
  }

  @Override
  public List<UserDTO> getAllUsers() {
    try {
      List<User> users = userService.getAllUsers();

      // Extract unique user IDs and create UserDTOs
      List<UserDTO> userDTOs = new ArrayList<>();

      for (User user : users) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());

        userDTOs.add(userDTO);
      }

      return userDTOs;

    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting all users", e);
      return new ArrayList<>();
    }
  }

  /**
   * Helper method to get all accounts from the banking-courant service.
   */
  @Override
  public List<CompteCourantDTO> getAllAccounts() {
    try {
      String url = BANKING_COURANT_BASE_URL + "/comptes";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      LOG.info("Fetching all accounts from: " + url);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        System.out.println("RESPONSE_BODY: " + response.body());
        CompteCourantDTO[] accounts = objectMapper.readValue(
            response.body(), CompteCourantDTO[].class);
        return Arrays.asList(accounts);
      } else {
        LOG.warning("Failed to fetch all accounts. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return new ArrayList<>();
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching all accounts", e);
      return new ArrayList<>();
    }
  }

  @Override
  public String makeTransfer(Integer sourceAccountId, Integer destinationAccountId, Double amount, String description,
      String actionDateTime) {
    try {
      String url = BANKING_COURANT_BASE_URL + "/transactions/transfert";

      // Create request body manually to match the expected format
      StringBuilder requestBodyBuilder = new StringBuilder();
      requestBodyBuilder.append("{\"compteSourceId\":").append(sourceAccountId)
          .append(",\"compteDestinationId\":").append(destinationAccountId)
          .append(",\"montant\":").append(amount)
          .append(",\"description\":\"").append(description != null ? description : "Transfer via interface")
          .append("\"");

      if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
        requestBodyBuilder.append(",\"actionDateTime\":\"").append(actionDateTime).append("\"");
      }

      requestBodyBuilder.append("}");
      String requestBody = requestBodyBuilder.toString();

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      LOG.info(
          "Making transfer from account " + sourceAccountId + " to " + destinationAccountId + " amount: " + amount);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        LOG.info("Transfer successful");
        return null; // Success
      } else {
        String errorMessage = extractErrorMessage(response.body());
        LOG.warning("Failed to make transfer. Status: " + response.statusCode() + ", Body: " + response.body());
        return errorMessage;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error making transfer from " + sourceAccountId + " to " + destinationAccountId, e);
      return "Erreur de communication avec le service bancaire: " + e.getMessage();
    }
  }

  @Override
  public List<TransactionCourantDTO> getTransactionHistory(Integer accountId) {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(BANKING_COURANT_BASE_URL + "/transactions/compte/" + accountId))
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        TransactionCourantDTO[] transactionArray = objectMapper
            .readValue(response.body(), TransactionCourantDTO[].class);
        return Arrays.asList(transactionArray);
      } else {
        LOG.warning("Failed to fetch transaction history. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return new ArrayList<>();
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching transaction history for account " + accountId, e);
      return new ArrayList<>();
    }
  }

  @Override
  public mg.razherana.banking.interfaces.dto.AccountStatusDTO getAccountStatus(Integer accountId,
      java.time.LocalDateTime statusDate) {
    try {
      // Get account details
      CompteCourantDTO account = getAccountById(accountId);
      if (account == null) {
        return null;
      }

      // Get transaction history
      List<TransactionCourantDTO> transactions = getTransactionHistory(accountId);

      // Filter transactions up to the status date
      List<TransactionCourantDTO> filteredTransactions = transactions.stream()
          .filter(t -> t.getDate() == null || !t.getDate().isAfter(statusDate))
          .collect(java.util.stream.Collectors.toList());

      // Calculate status information
      mg.razherana.banking.interfaces.dto.AccountStatusDTO status = new mg.razherana.banking.interfaces.dto.AccountStatusDTO();
      status.setStatusDate(statusDate);
      status.setTotalTransactions(filteredTransactions.size());

      java.math.BigDecimal balance = java.math.BigDecimal.ZERO;
      java.math.BigDecimal totalDeposits = java.math.BigDecimal.ZERO;
      java.math.BigDecimal totalWithdrawals = java.math.BigDecimal.ZERO;
      java.math.BigDecimal totalTransfersSent = java.math.BigDecimal.ZERO;
      java.math.BigDecimal totalTransfersReceived = java.math.BigDecimal.ZERO;
      java.math.BigDecimal totalTaxPayments = java.math.BigDecimal.ZERO;

      for (TransactionCourantDTO transaction : filteredTransactions) {
        java.math.BigDecimal amount = transaction.getMontant() != null ? transaction.getMontant()
            : java.math.BigDecimal.ZERO;

        if ("taxe".equals(transaction.getSpecialAction())) {
          // Tax payment
          totalTaxPayments = totalTaxPayments.add(amount);
          balance = balance.subtract(amount); // Tax reduces balance
        } else if (transaction.getSenderId() == null && accountId.equals(transaction.getReceiverId())) {
          // Deposit (money coming in)
          totalDeposits = totalDeposits.add(amount);
          balance = balance.add(amount);
        } else if (accountId.equals(transaction.getSenderId()) && transaction.getReceiverId() == null) {
          // Withdrawal (money going out)
          totalWithdrawals = totalWithdrawals.add(amount);
          balance = balance.subtract(amount);
        } else if (accountId.equals(transaction.getSenderId()) && transaction.getReceiverId() != null) {
          // Transfer sent (money going out)
          totalTransfersSent = totalTransfersSent.add(amount);
          balance = balance.subtract(amount);
        } else if (accountId.equals(transaction.getReceiverId()) && transaction.getSenderId() != null) {
          // Transfer received (money coming in)
          totalTransfersReceived = totalTransfersReceived.add(amount);
          balance = balance.add(amount);
        }
      }

      status.setBalance(balance);
      status.setTotalDeposits(totalDeposits);
      status.setTotalWithdrawals(totalWithdrawals);
      status.setTotalTransfersSent(totalTransfersSent);
      status.setTotalTransfersReceived(totalTransfersReceived);
      status.setTotalTaxPayments(totalTaxPayments);

      // Calculate tax information
      // For now, set basic tax info - this could be enhanced with more complex tax
      // calculation logic
      status.setTaxPaid(totalTaxPayments);

      // Get current tax to pay (this gives us the current outstanding tax)
      Double currentTaxToPay = getTaxToPay(accountId);
      status.setTaxToPay(
          currentTaxToPay != null ? java.math.BigDecimal.valueOf(currentTaxToPay) : java.math.BigDecimal.ZERO);

      return status;

    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error calculating account status for account " + accountId, e);
      return null;
    }
  }
}