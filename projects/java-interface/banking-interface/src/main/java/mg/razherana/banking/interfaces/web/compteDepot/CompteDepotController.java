package mg.razherana.banking.interfaces.web.compteDepot;

import mg.razherana.banking.interfaces.application.compteDepotServices.CompteDepotService;
import mg.razherana.banking.interfaces.application.template.ThymeleafService;
import mg.razherana.banking.interfaces.dto.CompteDepotDTO;
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
 * Web Controller for deposit accounts listing.
 * Displays all deposit accounts for the logged-in user.
 */
@WebServlet("/comptes-depots")
public class CompteDepotController extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(CompteDepotController.class.getName());

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
      // Get deposit accounts for the user
      List<CompteDepotDTO> comptes = compteDepotService.getAccountsByUserId(user.getId());

      // Create Thymeleaf context
      JakartaServletWebApplication application = JakartaServletWebApplication
          .buildApplication(getServletContext());
      WebContext webContext = new WebContext(application.buildExchange(request, response));

      // Add variables to context
      webContext.setVariable("user", user);
      webContext.setVariable("comptes", comptes);
      webContext.setVariable("error", request.getParameter("error"));
      webContext.setVariable("success", request.getParameter("success"));

      // Process template
      response.setContentType("text/html;charset=UTF-8");
      thymeleafService.getTemplateEngine(getServletContext()).process("comptes-depots/list", webContext,
          response.getWriter());

    } catch (Exception e) {
      LOG.severe("Error loading deposit accounts: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/comptes-depots?error=" +
          java.net.URLEncoder.encode("Erreur lors du chargement des comptes",
              java.nio.charset.StandardCharsets.UTF_8));
    }
  }
}