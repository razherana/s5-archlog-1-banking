package mg.razherana.banking.common.services.userServices;

import java.math.BigDecimal;
import java.util.List;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateful;
import mg.razherana.banking.common.entities.User;

@Stateful
public class UserRemoteServiceImpl implements UserRemoteService {

  @EJB
  private UserService userService;

  @Override
  public User findUserById(Integer userId) {
    return userService.findUserById(userId);
  }

  @Override
  public User findUserByEmail(String email) {
    return userService.findUserByEmail(email);
  }

  @Override
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  @Override
  public User createUser(String name, String email, String password) {
    return userService.createUser(name, email, password);
  }

  @Override
  public User updateUser(Integer userId, String name, String email, String password) {
    return userService.updateUser(userId, name, email, password);
  }

  @Override
  public void deleteUser(Integer userId) {
    userService.deleteUser(userId);
  }

  @Override
  public User authenticateUser(String email, String password) {
    return userService.authenticateUser(email, password);
  }

  @Override
  public BigDecimal calculateTotalBalanceAcrossModules(Integer userId, String actionDateTime) {
    return userService.calculateTotalBalanceAcrossModules(userId, actionDateTime);
  }

  @Override
  public BigDecimal getCurrentAccountBalance(Integer userId, String actionDateTime) {
    return userService.getCurrentAccountBalance(userId, actionDateTime);
  }

  @Override
  public BigDecimal getLoanBalance(Integer userId, String actionDateTime) {
    return userService.getLoanBalance(userId, actionDateTime);
  }

  @Override
  public BigDecimal getDepositBalance(Integer userId, String actionDateTime) {
    return userService.getDepositBalance(userId, actionDateTime);
  }
  
}
