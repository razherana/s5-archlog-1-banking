package mg.razherana.banking.courant.api;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mg.razherana.banking.courant.application.UserService;
import mg.razherana.banking.courant.dto.ErrorDTO;
import mg.razherana.banking.courant.dto.MessageDTO;
import mg.razherana.banking.courant.dto.requests.users.CreateUserRequest;
import mg.razherana.banking.courant.dto.requests.users.UpdateUserRequest;
import mg.razherana.banking.courant.entities.User;

import java.util.List;
import java.util.logging.Logger;

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
            List<User> users = userService.getUsers();
            return Response.ok(users)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception e) {
            LOG.severe("Error getting all users: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/users");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Integer id) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                ErrorDTO error = new ErrorDTO("User not found", 404, "Not Found", "/users/" + id);
                return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }
            return Response.ok(user)
                .type(MediaType.APPLICATION_JSON)
                .build();
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
            User user = userService.findByEmail(email);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("User not found").build();
            }
            return Response.ok(user).build();
        } catch (Exception e) {
            LOG.severe("Error getting user by email: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error: " + e.getMessage()).build();
        }
    }

    @POST
    public Response createUser(CreateUserRequest request) {
        try {
            if (request.getName() == null || request.getEmail() == null || request.getPassword() == null) {
                ErrorDTO error = new ErrorDTO("Name, email and password are required", 400, "Bad Request", "/users");
                return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            
            userService.create(user);
            return Response.status(Response.Status.CREATED)
                .type(MediaType.APPLICATION_JSON)
                .entity(user).build();
        } catch (IllegalArgumentException e) {
            LOG.warning("Invalid user data: " + e.getMessage());
            ErrorDTO error = new ErrorDTO("Invalid data: " + e.getMessage(), 400, "Bad Request", "/users");
            return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        } catch (Exception e) {
            LOG.severe("Error creating user: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/users");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Integer id, UpdateUserRequest request) {
        try {
            User existingUser = userService.findById(id);
            if (existingUser == null) {
                ErrorDTO error = new ErrorDTO("User not found", 404, "Not Found", "/users/" + id);
                return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }
            
            if (request.getName() != null) existingUser.setName(request.getName());
            if (request.getEmail() != null) existingUser.setEmail(request.getEmail());
            if (request.getPassword() != null) existingUser.setPassword(request.getPassword());
            
            userService.update(existingUser);
            return Response.ok(existingUser)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (IllegalArgumentException e) {
            LOG.warning("Invalid user data: " + e.getMessage());
            ErrorDTO error = new ErrorDTO("Invalid data: " + e.getMessage(), 400, "Bad Request", "/users/" + id);
            return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
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
            User user = userService.findById(id);
            if (user == null) {
                ErrorDTO error = new ErrorDTO("User not found", 404, "Not Found", "/users/" + id);
                return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }
            
            userService.delete(id);
            MessageDTO message = new MessageDTO("User deleted successfully");
            return Response.ok(message)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception e) {
            LOG.severe("Error deleting user: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/users/" + id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        }
    }
}
