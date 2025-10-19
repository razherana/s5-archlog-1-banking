package mg.razherana.banking.interfaces.web.controllers.compteCourant;

import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.CompteCourantDTO;
import mg.razherana.banking.interfaces.dto.UserDTO;
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
import java.util.List;
import java.util.logging.Logger;

/**
 * Web Controller for account detail page with transaction capabilities.
 */
@WebServlet("/comptes-courants/detail")
public class AccountDetailController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(AccountDetailController.class.getName());

  @EJB
  private CompteCourantService compteCourantService;

  @EJB
  private ThymeleafService thymeleafService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    User user = (User) session.getAttribute("user");
    String accountIdStr = request.getParameter("id");

    if (accountIdStr == null || accountIdStr.trim().isEmpty()) {
      response.sendRedirect("../comptes-courants?error=missing_account_id");
      return;
    }

    try {
      Integer accountId = Integer.parseInt(accountIdStr);
      CompteCourantDTO account = compteCourantService.getAccountById(accountId);

      if (account == null) {
        response.sendRedirect("../comptes-courants?error=account_not_found");
        return;
      }

      // Verify that the account belongs to the logged-in user
      if (!account.getUserId().equals(user.getId())) {
        response.sendRedirect("../comptes-courants?error=unauthorized_access");
        return;
      }

      // Get tax to pay amount
      Double taxToPay = compteCourantService.getTaxToPay(accountId);

      // Get all users for transfer functionality
      List<UserDTO> allUsers = compteCourantService.getAllUsers();

      // Get all accounts for each user (for JavaScript filtering)
      List<CompteCourantDTO> allAccounts = compteCourantService.getAllAccounts();

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
      WebContext context = new WebContext(application.buildExchange(request, response));

      System.out.println("All accounts : " + allAccounts);
      System.out.println("Tomee serialization config : " + System.getProperty("tomee.serialization.class.whitelist"));
      System.out.println("Tomee serialization config : " + System.getProperty("tomee.serialization.class.blacklist"));

      // Set template variables
      context.setVariable("userName", user.getName());
      context.setVariable("account", account);
      context.setVariable("taxToPay", taxToPay);
      context.setVariable("allUsers", allUsers);
      context.setVariable("allAccounts", allAccounts);
      context.setVariable("error", request.getParameter("error"));
      context.setVariable("success", request.getParameter("success"));

      // Process template and write response
      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext())
          .process("comptes-courants/detail", context, response.getWriter());

    } catch (NumberFormatException e) {
      response.sendRedirect("../comptes-courants?error=invalid_account_id");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    User user = (User) session.getAttribute("user");
    String accountIdStr = request.getParameter("accountId");
    String action = request.getParameter("action");

    if (accountIdStr == null || action == null) {
      response.sendRedirect("../comptes-courants?error=missing_parameters");
      return;
    }

    try {
      Integer accountId = Integer.parseInt(accountIdStr);
      CompteCourantDTO account = compteCourantService.getAccountById(accountId);

      if (account == null || !account.getUserId().equals(user.getId())) {
        response.sendRedirect("../comptes-courants?error=unauthorized_access");
        return;
      }

      boolean success = false;
      String errorMessage = null;
      String redirectUrl = "detail?id=" + accountId;

      switch (action) {
        case "deposit":
          errorMessage = handleDeposit(request, account);
          success = (errorMessage == null);

          if (success)
            redirectUrl += "&success=deposit_success";
          else
            redirectUrl += "&error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");
          break;

        case "withdraw":
          errorMessage = handleWithdrawal(request, account);
          success = (errorMessage == null);
          if (success)
            redirectUrl += "&success=withdrawal_success";
          else
            redirectUrl += "&error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");

          break;

        case "pay_tax":
          errorMessage = handleTaxPayment(request, account);
          success = (errorMessage == null);

          if (success)
            redirectUrl += "&success=tax_payment_success";
          else
            redirectUrl += "&error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");

          break;

        case "transfer":
          errorMessage = handleTransfer(request, account, user);
          success = (errorMessage == null);

          if (success)
            redirectUrl += "&success=transfer_success";
          else
            redirectUrl += "&error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");

          break;

        default:
          redirectUrl += "&error=" + java.net.URLEncoder.encode("Action invalide", "UTF-8");
      }

      response.sendRedirect(redirectUrl);

    } catch (NumberFormatException e) {
      response.sendRedirect("../comptes-courants?error=invalid_account_id");
    } catch (Exception e) {
      LOG.severe("Error processing transaction: " + e.getMessage());
      response.sendRedirect("detail?id=" + accountIdStr + "&error=" + e.getMessage());
    }
  }

  private String handleDeposit(HttpServletRequest request, CompteCourantDTO account) {
    try {
      String montantStr = request.getParameter("montant");
      String description = request.getParameter("description");
      String actionDateTime = request.getParameter("actionDateTime");

      if (montantStr == null || montantStr.trim().isEmpty()) {
        return "Montant requis";
      }

      Double montant = Double.parseDouble(montantStr);
      if (montant <= 0) {
        return "Le montant doit être positif";
      }

      return compteCourantService.makeDeposit(
          account.getId(),
          montant,
          description != null && !description.trim().isEmpty() ? description : "Deposit via interface",
          actionDateTime);

    } catch (NumberFormatException e) {
      return "Format de montant invalide";
    }
  }

  private String handleWithdrawal(HttpServletRequest request, CompteCourantDTO account) {
    try {
      String montantStr = request.getParameter("montant");
      String description = request.getParameter("description");
      String actionDateTime = request.getParameter("actionDateTime");

      if (montantStr == null || montantStr.trim().isEmpty()) {
        return "Montant requis";
      }

      Double montant = Double.parseDouble(montantStr);
      if (montant <= 0) {
        return "Le montant doit être positif";
      }

      return compteCourantService.makeWithdrawal(
          account.getId(),
          montant,
          description != null && !description.trim().isEmpty() ? description : "Withdrawal via interface",
          actionDateTime);

    } catch (NumberFormatException e) {
      return "Format de montant invalide";
    }
  }

  private String handleTaxPayment(HttpServletRequest request, CompteCourantDTO account) {
    String description = request.getParameter("description");
    String actionDateTime = request.getParameter("actionDateTime");
    return compteCourantService.payTax(
        account.getId(),
        description != null && !description.trim().isEmpty() ? description : "Tax payment via interface",
        actionDateTime);
  }

  private String handleTransfer(HttpServletRequest request, CompteCourantDTO account, User user) {
    try {
      String destinationAccountIdStr = request.getParameter("destinationAccountId");
      String amountStr = request.getParameter("amount");
      String description = request.getParameter("description");
      String actionDateTime = request.getParameter("actionDateTime");

      if (destinationAccountIdStr == null || destinationAccountIdStr.trim().isEmpty()) {
        return "Compte de destination requis";
      }

      if (amountStr == null || amountStr.trim().isEmpty()) {
        return "Montant requis";
      }

      Integer destinationAccountId = Integer.parseInt(destinationAccountIdStr);
      Double amount = Double.parseDouble(amountStr);

      if (amount <= 0) {
        return "Le montant doit être positif";
      }

      if (destinationAccountId.equals(account.getId())) {
        return "Vous ne pouvez pas effectuer un transfert vers le même compte";
      }

      return compteCourantService.makeTransfer(
          account.getId(),
          destinationAccountId,
          amount,
          description != null && !description.trim().isEmpty() ? description : "Transfer via interface",
          actionDateTime);

    } catch (NumberFormatException e) {
      return "Format de données invalide";
    }
  }
}