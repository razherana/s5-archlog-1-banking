package mg.razherana.banking.courant.application.virementService;

import java.time.LocalDateTime;
import java.util.List;

import mg.razherana.banking.courant.entities.TransactionCourant;
import mg.razherana.banking.courant.entities.TransactionEtat;

public interface VirementService {
  public TransactionCourant createVirement(Integer userAdminId, Integer compteSourceId, Integer compteDestinationId,
      String montantStr, String change, LocalDateTime actionDateTime);

  public TransactionCourant validateVirement(Integer userAdminId, Integer virementId, Integer etat,
      LocalDateTime actionDateTime);

    public List<TransactionEtat> getEtatsByTransaction(Integer transactionId);
}
