package mg.razherana.banking.courant.api;

import jakarta.ejb.EJB;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mg.razherana.banking.common.utils.ExceptionUtils;
import mg.razherana.banking.courant.application.compteCourantService.CompteCourantService;
import mg.razherana.banking.courant.application.transactionService.TransactionService;
import mg.razherana.banking.courant.dto.ErrorDTO;
import mg.razherana.banking.courant.dto.MessageDTO;
import mg.razherana.banking.courant.dto.TransactionCourantDTO;
import mg.razherana.banking.courant.dto.requests.transactions.DepotRequest;
import mg.razherana.banking.courant.dto.requests.transactions.RetraitRequest;
import mg.razherana.banking.courant.dto.requests.transactions.TransfertRequest;
import mg.razherana.banking.courant.dto.requests.transactions.PayTaxRequest;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {
  private static final Logger LOG = Logger.getLogger(TransactionResource.class.getName());

  @EJB
  private TransactionService transactionService;

  @EJB
  private CompteCourantService compteCourantService;

  /**
   * Helper method to handle EJBException and extract the underlying cause.
   * Returns true if the exception should be treated as a 400 Bad Request,
   * false if it should be treated as a 500 Internal Server Error.
   */
  private boolean isClientError(Exception exc) {
    return exc instanceof IllegalArgumentException
        || exc instanceof IllegalStateException
        || exc instanceof ConstraintViolationException;
  }

  /**
   * Helper method to extract error message from EJBException.
   */
  private String getErrorMessage(Exception ex) {
    if (isClientError(ex)) {
      return "Invalid data: " + ex.getMessage();
    } else {
      return "Internal server error";
    }
  }

  @GET
  public Response getAllTransactions() {
    try {
      List<TransactionCourant> transactions = transactionService.getAllTransactions();
      List<TransactionCourantDTO> transactionDTOs = transactions.stream()
          .map(TransactionCourantDTO::new)
          .collect(Collectors.toList());
      return Response.ok(transactionDTOs)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      e = ExceptionUtils.root(e);
      int statusCode = isClientError(e) ? 400 : 500;
      String statusText = isClientError(e) ? "Bad Request" : "Internal Server Error";
      String errorMessage = getErrorMessage(e);

      if (isClientError(e)) {
        LOG.warning("Invalid data from EJB: " + errorMessage);
      } else {
        LOG.severe("EJB error getting all transactions: " + e.getMessage());
      }

      ErrorDTO error = new ErrorDTO(errorMessage, statusCode, statusText, "/transactions");
      return Response.status(statusCode)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @GET
  @Path("/{id}")
  public Response getTransactionById(@PathParam("id") Integer id) {
    try {
      TransactionCourant transaction = transactionService.findById(id);
      if (transaction == null) {
        ErrorDTO error = new ErrorDTO("Transaction not found", 404, "Not Found", "/transactions/" + id);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
      TransactionCourantDTO transactionDTO = new TransactionCourantDTO(transaction);
      return Response.ok(transactionDTO)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      e = ExceptionUtils.root(e);
      int statusCode = isClientError(e) ? 400 : 500;
      String statusText = isClientError(e) ? "Bad Request" : "Internal Server Error";
      String errorMessage = getErrorMessage(e);

      if (isClientError(e)) {
        LOG.warning("Invalid data from EJB: " + errorMessage);
      } else {
        LOG.severe("EJB error getting transaction by ID: " + e.getMessage());
      }

      ErrorDTO error = new ErrorDTO(errorMessage, statusCode, statusText, "/transactions/" + id);
      return Response.status(statusCode)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @GET
  @Path("/compte/{compteId}")
  public Response getTransactionsByCompte(@PathParam("compteId") Integer compteId) {
    try {
      CompteCourant compte = compteCourantService.findById(compteId);
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/transactions/compte/" + compteId);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      List<TransactionCourant> transactions = transactionService.getTransactionsByCompte(compte);
      List<TransactionCourantDTO> transactionDTOs = transactions.stream()
          .map(TransactionCourantDTO::new)
          .collect(Collectors.toList());
      return Response.ok(transactionDTOs)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      e = ExceptionUtils.root(e);
      int statusCode = isClientError(e) ? 400 : 500;
      String statusText = isClientError(e) ? "Bad Request" : "Internal Server Error";
      String errorMessage = getErrorMessage(e);

      if (isClientError(e)) {
        LOG.warning("Invalid data from EJB: " + errorMessage);
      } else {
        LOG.severe("EJB error getting transactions by compte: " + e.getMessage());
      }

      ErrorDTO error = new ErrorDTO(errorMessage, statusCode, statusText, "/transactions/compte/" + compteId);
      return Response.status(statusCode)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @POST
  @Path("/depot")
  public Response depot(DepotRequest request) {
    try {
      if (request.getCompteId() == null || request.getMontant() == null) {
        ErrorDTO error = new ErrorDTO("Compte ID and montant are required", 400, "Bad Request", "/transactions/depot");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      CompteCourant compte = compteCourantService.findById(request.getCompteId());
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/transactions/depot");
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      TransactionCourant transaction = transactionService.depot(
          compte, request.getMontant(), request.getDescription(), request.getActionDateTime(), request.getDevise());

      TransactionCourantDTO transactionDTO = new TransactionCourantDTO(transaction);
      return Response.status(Response.Status.CREATED)
          .type(MediaType.APPLICATION_JSON)
          .entity(transactionDTO).build();
    } catch (Exception e) {
      e = ExceptionUtils.root(e);

      int statusCode = isClientError(e) ? 400 : 500;
      String statusText = isClientError(e) ? "Bad Request" : "Internal Server Error";
      String errorMessage = getErrorMessage(e);

      if (isClientError(e)) {
        LOG.warning("Invalid depot data from EJB: " + e.getMessage());
      } else {
        LOG.severe("EJB error processing depot: " + e.getMessage());
      }

      ErrorDTO error = new ErrorDTO(errorMessage, statusCode, statusText, "/transactions/depot");
      return Response.status(statusCode)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @POST
  @Path("/retrait")
  public Response retrait(RetraitRequest request) {
    try {
      if (request.getCompteId() == null || request.getMontant() == null) {
        ErrorDTO error = new ErrorDTO("Compte ID and montant are required", 400, "Bad Request",
            "/transactions/retrait");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      CompteCourant compte = compteCourantService.findById(request.getCompteId());
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/transactions/retrait");
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      // Default to current time if actionDateTime is not provided
      LocalDateTime actionDateTime = request.getActionDateTime() != null ? request.getActionDateTime()
          : LocalDateTime.now();

      TransactionCourant transaction = transactionService.retrait(
          compte, request.getMontant(), request.getDescription(), actionDateTime, request.getDevise());

      TransactionCourantDTO transactionDTO = new TransactionCourantDTO(transaction);
      return Response.status(Response.Status.CREATED)
          .type(MediaType.APPLICATION_JSON)
          .entity(transactionDTO).build();
    } catch (Exception e) {
      e = ExceptionUtils.root(e);

      int statusCode = isClientError(e) ? 400 : 500;
      String statusText = isClientError(e) ? "Bad Request" : "Internal Server Error";
      String errorMessage = getErrorMessage(e);

      if (isClientError(e)) {
        LOG.warning("Invalid retrait data from EJB: " + e.getMessage());
      } else {
        LOG.severe("EJB error processing retrait: " + e.getMessage());
      }

      ErrorDTO error = new ErrorDTO(errorMessage, statusCode, statusText, "/transactions/retrait");
      return Response.status(statusCode)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @POST
  @Path("/transfert")
  public Response transfert(TransfertRequest request) {
    try {
      if (request.getCompteSourceId() == null ||
          request.getCompteDestinationId() == null ||
          request.getMontant() == null) {
        ErrorDTO error = new ErrorDTO("Source account ID, destination account ID, and montant are required", 400,
            "Bad Request", "/transactions/transfert");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      CompteCourant compteSource = compteCourantService.findById(request.getCompteSourceId());
      if (compteSource == null) {
        ErrorDTO error = new ErrorDTO("Source compte not found", 404, "Not Found", "/transactions/transfert");
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      CompteCourant compteDestination = compteCourantService.findById(request.getCompteDestinationId());
      if (compteDestination == null) {
        ErrorDTO error = new ErrorDTO("Destination compte not found", 404, "Not Found", "/transactions/transfert");
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      // Default to current time if actionDateTime is not provided
      LocalDateTime actionDateTime = request.getActionDateTime() != null ? request.getActionDateTime()
          : LocalDateTime.now();

      transactionService.transfert(compteSource, compteDestination,
          request.getMontant(), request.getDescription(), actionDateTime, request.getDevise());

      MessageDTO message = new MessageDTO("Transfer completed successfully");
      return Response.status(Response.Status.CREATED)
          .type(MediaType.APPLICATION_JSON)
          .entity(message).build();
    } catch (Exception e) {
      e = ExceptionUtils.root(e);
      int statusCode = isClientError(e) ? 400 : 500;
      String statusText = isClientError(e) ? "Bad Request" : "Internal Server Error";
      String errorMessage = getErrorMessage(e);

      LOG.severe("EJB error processing transfert: " + e.getMessage());

      ErrorDTO error = new ErrorDTO(errorMessage, statusCode, statusText, "/transactions/transfert");
      return Response.status(statusCode)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @POST
  @Path("/pay-tax")
  public Response payTax(PayTaxRequest request) {
    try {
      if (request.getCompteId() == null) {
        ErrorDTO error = new ErrorDTO("Compte ID is required", 400, "Bad Request", "/transactions/pay-tax");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      CompteCourant compte = compteCourantService.findById(request.getCompteId());
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/transactions/pay-tax");
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      // Default to current time if actionDateTime is not provided
      LocalDateTime actionDateTime = request.getActionDateTime() != null ? request.getActionDateTime()
          : LocalDateTime.now();

      TransactionCourant transaction = transactionService.payTax(
          compte, request.getDescription(), actionDateTime, request.getDevise());

      if (transaction == null) {
        MessageDTO message = new MessageDTO("No tax to pay");
        return Response.ok(message)
            .type(MediaType.APPLICATION_JSON)
            .build();
      }

      TransactionCourantDTO transactionDTO = new TransactionCourantDTO(transaction);
      return Response.status(Response.Status.CREATED)
          .type(MediaType.APPLICATION_JSON)
          .entity(transactionDTO).build();
    } catch (Exception e) {
      e = ExceptionUtils.root(e);

      int statusCode = isClientError(e) ? 400 : 500;
      String statusText = isClientError(e) ? "Bad Request" : "Internal Server Error";
      String errorMessage = getErrorMessage(e);

      LOG.severe("EJB error processing tax payment: " + e.getMessage());

      ErrorDTO error = new ErrorDTO(errorMessage, statusCode, statusText, "/transactions/pay-tax");
      return Response.status(statusCode)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }
}
