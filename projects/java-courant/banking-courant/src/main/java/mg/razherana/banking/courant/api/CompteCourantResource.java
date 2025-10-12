package mg.razherana.banking.courant.api;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mg.razherana.banking.courant.application.CompteCourantService;
import mg.razherana.banking.courant.dto.CompteCourantDTO;
import mg.razherana.banking.courant.dto.ErrorDTO;
import mg.razherana.banking.courant.dto.requests.UpdateTaxeRequest;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/comptes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompteCourantResource {
  private static final Logger LOG = Logger.getLogger(CompteCourantResource.class.getName());

  @EJB
  private CompteCourantService compteCourantService;

  @GET
  public Response getAllComptes() {
    try {
      List<CompteCourant> comptes = compteCourantService.getComptes();
      List<CompteCourantDTO> compteDTOs = comptes.stream()
          .map(compte -> {
            BigDecimal solde = compteCourantService.calculateSolde(compte);
            return new CompteCourantDTO(compte, solde);
          })
          .collect(Collectors.toList());
      return Response.ok(compteDTOs)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/comptes");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error getting all comptes: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/comptes");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
    }
  }

  @GET
  @Path("/{id}")
  public Response getCompteById(@PathParam("id") Integer id) {
    try {
      CompteCourant compte = compteCourantService.findById(id);
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/comptes/" + id);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
      CompteCourantDTO compteDTO = new CompteCourantDTO(compte, compteCourantService.calculateSolde(compte));
      return Response.ok(compteDTO)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      LOG.severe("Error getting compte by ID: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/comptes/" + id);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }


  @GET
  @Path("/user/{userId}")
  public Response getComptesByUserId(@PathParam("userId") Integer userId) {
    try {
      // Use service method to get User (assumes user exists in central service)
      List<CompteCourant> comptes = compteCourantService.getComptesByUserId(userId);
      
      List<CompteCourantDTO> compteDTOs = comptes.stream()
          .map(compte -> {
            BigDecimal solde = compteCourantService.calculateSolde(compte);
            return new CompteCourantDTO(compte, solde);
          })
          .collect(Collectors.toList());
      return Response.ok(compteDTOs)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      LOG.severe("Error getting comptes by user ID: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/comptes/user/" + userId);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }


  @POST
  @Path("/user/{userId}")
  public Response createCompte(@PathParam("userId") Integer userId, @QueryParam("taxe") @DefaultValue("0") BigDecimal taxe) {
    try {
      // Use service method to find user (assumes user exists in central service)
      User user = compteCourantService.findUser(userId);

      CompteCourant compte = compteCourantService.create(user, taxe);
      CompteCourantDTO compteDTO = new CompteCourantDTO(compte, compteCourantService.calculateSolde(compte));
      return Response.status(Response.Status.CREATED)
          .type(MediaType.APPLICATION_JSON)
          .entity(compteDTO).build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/comptes/user/" + userId);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error creating compte: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/comptes/user/" + userId);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
    }
  }

  @DELETE
  @Path("/{id}")
  public Response deleteCompte(@PathParam("id") Integer id) {
    try {
      CompteCourant compte = compteCourantService.findById(id);
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/comptes/" + id);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      compteCourantService.delete(id);
      return Response.noContent().build();
    } catch (Exception e) {
      LOG.severe("Error deleting compte: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/comptes/" + id);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @GET
  @Path("/{id}/tax-to-pay")
  public Response getTaxToPay(@PathParam("id") Integer id, @QueryParam("actionDateTime") String actionDateTimeStr) {
    try {
      CompteCourant compte = compteCourantService.findById(id);
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/comptes/" + id + "/tax-to-pay");
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      LocalDateTime actionDateTime;
      if (actionDateTimeStr == null || actionDateTimeStr.trim().isEmpty()) {
        actionDateTime = LocalDateTime.now();
      } else {
        try {
          actionDateTime = LocalDateTime.parse(actionDateTimeStr);
        } catch (Exception e) {
          ErrorDTO error = new ErrorDTO("Invalid actionDateTime format. Use ISO format: YYYY-MM-DDTHH:MM:SS", 400, "Bad Request", "/comptes/" + id + "/tax-to-pay");
          return Response.status(Response.Status.BAD_REQUEST)
              .type(MediaType.APPLICATION_JSON)
              .entity(error).build();
        }
      }

      BigDecimal taxToPay = compteCourantService.getTaxToPay(compte, actionDateTime);
      return Response.ok("{\"taxToPay\": " + taxToPay + "}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      LOG.severe("Error getting tax to pay: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/comptes/" + id + "/tax-to-pay");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @GET
  @Path("/{id}/tax-paid")
  public Response getTaxPaid(@PathParam("id") Integer id, @QueryParam("actionDateTime") String actionDateTimeStr) {
    try {
      CompteCourant compte = compteCourantService.findById(id);
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/comptes/" + id + "/tax-paid");
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      BigDecimal taxPaid;
      if (actionDateTimeStr == null || actionDateTimeStr.trim().isEmpty()) {
        taxPaid = compteCourantService.getTaxPaidTotal(compte);
      } else {
        try {
          LocalDateTime actionDateTime = LocalDateTime.parse(actionDateTimeStr);
          taxPaid = compteCourantService.getTaxPaidDate(compte, actionDateTime);
        } catch (Exception e) {
          ErrorDTO error = new ErrorDTO("Invalid actionDateTime format. Use ISO format: YYYY-MM-DDTHH:MM:SS", 400, "Bad Request", "/comptes/" + id + "/tax-paid");
          return Response.status(Response.Status.BAD_REQUEST)
              .type(MediaType.APPLICATION_JSON)
              .entity(error).build();
        }
      }

      return Response.ok("{\"taxPaid\": " + taxPaid + "}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      LOG.severe("Error getting tax paid: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/comptes/" + id + "/tax-paid");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @GET
  @Path("/{id}/tax-status")
  public Response getTaxStatus(@PathParam("id") Integer id, @QueryParam("actionDateTime") String actionDateTimeStr) {
    try {
      CompteCourant compte = compteCourantService.findById(id);
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/comptes/" + id + "/tax-status");
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      LocalDateTime actionDateTime;
      if (actionDateTimeStr == null || actionDateTimeStr.trim().isEmpty()) {
        actionDateTime = LocalDateTime.now();
      } else {
        try {
          actionDateTime = LocalDateTime.parse(actionDateTimeStr);
        } catch (Exception e) {
          ErrorDTO error = new ErrorDTO("Invalid actionDateTime format. Use ISO format: YYYY-MM-DDTHH:MM:SS", 400, "Bad Request", "/comptes/" + id + "/tax-status");
          return Response.status(Response.Status.BAD_REQUEST)
              .type(MediaType.APPLICATION_JSON)
              .entity(error).build();
        }
      }

      boolean isTaxPaid = compteCourantService.isTaxPaid(compte, actionDateTime);
      BigDecimal taxToPay = compteCourantService.getTaxToPay(compte, actionDateTime);
      return Response.ok("{\"isPaid\": " + isTaxPaid + ", \"taxToPay\": " + taxToPay + "}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      LOG.severe("Error getting tax status: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/comptes/" + id + "/tax-status");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @PUT
  @Path("/{id}/taxe")
  public Response updateTaxe(@PathParam("id") Integer id, UpdateTaxeRequest request) {
    try {
      CompteCourant compte = compteCourantService.findById(id);
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/comptes/" + id + "/taxe");
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      if (request == null || request.getTaxe() == null) {
        ErrorDTO error = new ErrorDTO("Tax amount is required", 400, "Bad Request", "/comptes/" + id + "/taxe");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      if (request.getTaxe().compareTo(BigDecimal.ZERO) < 0) {
        ErrorDTO error = new ErrorDTO("Tax amount must be non-negative", 400, "Bad Request", "/comptes/" + id + "/taxe");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      compteCourantService.updateTaxe(compte, request.getTaxe());
      return Response.ok("{\"message\": \"Tax updated successfully\", \"newTaxe\": " + request.getTaxe() + "}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid taxe update from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/comptes/" + id + "/taxe");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error updating taxe: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/comptes/" + id + "/taxe");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
    }
  }
}
