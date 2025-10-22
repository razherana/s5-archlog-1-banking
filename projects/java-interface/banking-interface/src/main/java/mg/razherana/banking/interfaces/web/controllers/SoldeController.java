package mg.razherana.banking.interfaces.web.controllers;

import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.common.entities.User;
import mg.razherana.banking.common.services.userServices.UserService;

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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/solde")
public class SoldeController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(SoldeController.class.getName());

  @EJB
  private UserService userService;

  @EJB
  private ThymeleafService thymeleafService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("userAdmin") == null) {
      response.sendRedirect("login.html");
      return;
    }

    String userIdParam = request.getParameter("userId");
    String actionDateTime = request.getParameter("actionDateTime");

    if (userIdParam == null || userIdParam.trim().isEmpty()) {
      response.sendRedirect("menu?error=User ID is required");
      return;
    }

    try {
      Integer userId = Integer.parseInt(userIdParam);
      User user = userService.findUserById(userId);

      // Get individual module balances and total
      Map<String, Object> balanceData = new HashMap<>();

      try {
        // Use UserService to get detailed breakdown
        BigDecimal currentAccountBalance = userService.getCurrentAccountBalance(userId, actionDateTime);
        balanceData.put("currentAccountBalance", currentAccountBalance);
        balanceData.put("currentAccountError", null);
      } catch (Exception e) {
        LOG.warning("Failed to get current account balance for user " + userId + ": " + e.getMessage());
        balanceData.put("currentAccountBalance", null);
        balanceData.put("currentAccountError", e.getMessage());
      }

      try {
        BigDecimal loanBalance = userService.getLoanBalance(userId, actionDateTime);
        balanceData.put("loanBalance", loanBalance);
        balanceData.put("loanError", null);
      } catch (Exception e) {
        LOG.warning("Failed to get loan balance for user " + userId + ": " + e.getMessage());
        balanceData.put("loanBalance", null);
        balanceData.put("loanError", e.getMessage());
      }

      try {
        BigDecimal depositBalance = userService.getDepositBalance(userId, actionDateTime);
        balanceData.put("depositBalance", depositBalance);
        balanceData.put("depositError", null);
      } catch (Exception e) {
        LOG.warning("Failed to get deposit balance for user " + userId + ": " + e.getMessage());
        balanceData.put("depositBalance", null);
        balanceData.put("depositError", e.getMessage());
      }

      // Calculate total using UserService
      try {
        BigDecimal totalBalance = userService.calculateTotalBalanceAcrossModules(userId, actionDateTime);
        balanceData.put("totalBalance", totalBalance);
        balanceData.put("totalError", null);
      } catch (Exception e) {
        LOG.warning("Failed to calculate total balance for user " + userId + ": " + e.getMessage());
        balanceData.put("totalBalance", null);
        balanceData.put("totalError", e.getMessage());
      }

      renderSoldeView(request, response, user, null, balanceData);

    } catch (NumberFormatException e) {
      LOG.severe("Invalid user ID format: " + userIdParam);
      response.sendRedirect("menu?error=Invalid user ID format");
    } catch (Exception e) {
      LOG.severe("Error processing balance request: " + e.getMessage());
      response.sendRedirect("menu?error=" + e.getMessage());
    }
  }

  private void renderSoldeView(HttpServletRequest request, HttpServletResponse response, User user, String error,
      Map<String, Object> balanceData)
      throws ServletException, IOException {

    // Create Thymeleaf context
    JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
    WebContext context = new WebContext(application.buildExchange(request, response));

    // Set template variables
    context.setVariable("userName", user.getName());
    context.setVariable("user", user);
    context.setVariable("error", error);
    context.setVariable("balanceData", balanceData);
    context.setVariable("userId", request.getParameter("userId"));
    context.setVariable("actionDateTime", request.getParameter("actionDateTime"));

    // Process template and write response
    response.setContentType("text/html;charset=UTF-8");
    thymeleafService.getTemplateEngine(getServletContext())
        .process("solde", context, response.getWriter());
  }
}
