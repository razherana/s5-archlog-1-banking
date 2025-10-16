package mg.razherana.banking.interfaces.application.comptePretServices;

import mg.razherana.banking.interfaces.dto.comptePret.*;
import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
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
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/types";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      LOG.info("Fetching loan types from: " + url);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        TypeComptePretDTO[] types = objectMapper.readValue(
            response.body(), TypeComptePretDTO[].class);
        return Arrays.asList(types);
      } else {
        LOG.warning("Failed to fetch loan types. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return new ArrayList<>();
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching loan types", e);
      return new ArrayList<>();
    }
  }

  @Override
  public List<ComptePretDTO> getLoansByUserId(Integer userId) {
    try {
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/user/" + userId;

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      LOG.info("Fetching loans for user " + userId + " from: " + url);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        ComptePretDTO[] loans = objectMapper.readValue(
            response.body(), ComptePretDTO[].class);
        return Arrays.asList(loans);
      } else {
        LOG.warning("Failed to fetch loans. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return new ArrayList<>();
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching loans for user " + userId, e);
      return new ArrayList<>();
    }
  }

  @Override
  public ComptePretDTO getLoanById(Integer loanId) {
    try {
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/" + loanId;

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      LOG.info("Fetching loan " + loanId + " from: " + url);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), ComptePretDTO.class);
      } else {
        LOG.warning("Failed to fetch loan. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching loan " + loanId, e);
      return null;
    }
  }

  @Override
  public ComptePretDTO createLoan(CreateComptePretRequest request) {
    try {
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

      LOG.info("Creating loan at: " + url);
      LOG.info("Request body: " + requestBody);

      HttpResponse<String> response = httpClient.send(httpRequest,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        ComptePretDTO createdLoan = objectMapper.readValue(response.body(), ComptePretDTO.class);
        LOG.info("Loan created successfully with ID: " + createdLoan.getId());

        // Now deposit the loan amount to the specified current account
        if (request.getCompteCourantId() != null) {
          String description = "Prêt #" + createdLoan.getId() + " - Versement du montant emprunté";
          String depositResult = compteCourantService.makeDeposit(
              request.getCompteCourantId(),
              request.getMontant().doubleValue(),
              description,
              null // Use current time
          );

          if (depositResult != null) {
            LOG.warning("Failed to deposit loan amount to current account: " + depositResult);
            // Note: We could decide whether to rollback the loan creation here
            // For now, we'll log the error but return the created loan
          } else {
            LOG.info("Loan amount deposited successfully to current account " + request.getCompteCourantId());
          }
        }

        return createdLoan;
      } else {
        LOG.warning("Failed to create loan. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error creating loan", e);
      return null;
    }
  }

  @Override
  public EcheanceDTO makePayment(MakePaymentRequest request) {
    try {
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/make-payment";

      String requestBody = objectMapper.writeValueAsString(request);

      HttpRequest httpRequest = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      LOG.info("Making payment at: " + url);

      HttpResponse<String> response = httpClient.send(httpRequest,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        return objectMapper.readValue(response.body(), EcheanceDTO.class);
      } else if (response.statusCode() != 500) {
        LOG.warning("Failed to make payment. Status: " + response.statusCode() +
            ", Body: " + response.body());

        String errorMessage = objectMapper.readTree(response.body()).get("message").asText();

        throw new IllegalStateException(errorMessage);
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error making payment", e);
      throw new IllegalStateException(e.getMessage());
    }

    return null;
  }

  @Override
  public PaymentStatusDTO getPaymentStatus(Integer loanId) {
    try {
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/" + loanId + "/payment-status";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      LOG.info("Fetching payment status for loan " + loanId + " from: " + url);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), PaymentStatusDTO.class);
      } else {
        LOG.warning("Failed to fetch payment status. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching payment status for loan " + loanId, e);
      return null;
    }
  }

  @Override
  public PaymentStatusDTO getPaymentStatus(Integer loanId, LocalDateTime actionDateTime) {
    try {
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/" + loanId + "/payment-status";
      if (actionDateTime != null) {
        url += "?actionDateTime=" + actionDateTime.toString();
      }

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      LOG.info("Fetching payment status for loan " + loanId + " at " + actionDateTime + " from: " + url);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), PaymentStatusDTO.class);
      } else {
        LOG.warning("Failed to fetch payment status. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching payment status for loan " + loanId + " at " + actionDateTime, e);
      return null;
    }
  }

  @Override
  public List<EcheanceDTO> getPaymentHistory(Integer loanId) {
    try {
      String url = BANKING_PRET_BASE_URL + "/comptes-pret/" + loanId + "/payment-history";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      LOG.info("Fetching payment history for loan " + loanId + " from: " + url);

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        EcheanceDTO[] payments = objectMapper.readValue(
            response.body(), EcheanceDTO[].class);
        return Arrays.asList(payments);
      } else {
        LOG.warning("Failed to fetch payment history. Status: " + response.statusCode() +
            ", Body: " + response.body());
        return new ArrayList<>();
      }

    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching payment history for loan " + loanId, e);
      return new ArrayList<>();
    }
  }
}