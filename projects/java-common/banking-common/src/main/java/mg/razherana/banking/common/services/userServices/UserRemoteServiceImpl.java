package mg.razherana.banking.common.services.userServices;

import java.math.BigDecimal;
import java.util.HashMap;
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
  public User findUserById(UserAdmin userAdmin, Integer userId) {
    return userService.findUserById(userAdmin, userId);
  }

  @Override
  public List<User> getAllUsers(UserAdmin userAdmin) {
    return userService.getAllUsers(userAdmin);
  }

  @Override
  public User createUser(UserAdmin userAdmin, String name) {
    return userService.createUser(userAdmin, name);
  }

  @Override
  public User updateUser(UserAdmin userAdmin, Integer userId, String name) {
    return userService.updateUser(userAdmin, userId, name);
  }

  @Override
  public void deleteUser(UserAdmin userAdmin, Integer userId) {
    userService.deleteUser(userAdmin, userId);
  }

  @Override
  public UserAdmin authenticateUserAdmin(String email, String password) {
    return userService.authenticateUserAdmin(email, password);
  }

  @Override
  public BigDecimal calculateTotalBalanceAcrossModules(UserAdmin userAdmin, Integer userId, String actionDateTime) {
    return userService.calculateTotalBalanceAcrossModules(userAdmin, userId, actionDateTime);
  }

  @Override
  public BigDecimal getCurrentAccountBalance(UserAdmin userAdmin, Integer userId, String actionDateTime) {
    return userService.getCurrentAccountBalance(userAdmin, userId, actionDateTime);
  }

  @Override
  public BigDecimal getLoanBalance(UserAdmin userAdmin, Integer userId, String actionDateTime) {
    return userService.getLoanBalance(userAdmin, userId, actionDateTime);
  }

  @Override
  public BigDecimal getDepositBalance(UserAdmin userAdmin, Integer userId, String actionDateTime) {
    return userService.getDepositBalance(userAdmin, userId, actionDateTime);
  }

  @Override
  public UserAdmin findUserAdminByEmail(String email) {
    return userService.findUserAdminByEmail(email);
  }

  @Override
  public UserAdmin findUserAdminById(UserAdmin userAdmin, Integer userAdminId) {
    return userService.findUserAdminById(userAdmin, userAdminId);
  }

  @Override
  public List<ActionRole> getActionRoleByRole(Integer role) {
    return userService.getActionRoleByRole(role);
  }

  @Override
  public UserAdmin createUserAdmin(UserAdmin userAdmin, String email, String password, int role) {
    return userService.createUserAdmin(userAdmin, email, password, role);
  }

  @Override
  public java.util.Map<Integer, String> getAllUsersForDropdown(UserAdmin userAdmin) {
    return userService.getAllUsersForDropdown(userAdmin);
  }

  @Override
  public HashMap<UserAdmin, List<ActionRole>> getAllUserAdminsWithDepAndRoles(UserAdmin userAdmin) {
    return userService.getAllUserAdminsWithDepAndRoles(userAdmin);
  }

  @Override
  public boolean hasAuthorization(UserAdmin userAdmin, String operationType, String tableName) {
    return userService.hasAuthorization(userAdmin, operationType, tableName);
  }
}
