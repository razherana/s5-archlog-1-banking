package mg.razherana.banking.interfaces.web.controllers.compteDepot;

import mg.razherana.banking.interfaces.application.compteDepotServices.CompteDepotService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.CompteDepotDTO;
import mg.razherana.banking.interfaces.dto.TypeCompteDepotDTO;
import mg.razherana.banking.interfaces.entities.User;

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
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

/**
 * Web Controller for creating new deposit accounts.
 */
@WebServlet("/comptes-depots/create")
public class CreateCompteDepotController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(CreateCompteDepotController.class.getName());

  @EJB
  private CompteDepotService compteDepotService;

  @EJB
  private ThymeleafService thymeleafService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    User user = (User) session.getAttribute("user");

    try {
      // Get available deposit types
      List<TypeCompteDepotDTO> depositTypes = compteDepotService.getAllDepositTypes();

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication
          .buildApplication(getServletContext());
      WebContext webContext = new WebContext(application.buildExchange(request, response));

      // Add variables to context
      webContext.setVariable("user", user);
      webContext.setVariable("depositTypes", depositTypes);
      webContext.setVariable("error", request.getParameter("error"));
      webContext.setVariable("success", request.getParameter("success"));

      // Process template
      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext()).process("comptes-depots/create", webContext,
          response.getWriter());

    } catch (Exception e) {
      LOG.severe("Error loading create deposit account form: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/comptes-depots?error=" +
          java.net.URLEncoder.encode("Erreur lors du chargement du formulaire",
              java.nio.charset.StandardCharsets.UTF_8));
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    User user = (User) session.getAttribute("user");

    try {
      // Extract form parameters
      String typeIdStr = request.getParameter("typeCompteDepotId");
      String montantStr = request.getParameter("montant");
      String dateEcheance = request.getParameter("dateEcheance");
      String actionDateTime = request.getParameter("actionDateTime");

      // Validate required parameters
      if (typeIdStr == null || typeIdStr.trim().isEmpty() ||
          montantStr == null || montantStr.trim().isEmpty() ||
          dateEcheance == null || dateEcheance.trim().isEmpty()) {

        response.sendRedirect(request.getContextPath() + "/comptes-depots/create?error=" +
            java.net.URLEncoder.encode("Tous les champs sont requis", java.nio.charset.StandardCharsets.UTF_8));
        return;
      }

      // Parse parameters
      Integer typeId = Integer.parseInt(typeIdStr);
      BigDecimal montant = new BigDecimal(montantStr);

      // Validate amount
      if (montant.compareTo(BigDecimal.ZERO) <= 0) {
        response.sendRedirect(request.getContextPath() + "/comptes-depots/create?error=" +
            java.net.URLEncoder.encode("Le montant doit être positif", java.nio.charset.StandardCharsets.UTF_8));
        return;
      }

      // Format dates properly for .NET API (ISO format)
      String formattedEcheance = dateEcheance + "T00:00:00";
      String formattedActionDateTime = null;
      if (actionDateTime != null && !actionDateTime.trim().isEmpty()) {
        formattedActionDateTime = actionDateTime + ":00"; // Add seconds if missing
      }

      // Create the account
      CompteDepotDTO createdAccount = compteDepotService.createAccount(
          typeId, user.getId(), formattedEcheance, montant, formattedActionDateTime);

      if (createdAccount != null) {
        response.sendRedirect(request.getContextPath() + "/comptes-depots?success=" +
            java.net.URLEncoder.encode("Compte dépôt créé avec succès", java.nio.charset.StandardCharsets.UTF_8));
      } else {
        response.sendRedirect(request.getContextPath() + "/comptes-depots/create?error=" +
            java.net.URLEncoder.encode("Erreur lors de la création du compte",
                java.nio.charset.StandardCharsets.UTF_8));
      }

    } catch (NumberFormatException e) {
      response.sendRedirect(request.getContextPath() + "/comptes-depots/create?error=" +
          java.net.URLEncoder.encode("Format de données invalide", java.nio.charset.StandardCharsets.UTF_8));
    } catch (Exception e) {
      LOG.severe("Error creating deposit account: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/comptes-depots/create?error=" +
          java.net.URLEncoder.encode("Erreur interne lors de la création", java.nio.charset.StandardCharsets.UTF_8));
    }
  }
}