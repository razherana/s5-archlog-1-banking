using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BankingDepot.Models.Entities
{
  /// <summary>
  /// Entity representing a deposit account type with its interest rate.
  /// Maps to the 'type_compte_depots' table in the database.
  /// </summary>
  [Table("type_compte_depots")]
  public class TypeCompteDepot
  {
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    [Column("id")]
    public int Id { get; set; }

    [Required]
    [StringLength(255)]
    [Column("nom")]
    public string Nom { get; set; } = string.Empty;

    [Required]
    [Column("taux_interet", TypeName = "decimal(5,4)")]
    public decimal TauxInteret { get; set; }

    // Navigation property
    public virtual ICollection<CompteDepot> ComptesDepots { get; set; } = new List<CompteDepot>();
  }
}