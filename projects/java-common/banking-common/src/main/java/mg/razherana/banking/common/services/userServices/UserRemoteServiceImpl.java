package mg.razherana.banking.common.services.userServices;

import java.math.BigDecimal;
import java.util.List;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateful;
import mg.razherana.banking.common.entities.ActionRole;
import mg.razherana.banking.common.entities.User;
import mg.razherana.banking.common.entities.UserAdmin;

@Stateful
public class UserRemoteServiceImpl implements UserRemoteService {

  @EJB
  private UserService userService;

  @Override
  public User findUserById(Integer userId) {
    return userService.findUserById(userId);
  }

  @Override
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  @Override
  public User createUser(String name) {
    return userService.createUser(name);
  }

  @Override
  public User updateUser(Integer userId, String name) {
    return userService.updateUser(userId, name);
  }

  @Override
  public void deleteUser(Integer userId) {
    userService.deleteUser(userId);
  }

  @Override
  public UserAdmin authenticateUserAdmin(String email, String password) {
    return userService.authenticateUserAdmin(email, password);
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

  @Override
  public boolean hasAuthorization(Integer userId, String tableName, String action) {
    return userService.hasAuthorization(userId, tableName, action);
  }

  @Override
  public UserAdmin findUserAdminByEmail(String email) {
    return userService.findUserAdminByEmail(email);
  }

  @Override
  public UserAdmin findUserAdminById(Integer userAdminId) {
    return userService.findUserAdminById(userAdminId);
  }

  @Override
  public List<ActionRole> getActionRoleByRole(Integer role) {
    return userService.getActionRoleByRole(role);
  }

  @Override
  public UserAdmin getAuthedUser() {
    return userService.getAuthedUser();
  }

  @Override
  public UserAdmin createUserAdmin(String email, String password, int role) {
    return userService.createUserAdmin(email, password, role);
  }

}
