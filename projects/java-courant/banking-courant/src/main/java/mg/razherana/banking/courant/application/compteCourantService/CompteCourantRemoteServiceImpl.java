package mg.razherana.banking.courant.application.compteCourantService;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import mg.razherana.banking.common.entities.UserAdmin;
import mg.razherana.banking.courant.entities.CompteCourant;
import mg.razherana.banking.courant.entities.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Remote implementation of the CompteCourantService that delegates to the local
 * service.
 * 
 * <p>
 * This stateless EJB provides remote access to current account operations by
 * delegating all calls to the local CompteCourantServiceImpl. This allows
 * other applications to access the account services via EJB remote calls.
 * </p>
 * 
 * <p>
 * The implementation uses dependency injection to access the local service
 * and simply forwards all method calls, making the local functionality
 * available remotely without duplicating business logic.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see CompteCourantRemoteService
 * @see CompteCourantService
 * @see CompteCourantServiceImpl
 */
@Stateless
public class CompteCourantRemoteServiceImpl implements CompteCourantRemoteService {

  @EJB
  private CompteCourantService compteCourantService;

  @Override
  public User findUser(Integer userId) {
    return compteCourantService.findUser(userId);
  }

  @Override
  public CompteCourant create(User user, BigDecimal taxe, LocalDateTime actionDateTime) {
    return compteCourantService.create(user, taxe, actionDateTime);
  }

  @Override
  public List<CompteCourant> getComptes() {
    return compteCourantService.getComptes();
  }

  @Override
  public CompteCourant findById(Integer id) {
    return compteCourantService.findById(id);
  }

  @Override
  public List<CompteCourant> getComptesByUser(User user) {
    return compteCourantService.getComptesByUser(user);
  }

  @Override
  public List<CompteCourant> getComptesByUserId(Integer userId) {
    return compteCourantService.getComptesByUserId(userId);
  }

  @Override
  public BigDecimal calculateSolde(CompteCourant compte) {
    return compteCourantService.calculateSolde(compte);
  }

  @Override
  public BigDecimal calculateSolde(CompteCourant compte, LocalDateTime actionDateTime) {
    return compteCourantService.calculateSolde(compte, actionDateTime);
  }

  @Override
  public BigDecimal calculateTotalSoldeByUserId(Integer userId, LocalDateTime actionDateTime) {
    return compteCourantService.calculateTotalSoldeByUserId(userId, actionDateTime);
  }

  @Override
  public void updateTaxe(CompteCourant compte, BigDecimal nouvelleTaxe) {
    compteCourantService.updateTaxe(compte, nouvelleTaxe);
  }

  @Override
  public void delete(Integer id) {
    compteCourantService.delete(id);
  }

  @Override
  public BigDecimal getTaxPaidTotal(CompteCourant compte) {
    return compteCourantService.getTaxPaidTotal(compte);
  }

  @Override
  public BigDecimal getTaxPaidDate(CompteCourant compte, LocalDateTime actionDateTime) {
    return compteCourantService.getTaxPaidDate(compte, actionDateTime);
  }

  @Override
  public boolean isTaxPaid(CompteCourant compte, LocalDateTime actionDateTime) {
    return compteCourantService.isTaxPaid(compte, actionDateTime);
  }

  @Override
  public BigDecimal getTaxToPay(CompteCourant compte, LocalDateTime actionDateTime) {
    return compteCourantService.getTaxToPay(compte, actionDateTime);
  }

  @Override
  public boolean hasAuthorization(UserAdmin userAdmin, String operationType, String tableName) {
    return compteCourantService.hasAuthorization(userAdmin, operationType, tableName);
  }
}