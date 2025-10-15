using BankingDepot.Models.Entities;

namespace BankingDepot.Models.DTOs
{
  /// <summary>
  /// Data Transfer Object for CompteDepot entity.
  /// </summary>
  public class CompteDepotDTO
  {
    public int Id { get; set; }
    public int TypeCompteDepotId { get; set; }
    public string TypeCompteDepotNom { get; set; } = string.Empty;
    public decimal TauxInteret { get; set; }
    public int UserId { get; set; }
    public DateTime DateOuverture { get; set; }
    public DateTime DateEcheance { get; set; }
    public decimal Montant { get; set; }
    public bool EstRetire { get; set; }
    public DateTime? DateRetire { get; set; }
    public decimal? InteretCalcule { get; set; } // Calculated interest
    public decimal? MontantTotal { get; set; } // Montant + Interest

    public CompteDepotDTO() { }

    public CompteDepotDTO(CompteDepot entity)
    {
      Id = entity.Id;
      TypeCompteDepotId = entity.TypeCompteDepotId;
      TypeCompteDepotNom = entity.TypeCompteDepot?.Nom ?? string.Empty;
      TauxInteret = entity.TypeCompteDepot?.TauxInteret ?? 0;
      UserId = entity.UserId;
      DateOuverture = entity.DateOuverture;
      DateEcheance = entity.DateEcheance;
      Montant = entity.Montant;
      EstRetire = entity.EstRetire == 1;
      DateRetire = entity.DateRetire;
    }
  }
}