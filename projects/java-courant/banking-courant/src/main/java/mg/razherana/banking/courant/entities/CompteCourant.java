/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package mg.razherana.banking.courant.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Current account (Compte Courant) entity representing bank accounts in the
 * system.
 * 
 * <p>
 * This entity represents current accounts that belong to users. The account has
 * no stored balance field - the balance is calculated dynamically by summing
 * all
 * transactions where the account is involved as sender or receiver.
 * </p>
 * 
 * <p>
 * Each account has a monthly tax that accumulates over time and must be paid
 * before performing withdrawals or transfers.
 * </p>
 * 
 * @author Banking System
 * @version 1.0
 * @since 1.0
 * @see mg.razherana.banking.courant.entities.User
 * @see mg.razherana.banking.courant.entities.TransactionCourant
 * @see mg.razherana.banking.courant.application.compteCourantService.CompteCourantService
 * @see mg.razherana.banking.courant.dto.CompteCourantDTO
 */
@Entity
@Table(name = "compte_courants")
public class CompteCourant implements Serializable {
  /**
   * Unique identifier for the current account.
   * Auto-generated using database identity strategy.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Monthly tax amount for this account.
   * This amount is charged monthly and must be paid before withdrawals/transfers.
   * Stored with precision 10 and scale 2 for monetary values.
   */
  @Column(name = "taxe", nullable = false, precision = 10, scale = 2)
  private BigDecimal taxe;

  @Column(name = "user_id")
  private Integer userId;

  /**
   * The user who owns this current account.
   * User is not in the database so no foreign key constraint is enforced.
   */
  private transient User user;

  /**
   * Timestamp when this account was created.
   * Used for calculating accumulated taxes over time.
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /**
   * Gets the unique identifier of the current account.
   * 
   * @return the account ID, or null if not yet persisted
   */
  public Integer getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the current account.
   * 
   * @param id the account ID to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Gets the monthly tax amount for this account.
   * 
   * @return the monthly tax amount in BigDecimal format
   */
  public BigDecimal getTaxe() {
    return taxe;
  }

  /**
   * Sets the monthly tax amount for this account.
   * 
   * @param taxe the monthly tax amount to set
   * @throws IllegalArgumentException if taxe is negative
   */
  public void setTaxe(BigDecimal taxe) {
    this.taxe = taxe;
  }

  /**
   * Gets the user who owns this current account.
   * 
   * @return the account owner
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets the user who owns this current account.
   * 
   * @param user the account owner to set
   * @throws IllegalArgumentException if user is null
   */
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Gets the creation timestamp of this account.
   * 
   * @return the account creation date and time
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * Sets the creation timestamp of this account.
   * 
   * @param createdAt the account creation date and time to set
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Returns a string representation of the current account.
   * Excludes user details to prevent circular references.
   * 
   * @return a string representation containing id, tax amount, and creation date
   */
  @Override
  public String toString() {
    return "CompteCourant{" +
        "id=" + id +
        ", taxe=" + taxe +
        ", createdAt=" + createdAt +
        '}';
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }
}
