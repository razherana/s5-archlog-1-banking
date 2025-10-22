package mg.razherana.banking.interfaces.web.controllers.compteCourant;

import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;
import mg.razherana.banking.common.entities.User;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.common.services.userServices.UserService;
import mg.razherana.banking.common.utils.ExceptionUtils;
import mg.razherana.banking.interfaces.web.controllers.compteCourant.accountDetailDTOs.CompteData;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

  @EJB
  private UserService userService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String accountIdStr = request.getParameter("id");

    if (accountIdStr == null || accountIdStr.trim().isEmpty()) {
      response.sendRedirect("../comptes-courants?error=missing_account_id");
      return;
    }

    try {
      Integer accountId = Integer.parseInt(accountIdStr);
      CompteCourant account = compteCourantService.getAccountById(userAdmin, accountId);

      if (account == null) {
        response.sendRedirect("../comptes-courants?error=account_not_found");
        return;
      }

      // Get tax to pay amount
      BigDecimal taxToPay = compteCourantService.getTaxToPay(userAdmin, accountId, LocalDateTime.now());

      // Get all users for transfer functionality
      List<User> allUsers = userService.getAllUsers(userAdmin);

      // Get all accounts for each user (for JavaScript filtering)
      List<CompteCourant> allAccountsOld = compteCourantService.getAllAccounts(userAdmin);

      var allAccounts = allAccountsOld.stream()
          .map(
              ac -> new CompteData(ac.getId(), ac.getUserId(),
                  compteCourantService.getAccountBalance(userAdmin, accountId, null)))
          .toList();

      // Get current balance
      BigDecimal currentBalance = compteCourantService.getAccountBalance(userAdmin, accountId, LocalDateTime.now());

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
      WebContext context = new WebContext(application.buildExchange(request, response));

      System.out.println("All accounts : " + allAccounts);

      // Set template variables
      context.setVariable("userAdminName", userAdmin.getEmail());
      context.setVariable("account", account);
      context.setVariable("currentBalance", currentBalance);
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
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("../login.html");
      return;
    }

    UserAdmin userAdmin = (UserAdmin) session.getAttribute("userAdmin");
    String accountIdStr = request.getParameter("accountId");
    String action = request.getParameter("action");

    if (accountIdStr == null || action == null) {
      response.sendRedirect("../comptes-courants?error=missing_parameters");
      return;
    }

    try {
      Integer accountId = Integer.parseInt(accountIdStr);
      CompteCourant account = compteCourantService.getAccountById(userAdmin, accountId);

      if (account == null) {
        response.sendRedirect("../comptes-courants?error=account_not_found");
        return;
      }

      // UserAdmin can access any account, no ownership check needed

      boolean success = false;
      String errorMessage = null;
      String redirectUrl = "detail?id=" + accountId;

      switch (action) {
        case "deposit":
          errorMessage = handleDeposit(request, account, userAdmin);
          success = (errorMessage == null);

          if (success)
            redirectUrl += "&success=deposit_success";
          else
            redirectUrl += "&error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");
          break;

        case "withdraw":
          errorMessage = handleWithdrawal(request, account, userAdmin);
          success = (errorMessage == null);
          if (success)
            redirectUrl += "&success=withdrawal_success";
          else
            redirectUrl += "&error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");

          break;

        case "pay_tax":
          errorMessage = handleTaxPayment(request, account, userAdmin);
          success = (errorMessage == null);

          if (success)
            redirectUrl += "&success=tax_payment_success";
          else
            redirectUrl += "&error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");

          break;

        case "transfer":
          errorMessage = handleTransfer(request, account, userAdmin);
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

  private String handleDeposit(HttpServletRequest request, CompteCourant account, UserAdmin userAdmin) {
    try {
      String montantStr = request.getParameter("montant");
      String description = request.getParameter("description");
      String actionDateTimeStr = request.getParameter("actionDateTime");

      if (montantStr == null || montantStr.trim().isEmpty()) {
        return "Montant requis";
      }

      BigDecimal montant = new BigDecimal(montantStr);
      if (montant.compareTo(BigDecimal.ZERO) <= 0) {
        return "Le montant doit être positif";
      }

      LocalDateTime actionDateTime = LocalDateTime.now();
      if (actionDateTimeStr != null && !actionDateTimeStr.trim().isEmpty()) {
        try {
          actionDateTime = LocalDateTime.parse(actionDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
          LOG.warning("Invalid date format, using current time: " + e.getMessage());
        }
      }

      TransactionCourant result = compteCourantService.makeDeposit(
          userAdmin,
          account.getId(),
          montant,
          description != null && !description.trim().isEmpty() ? description : "Deposit via interface",
          actionDateTime);

      return result != null ? null : "Failed to create deposit transaction";

    } catch (NumberFormatException e) {
      return "Format de montant invalide";
    } catch (Exception e) {
      return ExceptionUtils.root(e).getMessage();
    }
  }

  private String handleWithdrawal(HttpServletRequest request, CompteCourant account, UserAdmin userAdmin) {
    try {
      String montantStr = request.getParameter("montant");
      String description = request.getParameter("description");
      String actionDateTimeStr = request.getParameter("actionDateTime");

      if (montantStr == null || montantStr.trim().isEmpty()) {
        return "Montant requis";
      }

      BigDecimal montant = new BigDecimal(montantStr);
      if (montant.compareTo(BigDecimal.ZERO) <= 0) {
        return "Le montant doit être positif";
      }

      LocalDateTime actionDateTime = LocalDateTime.now();
      if (actionDateTimeStr != null && !actionDateTimeStr.trim().isEmpty()) {
        try {
          actionDateTime = LocalDateTime.parse(actionDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
          LOG.warning("Invalid date format, using current time: " + e.getMessage());
        }
      }

      TransactionCourant result = compteCourantService.makeWithdrawal(
          userAdmin,
          account.getId(),
          montant,
          description != null && !description.trim().isEmpty() ? description : "Withdrawal via interface",
          actionDateTime);

      return result != null ? null : "Failed to create withdrawal transaction";

    } catch (NumberFormatException e) {
      return "Format de montant invalide";
    } catch (Exception e) {
      return ExceptionUtils.root(e).getMessage();
    }
  }

  private String handleTaxPayment(HttpServletRequest request, CompteCourant account, UserAdmin userAdmin) {
    String description = request.getParameter("description");
    String actionDateTimeStr = request.getParameter("actionDateTime");

    LocalDateTime actionDateTime = LocalDateTime.now();
    if (actionDateTimeStr != null && !actionDateTimeStr.trim().isEmpty()) {
      try {
        actionDateTime = LocalDateTime.parse(actionDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      } catch (Exception e) {
        LOG.warning("Invalid date format, using current time: " + e.getMessage());
      }
    }

    try {
      TransactionCourant result = compteCourantService.payTax(
          userAdmin,
          account.getId(),
          description != null && !description.trim().isEmpty() ? description : "Tax payment via interface",
          actionDateTime);

      return result != null ? null : "Failed to create tax payment transaction";
    } catch (Exception e) {
      return ExceptionUtils.root(e).getMessage();
    }
  }

  private String handleTransfer(HttpServletRequest request, CompteCourant account, UserAdmin userAdmin) {
    try {
      String destinationAccountIdStr = request.getParameter("destinationAccountId");
      String amountStr = request.getParameter("amount");
      String description = request.getParameter("description");
      String actionDateTimeStr = request.getParameter("actionDateTime");

      if (destinationAccountIdStr == null || destinationAccountIdStr.trim().isEmpty()) {
        return "Compte de destination requis";
      }

      if (amountStr == null || amountStr.trim().isEmpty()) {
        return "Montant requis";
      }

      Integer destinationAccountId = Integer.parseInt(destinationAccountIdStr);
      BigDecimal amount = new BigDecimal(amountStr);

      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        return "Le montant doit être positif";
      }

      if (destinationAccountId.equals(account.getId())) {
        return "Vous ne pouvez pas effectuer un transfert vers le même compte";
      }

      LocalDateTime actionDateTime = LocalDateTime.now();
      if (actionDateTimeStr != null && !actionDateTimeStr.trim().isEmpty()) {
        try {
          actionDateTime = LocalDateTime.parse(actionDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
          LOG.warning("Invalid date format, using current time: " + e.getMessage());
        }
      }

      boolean success = compteCourantService.makeTransfer(
          userAdmin,
          account.getId(),
          destinationAccountId,
          amount,
          description != null && !description.trim().isEmpty() ? description : "Transfer via interface",
          actionDateTime);

      return success ? null : "Failed to create transfer";

    } catch (NumberFormatException e) {
      return "Format de données invalide";
    } catch (Exception e) {
      return ExceptionUtils.root(e).getMessage();
    }
  }
}