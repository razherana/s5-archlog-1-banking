package mg.razherana.banking.pret.api;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mg.razherana.banking.pret.application.ComptePretService;
import mg.razherana.banking.pret.dto.ComptePretDTO;
import mg.razherana.banking.pret.dto.ErrorDTO;
import mg.razherana.banking.pret.entities.ComptePret;
import mg.razherana.banking.pret.entities.User;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/comptes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ComptePretResource {
  private static final Logger LOG = Logger.getLogger(ComptePretResource.class.getName());

  @EJB
  private ComptePretService comptePretService;

  @GET
  public Response getAllComptes() {
    try {
      List<ComptePret> comptes = comptePretService.getComptes();
      List<ComptePretDTO> compteDTOs = comptes.stream()
          .map(ComptePretDTO::new)
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
      ComptePret compte = comptePretService.findById(id);
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/comptes/" + id);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
      ComptePretDTO compteDTO = new ComptePretDTO(compte);
      return Response.ok(compteDTO)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/comptes/" + id);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error getting compte by ID: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/comptes/" + id);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
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
      List<ComptePret> comptes = comptePretService.getComptesByUserId(userId);

      List<ComptePretDTO> compteDTOs = comptes.stream()
          .map(ComptePretDTO::new)
          .collect(Collectors.toList());
      return Response.ok(compteDTOs)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request",
            "/comptes/user/" + userId);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error getting comptes by user ID: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/comptes/user/" + userId);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
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
      // Use service method to find user (assumes user exists in central service)
      User user = comptePretService.findUser(userId);

      ComptePret compte = comptePretService.create(user);
      ComptePretDTO compteDTO = new ComptePretDTO(compte);
      return Response.status(Response.Status.CREATED)
          .type(MediaType.APPLICATION_JSON)
          .entity(compteDTO).build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request",
            "/comptes/user/" + userId);
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
      ComptePret compte = comptePretService.findById(id);
      if (compte == null) {
        ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/comptes/" + id);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      comptePretService.delete(id);
      return Response.noContent().build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/comptes/" + id);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error deleting compte: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/comptes/" + id);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
    } catch (Exception e) {
      LOG.severe("Error deleting compte: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/comptes/" + id);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }
}