package mg.razherana.banking.pret.api;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mg.razherana.banking.pret.application.ComptePretService;
import mg.razherana.banking.pret.dto.*;
import mg.razherana.banking.pret.entities.ComptePret;
import mg.razherana.banking.pret.entities.TypeComptePret;
import mg.razherana.banking.pret.entities.Echeance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/comptes-pret")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ComptePretResource {
  private static final Logger LOG = Logger.getLogger(ComptePretResource.class.getName());

  @EJB
  private ComptePretService comptePretService;

  /**
   * Creates a new loan account.
   */
  @POST
  public Response createLoan(CreateComptePretRequest request) {
    try {
      if (request == null) {
        ErrorDTO error = new ErrorDTO("Request body cannot be null", 400, "Bad Request", "/comptes-pret");
        return Response.status(400).entity(error).build();
      }

      ComptePret loan = comptePretService.createLoan(
          request.getUserId(),
          request.getTypeComptePretId(),
          request.getMontant(),
          request.getDateDebut(),
          request.getDateFin());

      ComptePretDTO loanDTO = new ComptePretDTO(loan);
      return Response.status(201).entity(loanDTO).build();

    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request", "/comptes-pret");
        return Response.status(400).entity(error).build();
      } else {
        LOG.severe("EJB error creating loan: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/comptes-pret");
        return Response.status(500).entity(error).build();
      }
    } catch (IllegalArgumentException e) {
      LOG.warning("Invalid argument: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 400, "Bad Request", "/comptes-pret");
      return Response.status(400).entity(error).build();
    } catch (Exception e) {
      LOG.severe("Unexpected error creating loan: " + e.getMessage());
      ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/comptes-pret");
      return Response.status(500).entity(error).build();
    }
  }

  /**
   * Gets a loan account by ID.
   */
  @GET
  @Path("/{id}")
  public Response getLoanById(@PathParam("id") Integer id) {
    try {
      ComptePret loan = comptePretService.findById(id);
      if (loan == null) {
        ErrorDTO error = new ErrorDTO("Loan account not found", 404, "Not Found", "/comptes-pret/" + id);
        return Response.status(404).entity(error).build();
      }

      ComptePretDTO loanDTO = new ComptePretDTO(loan);
      return Response.ok(loanDTO).build();

    } catch (IllegalArgumentException e) {
      LOG.warning("Invalid argument: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 400, "Bad Request", "/comptes-pret/" + id);
      return Response.status(400).entity(error).build();
    } catch (Exception e) {
      LOG.severe("Unexpected error getting loan: " + e.getMessage());
      ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/comptes-pret/" + id);
      return Response.status(500).entity(error).build();
    }
  }

  /**
   * Gets all loan accounts for a user.
   */
  @GET
  @Path("/user/{userId}")
  public Response getLoansByUserId(@PathParam("userId") Integer userId) {
    try {
      List<ComptePret> loans = comptePretService.getLoansByUserId(userId);
      List<ComptePretDTO> loanDTOs = loans.stream()
          .map(ComptePretDTO::new)
          .collect(Collectors.toList());
      return Response.ok(loanDTOs).build();

    } catch (IllegalArgumentException e) {
      LOG.warning("Invalid argument: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 400, "Bad Request", "/comptes-pret/user/" + userId);
      return Response.status(400).entity(error).build();
    } catch (Exception e) {
      LOG.severe("Unexpected error getting user loans: " + e.getMessage());
      ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error",
          "/comptes-pret/user/" + userId);
      return Response.status(500).entity(error).build();
    }
  }

  /**
   * Gets payment status for a loan account.
   */
  @GET
  @Path("/{id}/payment-status")
  public Response getPaymentStatus(@PathParam("id") Integer id,
      @QueryParam("actionDateTime") String actionDateTimeStr) {
    try {
      LocalDateTime actionDateTime = null;
      if (actionDateTimeStr != null && !actionDateTimeStr.trim().isEmpty()) {
        try {
          actionDateTime = LocalDateTime.parse(actionDateTimeStr);
        } catch (Exception e) {
          ErrorDTO error = new ErrorDTO("Invalid actionDateTime format. Use ISO format: yyyy-MM-ddTHH:mm:ss",
              400, "Bad Request", "/comptes-pret/" + id + "/payment-status");
          return Response.status(400).entity(error).build();
        }
      }

      PaymentStatusDTO status = comptePretService.getPaymentStatus(id, actionDateTime);
      return Response.ok(status).build();

    } catch (IllegalArgumentException e) {
      LOG.warning("Invalid argument: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 400, "Bad Request", "/comptes-pret/" + id + "/payment-status");
      return Response.status(400).entity(error).build();
    } catch (Exception e) {
      LOG.severe("Unexpected error getting payment status: " + e.getMessage());
      ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error",
          "/comptes-pret/" + id + "/payment-status");
      return Response.status(500).entity(error).build();
    }
  }

  /**
   * Gets payment history for a loan account.
   */
  @GET
  @Path("/{id}/payment-history")
  public Response getPaymentHistory(@PathParam("id") Integer id) {
    try {
      List<Echeance> payments = comptePretService.getPaymentHistory(id);
      List<EcheanceDTO> paymentDTOs = payments.stream()
          .map(payment -> new EcheanceDTO(
              payment.getId(),
              payment.getCompteId(),
              payment.getMontant(),
              payment.getDateEcheance()))
          .collect(Collectors.toList());
      return Response.ok(paymentDTOs).build();

    } catch (IllegalArgumentException e) {
      LOG.warning("Invalid argument: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 400, "Bad Request", "/comptes-pret/" + id + "/payment-history");
      return Response.status(400).entity(error).build();
    } catch (Exception e) {
      LOG.severe("Unexpected error getting payment history: " + e.getMessage());
      ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error",
          "/comptes-pret/" + id + "/payment-history");
      return Response.status(500).entity(error).build();
    }
  }

  /**
   * Makes a payment for a loan account.
   */
  @POST
  @Path("/make-payment")
  public Response makePayment(MakePaymentRequest request) {
    try {
      if (request == null) {
        ErrorDTO error = new ErrorDTO("Request body cannot be null", 400, "Bad Request", "/comptes-pret/make-payment");
        return Response.status(400).entity(error).build();
      }

      Echeance payment = comptePretService.makePayment(
          request.getCompteId(),
          request.getMontant(),
          request.getActionDateTime());

      EcheanceDTO paymentDTO = new EcheanceDTO(
          payment.getId(),
          payment.getCompteId(),
          payment.getMontant(),
          payment.getDateEcheance());
      return Response.status(201).entity(paymentDTO).build();

    } catch (EJBException e) {
      if (e.getCausedByException() instanceof IllegalArgumentException) {
        IllegalArgumentException cause = (IllegalArgumentException) e.getCausedByException();
        LOG.warning("Invalid data from EJB: " + cause.getMessage());
        ErrorDTO error = new ErrorDTO("Invalid data: " + cause.getMessage(), 400, "Bad Request",
            "/comptes-pret/make-payment");
        return Response.status(400).entity(error).build();
      } else {
        LOG.severe("EJB error making payment: " + e.getMessage());
        ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error",
            "/comptes-pret/make-payment");
        return Response.status(500).entity(error).build();
      }
    } catch (IllegalArgumentException e) {
      LOG.warning("Invalid argument: " + e.getMessage());
      ErrorDTO error = new ErrorDTO(e.getMessage(), 400, "Bad Request", "/comptes-pret/make-payment");
      return Response.status(400).entity(error).build();
    } catch (Exception e) {
      LOG.severe("Unexpected error making payment: " + e.getMessage());
      ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error",
          "/comptes-pret/make-payment");
      return Response.status(500).entity(error).build();
    }
  }

  /**
   * Gets all loan types.
   */
  @GET
  @Path("/types")
  public Response getAllLoanTypes() {
    try {
      List<TypeComptePret> loanTypes = comptePretService.getAllLoanTypes();
      return Response.ok(loanTypes).build();

    } catch (Exception e) {
      LOG.severe("Unexpected error getting loan types: " + e.getMessage());
      ErrorDTO error = new ErrorDTO("Internal server error", 500, "Internal Server Error", "/comptes-pret/types");
      return Response.status(500).entity(error).build();
    }
  }
}