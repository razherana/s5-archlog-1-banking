package mg.razherana.banking.common.entities;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true)
  private String name;

  @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
  private List<UserAdmin> users;

  @Column(nullable = false)
  private Integer niveau;

  public Department(Integer id, String name, Integer niveau) {
    this.id = id;
    this.name = name;
    this.niveau = niveau;
  }

  public Department() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getNiveau() {
    return niveau;
  }

  public void setNiveau(Integer niveau) {
    this.niveau = niveau;
  }

}
