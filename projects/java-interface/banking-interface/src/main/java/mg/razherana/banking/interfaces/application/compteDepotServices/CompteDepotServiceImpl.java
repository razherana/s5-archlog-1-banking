package mg.razherana.banking.interfaces.application.compteDepotServices;

import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.dto.CompteDepotDTO;
import mg.razherana.banking.interfaces.dto.TypeCompteDepotDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Implementation of CompteDepotService that communicates with the .NET
 * banking-depot service.
 * Handles HTTP communication and JSON serialization/deserialization.
 */
@Stateless
public class CompteDepotServiceImpl implements CompteDepotService {

  private static final Logger LOG = Logger.getLogger(CompteDepotServiceImpl.class.getName());
  private static final String BASE_URL = "http://127.0.0.4:8080/api";
  private static final String COMPTES_ENDPOINT = BASE_URL + "/ComptesDepots";
  private static final String TYPES_ENDPOINT = BASE_URL + "/TypeComptesDepots";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  @EJB
  private CompteCourantService compteCourantService;

  public CompteDepotServiceImpl() {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  @Override
  public List<CompteDepotDTO> getAccountsByUserId(Integer userId) {
    try {
      String url = COMPTES_ENDPOINT + "/user/" + userId;
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .GET()
          .build();

      LOG.info("Fetching deposit accounts for user: " + userId);
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        List<CompteDepotDTO> accounts = objectMapper.readValue(response.body(), new TypeReference<List<CompteDepotDTO>>() {});
        LOG.info("Successfully fetched " + accounts.size() + " deposit accounts for user: " + userId);
        return accounts;
      } else {
        LOG.warning("Failed to get accounts for user " + userId + ": " + response.statusCode() + " - " + response.body());
        return List.of();
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting accounts for user " + userId + ": " + e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public CompteDepotDTO getAccountById(Integer accountId) {
    try {
      String url = COMPTES_ENDPOINT + "/" + accountId;
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .GET()
          .build();

      LOG.info("Fetching deposit account: " + accountId);
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        CompteDepotDTO account = objectMapper.readValue(response.body(), CompteDepotDTO.class);
        LOG.info("Successfully fetched deposit account: " + accountId);
        return account;
      } else {
        LOG.warning("Failed to get account " + accountId + ": " + response.statusCode() + " - " + response.body());
        return null;
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting account " + accountId + ": " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public CompteDepotDTO createAccount(Integer typeCompteDepotId, Integer userId, String dateEcheance,
      BigDecimal montant, String actionDateTime) {
    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("typeCompteDepotId", typeCompteDepotId);
      requestBody.put("userId", userId);
      requestBody.put("dateEcheance", dateEcheance);
      requestBody.put("montant", montant);
      if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
        requestBody.put("actionDateTime", actionDateTime);
      }

      String jsonBody = objectMapper.writeValueAsString(requestBody);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(COMPTES_ENDPOINT))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
          .build();

      LOG.info("Creating deposit account for user " + userId + " with type " + typeCompteDepotId + " and amount " + montant);
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        CompteDepotDTO account = objectMapper.readValue(response.body(), CompteDepotDTO.class);
        LOG.info("Successfully created deposit account with ID: " + account.getId());
        return account;
      } else {
        LOG.warning("Failed to create account: " + response.statusCode() + " - " + response.body());
        return null;
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error creating account: " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public String withdrawFromAccount(Integer accountId, Integer targetAccountId, String actionDateTime) {
    try {
      Map<String, Object> requestBody = new HashMap<>();
      if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
        requestBody.put("actionDateTime", actionDateTime);
      }

      String jsonBody = objectMapper.writeValueAsString(requestBody);
      String url = COMPTES_ENDPOINT + "/" + accountId + "/withdraw";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
          .build();

      LOG.info("Processing withdrawal for deposit account: " + accountId + 
               (targetAccountId != null ? " with transfer to current account: " + targetAccountId : ""));
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        String withdrawalMessage = "Retrait effectué avec succès. Détails: " + response.body();
        
        // If targetAccountId is provided, make a deposit to that current account
        if (targetAccountId != null) {
          // First get the deposit account details to know the withdrawn amount
          CompteDepotDTO account = getAccountById(accountId);
          if (account != null && account.getMontantTotal() != null) {
            LocalDateTime actionDateTimeParsed = LocalDateTime.now();
            if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
              try {
                actionDateTimeParsed = LocalDateTime.parse(actionDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
              } catch (Exception e) {
                LOG.log(Level.WARNING, "Invalid actionDateTime format, using current time: " + e.getMessage());
              }
            }
            
            try {
              var depositResult = compteCourantService.makeDeposit(
                  targetAccountId, 
                  account.getMontantTotal(), 
                  "Dépôt provenant du retrait du compte dépôt #" + accountId, 
                  actionDateTimeParsed
              );
              
              if (depositResult != null) {
                withdrawalMessage += " Dépôt automatique effectué sur le compte courant #" + targetAccountId;
              } else {
                withdrawalMessage += " ATTENTION: Échec du dépôt automatique sur le compte courant #" + targetAccountId;
              }
            } catch (Exception e) {
              LOG.log(Level.SEVERE, "Error making automatic deposit: " + e.getMessage(), e);
              withdrawalMessage += " ATTENTION: Erreur lors du dépôt automatique: " + e.getMessage();
            }
          }
        }
        
        return withdrawalMessage;
      } else {
        return extractErrorMessage(response);
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error withdrawing from account " + accountId + ": " + e.getMessage(), e);
      return "Erreur lors du retrait: " + e.getMessage();
    }
  }

