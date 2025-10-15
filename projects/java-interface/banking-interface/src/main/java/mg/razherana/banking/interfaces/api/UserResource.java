package mg.razherana.banking.interfaces.api;

import mg.razherana.banking.interfaces.application.userServices.UserService;
import mg.razherana.banking.interfaces.dto.UserDTO;
import mg.razherana.banking.interfaces.dto.ErrorDTO;
import mg.razherana.banking.interfaces.dto.requests.RegisterRequest;
import mg.razherana.banking.interfaces.entities.User;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * REST API Resource for User management operations.
 * 
 * <p>
 * This resource provides HTTP endpoints for user CRUD operations and
 * can be called by external services or web applications.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

  private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

  @EJB
  private UserService userService;

  @GET
  public Response getAllUsers() {
    try {
      List<User> users = userService.getAllUsers();
      List<UserDTO> userDTOs = users.stream()
          .map(UserDTO::new)
          .collect(Collectors.toList());

      return Response.ok(userDTOs)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (EJBException e) {
      LOG.severe("EJB error getting all users: " + e.getMessage());
      ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/users");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @GET
  @Path("/{id}")
  public Response getUserById(@PathParam("id") Integer id) {
    try {
      User user = userService.findUserById(id);
      if (user == null) {
        ErrorDTO error = new ErrorDTO("User not found", 404, "Not Found", "/users/" + id);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      UserDTO userDTO = new UserDTO(user);
      return Response.ok(userDTO)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/users/" + id);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error getting user by ID: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/users/" + id);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
    } catch (Exception e) {
      LOG.severe("Error getting user by ID: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/users/" + id);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @GET
  @Path("/email/{email}")
  public Response getUserByEmail(@PathParam("email") String email) {
    try {
      User user = userService.findUserByEmail(email);
      if (user == null) {
        ErrorDTO error = new ErrorDTO("User not found", 404, "Not Found", "/users/email/" + email);
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      UserDTO userDTO = new UserDTO(user);
      return Response.ok(userDTO)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/users/email/" + email);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error getting user by email: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/users/email/" + email);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
    } catch (Exception e) {
      LOG.severe("Error getting user by email: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/users/email/" + email);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @POST
  public Response createUser(RegisterRequest request) {
    try {
      if (request == null) {
        ErrorDTO error = new ErrorDTO("Request body is required", 400, "Bad Request", "/users");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      User user = userService.createUser(request.getName(), request.getEmail(), request.getPassword());
      UserDTO userDTO = new UserDTO(user);

      return Response.status(Response.Status.CREATED)
          .type(MediaType.APPLICATION_JSON)
          .entity(userDTO).build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/users");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error creating user: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/users");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
    }
  }

  @PUT
  @Path("/{id}")
  public Response updateUser(@PathParam("id") Integer id, RegisterRequest request) {
    try {
      if (request == null) {
        ErrorDTO error = new ErrorDTO("Request body is required", 400, "Bad Request", "/users/" + id);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }

      User user = userService.updateUser(id, request.getName(), request.getEmail(), request.getPassword());
      UserDTO userDTO = new UserDTO(user);

      return Response.ok(userDTO)
          .type(MediaType.APPLICATION_JSON)
          .build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/users/" + id);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error updating user: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/users/" + id);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
    } catch (Exception e) {
      LOG.severe("Error updating user: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/users/" + id);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }

  @DELETE
  @Path("/{id}")
  public Response deleteUser(@PathParam("id") Integer id) {
    try {
      userService.deleteUser(id);
      return Response.noContent().build();
    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/users/" + id);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      } else {
        LOG.severe("EJB error deleting user: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/users/" + id);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error).build();
      }
    } catch (Exception e) {
      LOG.severe("Error deleting user: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/users/" + id);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.APPLICATION_JSON)
          .entity(error).build();
    }
  }
}