package mg.razherana.banking.courant.api;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mg.razherana.banking.courant.application.CompteCourantService;
import mg.razherana.banking.courant.application.UserService;
import mg.razherana.banking.courant.dto.CompteCourantDTO;
import mg.razherana.banking.courant.dto.ErrorDTO;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.User;

import java.math.BigDecimal;
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

  @EJB
  private UserService userService;

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
    } catch (Exception e) {
      LOG.severe("Error getting all comptes: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/comptes");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
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
      User user = userService.findById(userId);
      if (user == null) {
        ErrorDTO error = new ErrorDTO("User not found", 404, "Not Found", "/comptes/user/" + userId);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      List<CompteCourant> comptes = compteCourantService.getComptesByUser(user);
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
  public Response createCompte(@PathParam("userId") Integer userId) {
    try {
      User user = userService.findById(userId);
      if (user == null) {
        ErrorDTO error = new ErrorDTO("User not found", 404, "Not Found", "/comptes/user/" + userId);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      CompteCourant compte = compteCourantService.create(user);
      CompteCourantDTO compteDTO = new CompteCourantDTO(compte, compteCourantService.calculateSolde(compte));
      return Response.status(Response.Status.CREATED)
          .type(MediaType.APPLICATION_JSON)
          .entity(compteDTO).build();
    } catch (IllegalArgumentException e) {
      LOG.warning("Invalid data: " + e.getMessage());
      ErrorDTO error = new ErrorDTO("Invalid data: " + e.getMessage(), 400, "Bad Request", "/comptes/user/" + userId);
      return Response.status(Response.Status.BAD_REQUEST)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    } catch (Exception e) {
      LOG.severe("Error creating compte: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/comptes/user/" + userId);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
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
}
