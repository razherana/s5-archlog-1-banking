package mg.razherana.banking.interfaces.web.controllers.compteDepot;

import mg.razherana.banking.interfaces.application.compteDepotServices.CompteDepotService;
import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.CompteDepotDTO;
import mg.razherana.banking.courant.entities.CompteCourant;
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
import java.util.List;
import java.util.logging.Logger;

/**
 * Web Controller for deposit account detail page with withdrawal capabilities.
 */
@WebServlet("/comptes-depots/detail")
public class AccountDepotDetailController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(AccountDepotDetailController.class.getName());

  @EJB
  private CompteDepotService compteDepotService;

  @EJB
  private CompteCourantService compteCourantService;

  @EJB
  private ThymeleafService thymeleafService;

  @EJB
  private UserService userService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String accountIdStr = request.getParameter("id");

    if (accountIdStr == null || accountIdStr.trim().isEmpty()) {
      response.sendRedirect(request.getContextPath() + "/comptes-depots?error=" +
          URLEncoder.encode("ID de compte requis", StandardCharsets.UTF_8));
      return;
    }

    try {
      Integer accountId = Integer.parseInt(accountIdStr);
      CompteDepotDTO account = compteDepotService.getAccountById(userAdmin, accountId);

      if (account == null) {
        response.sendRedirect(request.getContextPath() + "/comptes-depots?error=" +
            URLEncoder.encode("Compte non trouvé", StandardCharsets.UTF_8));
        return;
      }

      // UserAdmin can access any account, no ownership check needed

      // Get user's current accounts for withdrawal dropdown
      List<CompteCourant> currentAccounts = compteCourantService.getAccountsByUserId(userAdmin, account.getUserId());

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication
          .buildApplication(getServletContext());
      WebContext webContext = new WebContext(application.buildExchange(request, response));

      // Add variables to context
      webContext.setVariable("userAdminName", userAdmin.getEmail());
      webContext.setVariable("account", account);
      webContext.setVariable("currentAccounts", currentAccounts);
      webContext.setVariable("error", request.getParameter("error"));
      webContext.setVariable("success", request.getParameter("success"));

      // Process template
      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext()).process("comptes-depots/detail", webContext,
          response.getWriter());

    } catch (NumberFormatException e) {
      response.sendRedirect(request.getContextPath() + "/comptes-depots?error=" +
          URLEncoder.encode("ID de compte invalide", StandardCharsets.UTF_8));
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String accountIdStr = request.getParameter("accountId");
    String action = request.getParameter("action");

    if (accountIdStr == null || action == null) {
      response.sendRedirect(request.getContextPath() + "/comptes-depots?error=" +
          URLEncoder.encode("Paramètres manquants", StandardCharsets.UTF_8));
      return;
    }

    try {
      Integer accountId = Integer.parseInt(accountIdStr);
      CompteDepotDTO account = compteDepotService.getAccountById(userAdmin, accountId);

      if (account == null) {
        response.sendRedirect(request.getContextPath() + "/comptes-depots?error=" +
            URLEncoder.encode("Compte non trouvé", StandardCharsets.UTF_8));
        return;
      }

      // UserAdmin can access any account, no ownership check needed

      String redirectUrl = request.getContextPath() + "/comptes-depots/detail?id=" + accountId;
      String result;

      switch (action) {
        case "withdraw":
          result = handleWithdrawal(request, account, userAdmin);
          break;
        default:
          result = "error=" + URLEncoder.encode("Action non reconnue", StandardCharsets.UTF_8);
          break;
      }

      response.sendRedirect(redirectUrl + "&" + result);

    } catch (NumberFormatException e) {
      response.sendRedirect(request.getContextPath() + "/comptes-depots?error=" +
          URLEncoder.encode("ID de compte invalide", StandardCharsets.UTF_8));
    } catch (Exception e) {
      e = ExceptionUtils.root(e);
      LOG.severe("Error processing deposit account action: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/comptes-depots?error=" +
          URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
    }
  }

  private String handleWithdrawal(HttpServletRequest request, CompteDepotDTO account, UserAdmin userAdmin) {
    try {
      String actionDateTime = request.getParameter("actionDateTime");
      String targetAccountIdStr = request.getParameter("targetAccountId");

      Integer targetAccountId = null;
      if (targetAccountIdStr != null && !targetAccountIdStr.trim().isEmpty()) {
        targetAccountId = Integer.parseInt(targetAccountIdStr);
      }

      String result = compteDepotService.withdrawFromAccount(userAdmin, account.getId(), targetAccountId, actionDateTime);

      if (result.startsWith("Retrait effectué")) {
        return "success=" + URLEncoder.encode(result, StandardCharsets.UTF_8);
      }

      return "error=" + result; // Already URL encoded
    } catch (NumberFormatException e) {
      return "error="
          + URLEncoder.encode("Format de données invalide", StandardCharsets.UTF_8);
    } catch (Exception e) {
      LOG.severe("Error during withdrawal: " + e.getMessage());
      return "error="
          + URLEncoder.encode(ExceptionUtils.root(e).getMessage(), StandardCharsets.UTF_8);
    }
  }
}