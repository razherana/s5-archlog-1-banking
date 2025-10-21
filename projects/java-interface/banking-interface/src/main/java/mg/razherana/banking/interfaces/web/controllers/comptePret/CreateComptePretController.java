package mg.razherana.banking.interfaces.web.controllers.comptePret;

import mg.razherana.banking.interfaces.application.comptePretServices.ComptePretService;
import mg.razherana.banking.interfaces.application.compteCourantServices.CompteCourantService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.comptePret.*;
import mg.razherana.banking.courant.entities.CompteCourant;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Web Controller for creating new loan accounts (comptes prêts).
 */
@WebServlet("/comptes-prets/create")
public class CreateComptePretController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(CreateComptePretController.class.getName());

  @EJB
  private ComptePretService comptePretService;

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

    // Get loan types and current accounts for the user
    List<TypeComptePretDTO> loanTypes = comptePretService.getAllLoanTypes();
    List<CompteCourant> currentAccounts = compteCourantService.getAccountsByUserId(user.getId());

    // Create Thymeleaf context
    JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
    WebContext context = new WebContext(application.buildExchange(request, response));

    // Set template variables
    context.setVariable("userName", user.getName());
    context.setVariable("loanTypes", loanTypes);
    context.setVariable("currentAccounts", currentAccounts);
    context.setVariable("error", request.getParameter("error"));
    context.setVariable("success", request.getParameter("success"));

    // Process template and write response
    response.setContentType("text/html;charset=UTF-8");
    thymeleafService.getTemplateEngine(getServletContext())
        .process("comptes-prets/create", context, response.getWriter());
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
    Integer userId = user.getId();

    try {
      // Get form parameters
      String typeComptePretIdParam = request.getParameter("typeComptePretId");
      String montantParam = request.getParameter("montant");
      String dateDebutParam = request.getParameter("dateDebut");
      String dateFinParam = request.getParameter("dateFin");
      String compteCourantIdParam = request.getParameter("compteCourantId");

      // Validate required fields
      if (typeComptePretIdParam == null || typeComptePretIdParam.trim().isEmpty() ||
          montantParam == null || montantParam.trim().isEmpty() ||
          dateDebutParam == null || dateDebutParam.trim().isEmpty() ||
          dateFinParam == null || dateFinParam.trim().isEmpty() ||
          compteCourantIdParam == null || compteCourantIdParam.trim().isEmpty()) {
        response.sendRedirect("create?error=missing_fields");
        return;
      }

      // Parse and validate fields
      Integer typeComptePretId = Integer.parseInt(typeComptePretIdParam);
      BigDecimal montant = new BigDecimal(montantParam);
      Integer compteCourantId = Integer.parseInt(compteCourantIdParam);

      if (montant.compareTo(BigDecimal.ZERO) <= 0) {
        response.sendRedirect("create?error=invalid_amount");
        return;
      }

      // Parse dates
      LocalDateTime dateDebut = LocalDateTime.parse(dateDebutParam + "T00:00:00");
      LocalDateTime dateFin = LocalDateTime.parse(dateFinParam + "T23:59:59");

      if (dateFin.isBefore(dateDebut)) {
        response.sendRedirect("create?error=invalid_date_range");
        return;
      }

      // Verify that the current account belongs to the user
      CompteCourant currentAccount = compteCourantService.getAccountById(compteCourantId);
      if (currentAccount == null || !currentAccount.getUserId().equals(userId)) {
        response.sendRedirect("create?error=invalid_current_account");
        return;
      }

      // Create loan request
      CreateComptePretRequest loanRequest = new CreateComptePretRequest();
      loanRequest.setUserId(userId);
      loanRequest.setTypeComptePretId(typeComptePretId);
      loanRequest.setMontant(montant);
      loanRequest.setDateDebut(dateDebut);
      loanRequest.setDateFin(dateFin);
      loanRequest.setCompteCourantId(compteCourantId);

      LOG.info("Creating loan for user: " + userId + " with amount: " + montant +
          " to be deposited in account: " + compteCourantId);

      // Create the loan
      ComptePretDTO createdLoan = comptePretService.createLoan(loanRequest);

      if (createdLoan != null) {
        LOG.info("Loan created successfully: " + createdLoan.getId());
        response.sendRedirect("../comptes-prets?success=loan_created");
      } else {
        LOG.warning("Failed to create loan for user: " + userId);
        response.sendRedirect("create?error=creation_failed");
      }

    } catch (NumberFormatException e) {
      LOG.warning("Invalid number format in loan creation: " + e.getMessage());
      response.sendRedirect("create?error=invalid_format");
    } catch (Exception e) {
      LOG.severe("Error during loan creation: " + e.getMessage());
      response.sendRedirect("create?error=system_error");
    }
  }
}