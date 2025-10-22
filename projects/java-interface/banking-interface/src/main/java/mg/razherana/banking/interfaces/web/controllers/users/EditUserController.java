package mg.razherana.banking.interfaces.web.controllers.users;

import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.common.entities.User;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.userServices.UserService;
import mg.razherana.banking.common.utils.ExceptionUtils;

import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

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
 * Web Controller for editing existing users.
 */
@WebServlet("/users/edit")
public class EditUserController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(EditUserController.class.getName());

  @EJB
  private UserService userService;

  @EJB
  private ThymeleafService thymeleafService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    UserAdmin currentUserAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String userIdStr = request.getParameter("id");

    if (userIdStr == null || userIdStr.trim().isEmpty()) {
      response.sendRedirect("../users?error=" + URLEncoder.encode("ID utilisateur manquant", StandardCharsets.UTF_8));
      return;
    }

    try {
      Integer userId = Integer.parseInt(userIdStr);
      User userToEdit = userService.findUserById(currentUserAdmin, userId);

      if (userToEdit == null) {
        response.sendRedirect("../users?error=" + URLEncoder.encode("Utilisateur introuvable", StandardCharsets.UTF_8));
        return;
      }

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
      WebContext context = new WebContext(application.buildExchange(request, response));

      // Set template variables
      context.setVariable("userAdminName", currentUserAdmin.getEmail());
      context.setVariable("userToEdit", userToEdit);
      context.setVariable("error", request.getParameter("error"));

      // Process template and write response
      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext())
          .process("users/edit", context, response.getWriter());

    } catch (NumberFormatException e) {
      response.sendRedirect("../users?error=" + URLEncoder.encode("ID utilisateur invalide", StandardCharsets.UTF_8));
    } catch (Exception e) {
      e = ExceptionUtils.root(e);
      LOG.severe("Error loading user for edit: " + e.getMessage());
      response.sendRedirect("../users?error=" + URLEncoder.encode("Erreur système", StandardCharsets.UTF_8));
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    UserAdmin currentUserAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String userIdStr = request.getParameter("id");
    String name = request.getParameter("name");

    if (userIdStr == null || userIdStr.trim().isEmpty()) {
      response.sendRedirect("../users?error=" + URLEncoder.encode("ID utilisateur manquant", StandardCharsets.UTF_8));
      return;
    }

    try {
      Integer userId = Integer.parseInt(userIdStr);

      // Update the user (User entity for data, not for auth)
      userService.updateUser(
          currentUserAdmin,
          userId,
          name != null && !name.trim().isEmpty() ? name.trim() : null);

      LOG.info("User updated successfully for ID: " + userId);
      response.sendRedirect(
          "../users?success=" + URLEncoder.encode("Utilisateur modifié avec succès", StandardCharsets.UTF_8));

    } catch (NumberFormatException e) {
      response.sendRedirect("../users?error=" + URLEncoder.encode("ID utilisateur invalide", StandardCharsets.UTF_8));
    } catch (IllegalArgumentException e) {
      LOG.warning("Failed to update user: " + e.getMessage());
      response
          .sendRedirect("edit?id=" + userIdStr + "&error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
    } catch (Exception e) {
      e = ExceptionUtils.root(e);
      LOG.severe("Error updating user: " + e.getMessage());
      response.sendRedirect("edit?id=" + userIdStr + "&error="
          + URLEncoder.encode("Erreur système lors de la modification", StandardCharsets.UTF_8));
    }
  }
}