using System.ComponentModel.DataAnnotations;

namespace BankingDepot.Models.DTOs.Requests
{
  /// <summary>
  /// Request DTO for creating a new TypeCompteDepot.
  /// </summary>
  public class CreateTypeCompteDepotRequest
  {
    [Required(ErrorMessage = "Le nom est obligatoire")]
    [StringLength(255, ErrorMessage = "Le nom ne peut pas dépasser 255 caractères")]
    public string Nom { get; set; } = string.Empty;

    [Required(ErrorMessage = "Le taux d'intérêt est obligatoire")]
    [Range(0.0001, 1.0000, ErrorMessage = "Le taux d'intérêt doit être entre 0.0001 et 1.0000")]
    public decimal TauxInteret { get; set; }
  }
}