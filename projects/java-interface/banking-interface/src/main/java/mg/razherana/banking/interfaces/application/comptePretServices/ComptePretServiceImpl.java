package mg.razherana.banking.interfaces.application.comptePretServices;

import mg.razherana.banking.interfaces.dto.comptePret.*;
import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.courant.entities.TransactionCourant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.HashMap;
import java.util.Map;

/**
 * Service implementation for communicating with the banking-pret service.
 */
@Stateless
public class ComptePretServiceImpl implements ComptePretService {

  private static final Logger LOG = Logger.getLogger(ComptePretServiceImpl.class.getName());
  private static final String BANKING_PRET_BASE_URL = "http://127.0.0.3:8080/api";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  @EJB
  private CompteCourantService compteCourantService;

  public ComptePretServiceImpl() {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Override
  public List<TypeComptePretDTO> getAllLoanTypes() {
    try {
      LOG.info("Fetching all loan types from banking-pret service");
      
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/types";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        TypeComptePretDTO[] types = objectMapper.readValue(
            response.body(), TypeComptePretDTO[].class);
        LOG.info("Successfully retrieved " + types.length + " loan types");
        return Arrays.asList(types);
      } else {
        LOG.warning("Failed to fetch loan types. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return new ArrayList<>();
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching loan types: " + e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  @Override
  public List<ComptePretDTO> getLoansByUserId(Integer userId) {
    try {
      LOG.info("Fetching loans for user ID: " + userId);
      
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/user/" + userId;

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        ComptePretDTO[] loans = objectMapper.readValue(
            response.body(), ComptePretDTO[].class);
        LOG.info("Successfully retrieved " + loans.length + " loans for user " + userId);
        return Arrays.asList(loans);
      } else if (response.statusCode() == 404) {
        LOG.info("No loans found for user " + userId);
        return new ArrayList<>();
      } else {
        LOG.warning("Failed to fetch loans for user " + userId + ". Status: " + response.statusCode() +
            ", Body: " + response.body());
        return new ArrayList<>();
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching loans for user " + userId + ": " + e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  @Override
  public ComptePretDTO getLoanById(Integer loanId) {
    try {
      LOG.info("Fetching loan with ID: " + loanId);
      
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/" + loanId;

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        ComptePretDTO loan = objectMapper.readValue(response.body(), ComptePretDTO.class);
        LOG.info("Successfully retrieved loan " + loanId);
        return loan;
      } else if (response.statusCode() == 404) {
        LOG.warning("Loan with ID " + loanId + " not found");
        return null;
      } else {
        LOG.warning("Failed to fetch loan " + loanId + ". Status: " + response.statusCode() +
            ", Body: " + response.body());
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching loan " + loanId + ": " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public ComptePretDTO createLoan(CreateComptePretRequest request) {
    try {
      LOG.info("Creating loan for user " + request.getUserId() + " with amount " + request.getMontant());
      
      // First, create the loan via banking-pret service
      String url = BANKING_PRET_BASE_URL + "/comptes-pret";

      // Create the loan request body (without compteCourantId which is for our logic
      // only)
      Map<String, Object> loanRequest = new HashMap<>();
      loanRequest.put("userId", request.getUserId());
      loanRequest.put("typeComptePretId", request.getTypeComptePretId());
      loanRequest.put("montant", request.getMontant());
      loanRequest.put("dateDebut", request.getDateDebut());
      loanRequest.put("dateFin", request.getDateFin());

      String requestBody = objectMapper.writeValueAsString(loanRequest);

      HttpRequest httpRequest = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      HttpResponse<String> response = httpClient.send(httpRequest,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        ComptePretDTO createdLoan = objectMapper.readValue(response.body(), ComptePretDTO.class);
        LOG.info("Loan created successfully with ID: " + createdLoan.getId());

        // Now deposit the loan amount to the specified current account
        if (request.getCompteCourantId() != null) {
          try {
            String description = "Prêt #" + createdLoan.getId() + " - Versement du montant emprunté";
            TransactionCourant depositTransaction = compteCourantService.makeDeposit(
                request.getCompteCourantId(),
                request.getMontant(),
                description,
                null // Use current time
            );

            if (depositTransaction != null) {
              LOG.info("Loan amount deposited successfully to current account " + request.getCompteCourantId() + 
                      ", transaction ID: " + depositTransaction.getId());
            } else {
              LOG.warning("Failed to deposit loan amount to current account - transaction returned null");
              // Note: We could decide whether to rollback the loan creation here
              // For now, we'll log the error but return the created loan
            }
          } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error depositing loan amount to current account: " + e.getMessage(), e);
            // Note: We could decide whether to rollback the loan creation here
            // For now, we'll log the error but return the created loan
          }
        }

        return createdLoan;
      } else {
        LOG.warning("Failed to create loan. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error creating loan: " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public EcheanceDTO makePayment(MakePaymentRequest request) {
    try {
      LOG.info("Making payment for loan " + request.getCompteId() + " with amount " + request.getMontant());
      
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/make-payment";

      String requestBody = objectMapper.writeValueAsString(request);

      HttpRequest httpRequest = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      HttpResponse<String> response = httpClient.send(httpRequest,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        EcheanceDTO payment = objectMapper.readValue(response.body(), EcheanceDTO.class);
        LOG.info("Payment processed successfully for loan " + request.getCompteId());
        return payment;
      } else if (response.statusCode() != 500) {
        LOG.warning("Failed to make payment. Status: " + response.statusCode() +
            ", Body: " + response.body());

        String errorMessage = objectMapper.readTree(response.body()).get("message").asText();
        throw new IllegalStateException(errorMessage);
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error making payment for loan " + request.getCompteId() + ": " + e.getMessage(), e);
      throw new IllegalStateException(e.getMessage());
    }

    return null;
  }

  @Override
  public PaymentStatusDTO getPaymentStatus(Integer loanId) {
    try {
      LOG.info("Fetching payment status for loan ID: " + loanId);
      
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/" + loanId + "/payment-status";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        PaymentStatusDTO status = objectMapper.readValue(response.body(), PaymentStatusDTO.class);
        LOG.info("Successfully retrieved payment status for loan " + loanId);
        return status;
      } else if (response.statusCode() == 404) {
        LOG.warning("Payment status not found for loan " + loanId);
        return null;
      } else {
        LOG.warning("Failed to fetch payment status for loan " + loanId + ". Status: " + response.statusCode() +
            ", Body: " + response.body());
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching payment status for loan " + loanId + ": " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public PaymentStatusDTO getPaymentStatus(Integer loanId, LocalDateTime actionDateTime) {
    try {
      LOG.info("Fetching payment status for loan " + loanId + " at date: " + actionDateTime);
      
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/" + loanId + "/payment-status";
      if (actionDateTime != null) {
        url += "?actionDateTime=" + actionDateTime.toString();
      }

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        PaymentStatusDTO status = objectMapper.readValue(response.body(), PaymentStatusDTO.class);
        LOG.info("Successfully retrieved payment status for loan " + loanId + " at " + actionDateTime);
        return status;
      } else if (response.statusCode() == 404) {
        LOG.warning("Payment status not found for loan " + loanId + " at " + actionDateTime);
        return null;
      } else {
        LOG.warning("Failed to fetch payment status for loan " + loanId + " at " + actionDateTime + 
                   ". Status: " + response.statusCode() + ", Body: " + response.body());
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching payment status for loan " + loanId + " at " + actionDateTime + 
             ": " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public List<EcheanceDTO> getPaymentHistory(Integer loanId) {
    try {
      LOG.info("Fetching payment history for loan ID: " + loanId);
      
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/" + loanId + "/payment-history";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        EcheanceDTO[] payments = objectMapper.readValue(
            response.body(), EcheanceDTO[].class);
        LOG.info("Successfully retrieved " + payments.length + " payment records for loan " + loanId);
        return Arrays.asList(payments);
      } else if (response.statusCode() == 404) {
        LOG.info("No payment history found for loan " + loanId);
        return new ArrayList<>();
      } else {
        LOG.warning("Failed to fetch payment history for loan " + loanId + ". Status: " + response.statusCode() +
            ", Body: " + response.body());
        return new ArrayList<>();
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching payment history for loan " + loanId + ": " + e.getMessage(), e);
      return new ArrayList<>();
    }
  }
}