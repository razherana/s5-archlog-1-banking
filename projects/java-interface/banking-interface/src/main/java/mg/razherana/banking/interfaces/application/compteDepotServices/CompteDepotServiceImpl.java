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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), new TypeReference<List<CompteDepotDTO>>() {
        });
      } else {
        LOG.warning("Failed to get accounts for user " + userId + ": " + response.statusCode());
        return List.of();
      }
    } catch (Exception e) {
      LOG.severe("Error getting accounts for user " + userId + ": " + e.getMessage());
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

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), CompteDepotDTO.class);
      } else {
        LOG.warning("Failed to get account " + accountId + ": " + response.statusCode());
        return null;
      }
    } catch (Exception e) {
      LOG.severe("Error getting account " + accountId + ": " + e.getMessage());
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

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        return objectMapper.readValue(response.body(), CompteDepotDTO.class);
      } else {
        LOG.warning("Failed to create account: " + response.statusCode() + " - " + response.body());
        return null;
      }
    } catch (Exception e) {
      LOG.severe("Error creating account: " + e.getMessage());
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

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        String withdrawalMessage = "Retrait effectué avec succès. Détails: " + response.body();
        
        // If targetAccountId is provided, make a deposit to that current account
        if (targetAccountId != null) {
          // First get the deposit account details to know the withdrawn amount
          CompteDepotDTO account = getAccountById(accountId);
          if (account != null && account.getMontantTotal() != null) {
            String depositResult = compteCourantService.makeDeposit(
                targetAccountId, 
                account.getMontantTotal().doubleValue(), 
                "Dépôt provenant du retrait du compte dépôt #" + accountId, 
                actionDateTime
            );
            
            if (depositResult == null) {
              withdrawalMessage += " Dépôt automatique effectué sur le compte courant #" + targetAccountId;
            } else {
              withdrawalMessage += " ATTENTION: Échec du dépôt automatique: " + depositResult;
            }
          }
        }
        
        return withdrawalMessage;
      } else {
        return extractErrorMessage(response);
      }
    } catch (Exception e) {
      LOG.severe("Error withdrawing from account " + accountId + ": " + e.getMessage());
      return "Erreur lors du retrait: " + e.getMessage();
    }
  }

  @Override
  public List<TypeCompteDepotDTO> getAllDepositTypes() {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(TYPES_ENDPOINT))
          .header("Content-Type", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), new TypeReference<List<TypeCompteDepotDTO>>() {
        });
      } else {
        LOG.warning("Failed to get deposit types: " + response.statusCode());
        return List.of();
      }
    } catch (Exception e) {
      LOG.severe("Error getting deposit types: " + e.getMessage());
      return List.of();
    }
  }

  @Override
  public TypeCompteDepotDTO getDepositTypeById(Integer typeId) {
    try {
      String url = TYPES_ENDPOINT + "/" + typeId;
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), TypeCompteDepotDTO.class);
      } else {
        LOG.warning("Failed to get deposit type " + typeId + ": " + response.statusCode());
        return null;
      }
    } catch (Exception e) {
      LOG.severe("Error getting deposit type " + typeId + ": " + e.getMessage());
      return null;
    }
  }

  @Override
  public List<CompteDepotDTO> getAllAccounts() {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(COMPTES_ENDPOINT))
          .header("Content-Type", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return objectMapper.readValue(response.body(), new TypeReference<List<CompteDepotDTO>>() {
        });
      } else {
        LOG.warning("Failed to get all accounts: " + response.statusCode());
        return List.of();
      }
    } catch (Exception e) {
      LOG.severe("Error getting all accounts: " + e.getMessage());
      return List.of();
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