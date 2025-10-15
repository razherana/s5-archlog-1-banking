using BankingDepot.Models.Entities;

namespace BankingDepot.Models.DTOs
{
  /// <summary>
  /// Data Transfer Object for TypeCompteDepot entity.
  /// </summary>
  public class TypeCompteDepotDTO
  {
    public int Id { get; set; }
    public string Nom { get; set; } = string.Empty;
    public decimal TauxInteret { get; set; }

    public TypeCompteDepotDTO() { }

    public TypeCompteDepotDTO(TypeCompteDepot entity)
    {
      Id = entity.Id;
      Nom = entity.Nom;
      TauxInteret = entity.TauxInteret;
    }
  }
}