  @Override
  public List<TypeCompteDepotDTO> getAllDepositTypes() {
    try {
      LOG.info("Fetching all deposit types from service");
      
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(BASE_URL + "/type-comptes-depots"))
              .header("Content-Type", "application/json")
              .GET()
              .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      
      if (response.statusCode() == 200) {
        List<TypeCompteDepotDTO> types = objectMapper.readValue(response.body(), 
                  objectMapper.getTypeFactory().constructCollectionType(List.class, TypeCompteDepotDTO.class));
        LOG.info("Successfully retrieved " + types.size() + " deposit types");
        return types;
      } else {
        LOG.warning("Failed to retrieve deposit types. Status: " + response.statusCode() + ", Body: " + response.body());
        return new ArrayList<>();
      }
    } catch (IOException | InterruptedException e) {
      LOG.log(Level.SEVERE, "Error fetching all deposit types: " + e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  @Override
  public TypeCompteDepotDTO getDepositTypeById(Integer typeId) {
    try {
      LOG.info("Fetching deposit type with ID: " + typeId);
      
      String url = TYPES_ENDPOINT + "/" + typeId;
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        TypeCompteDepotDTO type = objectMapper.readValue(response.body(), TypeCompteDepotDTO.class);
        LOG.info("Successfully retrieved deposit type: " + type.getNom());
        return type;
      } else if (response.statusCode() == 404) {
        LOG.warning("Deposit type with ID " + typeId + " not found");
        return null;
      } else {
        LOG.warning("Failed to get deposit type " + typeId + ". Status: " + response.statusCode() + ", Body: " + response.body());
        return null;
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting deposit type " + typeId + ": " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public List<CompteDepotDTO> getAllAccounts() {
    try {
      LOG.info("Fetching all deposit accounts from service");
      
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(COMPTES_ENDPOINT))
          .header("Content-Type", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        List<CompteDepotDTO> accounts = objectMapper.readValue(response.body(), new TypeReference<List<CompteDepotDTO>>() {
        });
        LOG.info("Successfully retrieved " + accounts.size() + " deposit accounts");
        return accounts;
      } else {
        LOG.warning("Failed to get all accounts. Status: " + response.statusCode() + ", Body: " + response.body());
        return new ArrayList<>();
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error getting all accounts: " + e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  /**
   * Extracts error message from API response for user display.
   */
  private String extractErrorMessage(HttpResponse<String> response) {
    try {
      Map<String, Object> errorResponse = objectMapper.readValue(response.body(),
          new TypeReference<Map<String, Object>>() {
          });

      String message = (String) errorResponse.get("message");
      if (message != null && !message.trim().isEmpty()) {
        return URLEncoder.encode(message, StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      LOG.warning("Could not parse error response: " + e.getMessage());
    }

    return URLEncoder.encode("Erreur lors de l'opération", StandardCharsets.UTF_8);
  }
}