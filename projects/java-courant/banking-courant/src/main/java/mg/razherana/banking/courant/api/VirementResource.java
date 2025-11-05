package mg.razherana.banking.courant.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.ejb.EJB;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mg.razherana.banking.common.utils.ExceptionUtils;
import mg.razherana.banking.courant.application.compteCourantService.CompteCourantService;
import mg.razherana.banking.courant.application.transactionService.TransactionService;
import mg.razherana.banking.courant.dto.ErrorDTO;
import mg.razherana.banking.courant.dto.TransactionCourantDTO;
import mg.razherana.banking.courant.dto.requests.transactions.InvaliderVirementRequest;
import mg.razherana.banking.courant.dto.requests.transactions.ValiderVirementRequest;
import mg.razherana.banking.courant.dto.requests.virements.VirementDuJourDTO;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/virements")
public class VirementResource {
  private static final Logger LOG = Logger.getLogger(VirementResource.class.getName());

  @EJB
  private CompteCourantService compteCourantService;

  @EJB
  private TransactionService transactionService;

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

  @POST
  @Path("/virement-jour")
  public Response getVirementDuJour(@Valid VirementDuJourDTO request) {
    try {
      CompteCourant compte = compteCourantService.findById(request.getId());
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/virements/virement-jour/");
        return Response.status(Response.Status.NOT_FOUND)
            .entity(error).build();
      }

      if (request.getDate() == null)
        request.setDate(LocalDate.now());

      List<TransactionCourant> transactions = compteCourantService.getVirementToday(compte, request.getDate());

      List<TransactionCourantDTO> transactionDTOs = transactions.stream()
          .map(TransactionCourantDTO::new)
          .collect(Collectors.toList());

      return Response.ok(transactionDTOs)
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

      ErrorDTO error = new ErrorDTO(errorMessage, statusCode, statusText, "/virements/virement-jour/");
      return Response.status(statusCode)
          .entity(error).build();
    }
  }

  @POST
  @Path("/valider")
  public Response valider(@Valid ValiderVirementRequest request) {
    try {
      if (request.getDateValidation() == null) {
        request.setDateValidation(LocalDateTime.now());
      }

      TransactionCourant validatedVirement = transactionService.validerVirement(
          request.getId(), request.getDateValidation());

      TransactionCourantDTO transactionDTO = new TransactionCourantDTO(validatedVirement);

      return Response.status(Response.Status.OK)
          .entity(transactionDTO).build();
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
  @Path("/invalider")
  public Response invalider(@Valid InvaliderVirementRequest request) {
    try {
      TransactionCourant validatedVirement = transactionService.validerVirement(
          request.getId(),
          null);

      TransactionCourantDTO transactionDTO = new TransactionCourantDTO(validatedVirement);

      return Response.status(Response.Status.OK)
          .entity(transactionDTO).build();
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
}
