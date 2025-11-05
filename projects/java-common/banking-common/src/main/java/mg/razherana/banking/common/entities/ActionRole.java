package mg.razherana.banking.common.entities;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table(name = "action_roles")
public class ActionRole implements Serializable {

  public static enum Action {
    CREATE,
    READ,
    UPDATE,
    DELETE;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private Integer role;

  @Column(name = "table_name", nullable = false)
  private String tableName;

  @Column(name = "action", nullable = false)
  private String action;

  public ActionRole() {
  }

  public ActionRole(Integer id, Integer role, String tableName, String action) {
    this.id = id;
    this.role = role;
    this.tableName = tableName;
    this.action = action;
  }

  public Action getActionEnum() {
    return Action.valueOf(action.toUpperCase());
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getRole() {
    return role;
  }

  public void setRole(Integer role) {
    this.role = role;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  @Override
  public String toString() {
    return "ActionRole [role=" + role + ", tableName=" + tableName + ", action=" + action + "]";
  }
}
