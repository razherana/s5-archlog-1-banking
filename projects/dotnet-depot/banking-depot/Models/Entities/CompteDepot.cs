using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BankingDepot.Models.Entities
{
  /// <summary>
  /// Entity representing a deposit account (compte à terme).
  /// Maps to the 'compte_depots' table in the database.
  /// </summary>
  [Table("compte_depots")]
  public class CompteDepot
  {
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    [Column("id")]
    public int Id { get; set; }

    [Required]
    [Column("type_compte_depot_id")]
    public int TypeCompteDepotId { get; set; }

    [Required]
    [Column("user_id")]
    public int UserId { get; set; }

    [Required]
    [Column("date_ouverture")]
    public DateTime DateOuverture { get; set; }

    [Required]
    [Column("date_echeance")]
    public DateTime DateEcheance { get; set; }

    [Required]
    [Column("montant", TypeName = "decimal(15,2)")]
    public decimal Montant { get; set; }

    [Required]
    [Column("est_retire")]
    public int EstRetire { get; set; } = 0; // 0: non, 1: oui

    [Column("date_retire")]
    public DateTime? DateRetire { get; set; }

    // Navigation properties
    [ForeignKey("TypeCompteDepotId")]
    public virtual TypeCompteDepot TypeCompteDepot { get; set; } = null!;

    // Helper properties
    [NotMapped]
    public bool IsWithdrawn => EstRetire == 1;

    public bool CanWithdraw(DateTime actionDateTime) => actionDateTime >= DateEcheance && !IsWithdrawn;
  }
}