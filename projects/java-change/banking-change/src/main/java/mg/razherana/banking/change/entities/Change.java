package mg.razherana.banking.change.entities;

import java.time.LocalDateTime;

public class Change {
  private String name;
  private double value;
  private LocalDateTime dateStart;
  private LocalDateTime dateEnd;

  public Change(String name, double value, LocalDateTime dateStart, LocalDateTime dateEnd) {
    this.name = name;
    this.value = value;
    this.dateStart = dateStart;
    this.dateEnd = dateEnd;
  }

  public LocalDateTime getDateStart() {
    return dateStart;
  }

  public void setDateStart(LocalDateTime dateStart) {
    this.dateStart = dateStart;
  }

  public LocalDateTime getDateEnd() {
    return dateEnd;
  }

  public void setDateEnd(LocalDateTime dateEnd) {
    this.dateEnd = dateEnd;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

}