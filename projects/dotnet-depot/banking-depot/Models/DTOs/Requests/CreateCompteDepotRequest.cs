using System.ComponentModel.DataAnnotations;

namespace BankingDepot.Models.DTOs.Requests
{
  /// <summary>
  /// Request DTO for creating a new CompteDepot.
  /// </summary>
  public class CreateCompteDepotRequest
  {
    [Required(ErrorMessage = "L'ID du type de compte est obligatoire")]
    public int TypeCompteDepotId { get; set; }

    [Required(ErrorMessage = "L'ID de l'utilisateur est obligatoire")]
    public int UserId { get; set; }

    [Required(ErrorMessage = "La date d'échéance est obligatoire")]
    public DateTime DateEcheance { get; set; }

    [Required(ErrorMessage = "Le montant est obligatoire")]
    [Range(0.01, double.MaxValue, ErrorMessage = "Le montant doit être positif")]
    public decimal Montant { get; set; }

    public DateTime? ActionDateTime { get; set; } // For backtracking support
  }
}