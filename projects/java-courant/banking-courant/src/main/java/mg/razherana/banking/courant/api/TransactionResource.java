package mg.razherana.banking.courant.api;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mg.razherana.banking.courant.application.CompteCourantService;
import mg.razherana.banking.courant.application.TransactionService;
import mg.razherana.banking.courant.dto.ErrorDTO;
import mg.razherana.banking.courant.dto.MessageDTO;
import mg.razherana.banking.courant.dto.TransactionCourantDTO;
import mg.razherana.banking.courant.dto.requests.transactions.DepotRequest;
import mg.razherana.banking.courant.dto.requests.transactions.RetraitRequest;
import mg.razherana.banking.courant.dto.requests.transactions.TransfertRequest;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.TransactionCourant;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {
    private static final Logger LOG = Logger.getLogger(TransactionResource.class.getName());

    @EJB
    private TransactionService transactionService;

    @EJB
    private CompteCourantService compteCourantService;

    @GET
    public Response getAllTransactions() {
        try {
            List<TransactionCourant> transactions = transactionService.getAllTransactions();
            List<TransactionCourantDTO> transactionDTOs = transactions.stream()
                .map(TransactionCourantDTO::new)
                .collect(Collectors.toList());
            return Response.ok(transactionDTOs)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception e) {
            LOG.severe("Error getting all transactions: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/transactions");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getTransactionById(@PathParam("id") Integer id) {
        try {
            TransactionCourant transaction = transactionService.findById(id);
            if (transaction == null) {
                ErrorDTO error = new ErrorDTO("Transaction not found", 404, "Not Found", "/transactions/" + id);
                return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }
            TransactionCourantDTO transactionDTO = new TransactionCourantDTO(transaction);
            return Response.ok(transactionDTO)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception e) {
            LOG.severe("Error getting transaction by ID: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/transactions/" + id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        }
    }

    @GET
    @Path("/compte/{compteId}")
    public Response getTransactionsByCompte(@PathParam("compteId") Integer compteId) {
        try {
            CompteCourant compte = compteCourantService.findById(compteId);
            if (compte == null) {
                ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/transactions/compte/" + compteId);
                return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }
            
            List<TransactionCourant> transactions = transactionService.getTransactionsByCompte(compte);
            List<TransactionCourantDTO> transactionDTOs = transactions.stream()
                .map(TransactionCourantDTO::new)
                .collect(Collectors.toList());
            return Response.ok(transactionDTOs)
                .type(MediaType.APPLICATION_JSON)
                .build();
        } catch (Exception e) {
            LOG.severe("Error getting transactions by compte: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/transactions/compte/" + compteId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        }
    }

    @POST
    @Path("/depot")
    public Response depot(DepotRequest request) {
        try {
            if (request.getCompteId() == null || request.getMontant() == null) {
                ErrorDTO error = new ErrorDTO("Compte ID and montant are required", 400, "Bad Request", "/transactions/depot");
                return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }

            CompteCourant compte = compteCourantService.findById(request.getCompteId());
            if (compte == null) {
                ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/transactions/depot");
                return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }

            TransactionCourant transaction = transactionService.depot(
                compte, request.getMontant(), request.getDescription());
            
            TransactionCourantDTO transactionDTO = new TransactionCourantDTO(transaction);
            return Response.status(Response.Status.CREATED)
                .type(MediaType.APPLICATION_JSON)
                .entity(transactionDTO).build();
        } catch (IllegalArgumentException e) {
            LOG.warning("Invalid depot data: " + e.getMessage());
            ErrorDTO error = new ErrorDTO("Invalid data: " + e.getMessage(), 400, "Bad Request", "/transactions/depot");
            return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        } catch (Exception e) {
            LOG.severe("Error processing depot: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/transactions/depot");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        }
    }

    @POST
    @Path("/retrait")
    public Response retrait(RetraitRequest request) {
        try {
            if (request.getCompteId() == null || request.getMontant() == null) {
                ErrorDTO error = new ErrorDTO("Compte ID and montant are required", 400, "Bad Request", "/transactions/retrait");
                return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }

            CompteCourant compte = compteCourantService.findById(request.getCompteId());
            if (compte == null) {
                ErrorDTO error = new ErrorDTO("Compte not found", 404, "Not Found", "/transactions/retrait");
                return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }

            TransactionCourant transaction = transactionService.retrait(
                compte, request.getMontant(), request.getDescription());
            
            TransactionCourantDTO transactionDTO = new TransactionCourantDTO(transaction);
            return Response.status(Response.Status.CREATED)
                .type(MediaType.APPLICATION_JSON)
                .entity(transactionDTO).build();
        } catch (IllegalArgumentException e) {
            LOG.warning("Invalid retrait data: " + e.getMessage());
            ErrorDTO error = new ErrorDTO("Invalid data: " + e.getMessage(), 400, "Bad Request", "/transactions/retrait");
            return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        } catch (Exception e) {
            LOG.severe("Error processing retrait: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/transactions/retrait");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        }
    }

    @POST
    @Path("/transfert")
    public Response transfert(TransfertRequest request) {
        try {
            if (request.getCompteSourceId() == null || 
                request.getCompteDestinationId() == null || 
                request.getMontant() == null) {
                ErrorDTO error = new ErrorDTO("Source account ID, destination account ID and montant are required", 400, "Bad Request", "/transactions/transfert");
                return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }

            CompteCourant compteSource = compteCourantService.findById(request.getCompteSourceId());
            if (compteSource == null) {
                ErrorDTO error = new ErrorDTO("Source compte not found", 404, "Not Found", "/transactions/transfert");
                return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }

            CompteCourant compteDestination = compteCourantService.findById(request.getCompteDestinationId());
            if (compteDestination == null) {
                ErrorDTO error = new ErrorDTO("Destination compte not found", 404, "Not Found", "/transactions/transfert");
                return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error).build();
            }

            transactionService.transfert(compteSource, compteDestination, 
                request.getMontant(), request.getDescription());
            
            MessageDTO message = new MessageDTO("Transfer completed successfully");
            return Response.status(Response.Status.CREATED)
                .type(MediaType.APPLICATION_JSON)
                .entity(message).build();
        } catch (IllegalArgumentException e) {
            LOG.warning("Invalid transfert data: " + e.getMessage());
            ErrorDTO error = new ErrorDTO("Invalid data: " + e.getMessage(), 400, "Bad Request", "/transactions/transfert");
            return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        } catch (Exception e) {
            LOG.severe("Error processing transfert: " + e.getMessage());
            ErrorDTO error = new ErrorDTO(e.getMessage(), 500, "Internal Server Error", "/transactions/transfert");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
        }
    }
}
