package mg.razherana.banking.interfaces.web.controllers.users;

import mg.razherana.banking.common.services.userServices.UserService;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Web Controller for deleting users.
 */
@WebServlet("/users/delete")
public class DeleteUserController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(DeleteUserController.class.getName());

  @EJB
  private UserService userService;

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    String userIdStr = request.getParameter("id");

    if (userIdStr == null || userIdStr.trim().isEmpty()) {
      response.sendRedirect("../users?error=" + URLEncoder.encode("ID utilisateur manquant", StandardCharsets.UTF_8));
      return;
    }

    try {
      Integer userId = Integer.parseInt(userIdStr);

      // No need to prevent self-deletion since we're now using UserAdmin for auth
      // and deleting User entities for data

      // Delete the user
      userService.deleteUser(userId);

      LOG.info("User deleted successfully: ID " + userId);
      response.sendRedirect(
          "../users?success=" + URLEncoder.encode("Utilisateur supprimé avec succès", StandardCharsets.UTF_8));

    } catch (NumberFormatException e) {
      response.sendRedirect("../users?error=" + URLEncoder.encode("ID utilisateur invalide", StandardCharsets.UTF_8));
    } catch (IllegalArgumentException e) {
      LOG.warning("Failed to delete user: " + e.getMessage());
      response.sendRedirect("../users?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
    } catch (Exception e) {
      LOG.severe("Error deleting user: " + e.getMessage());
      response.sendRedirect(
          "../users?error=" + URLEncoder.encode("Erreur système lors de la suppression", StandardCharsets.UTF_8));
    }
  }
}