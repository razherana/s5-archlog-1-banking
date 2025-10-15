namespace BankingDepot.Models.DTOs.Responses
{
  /// <summary>
  /// Response DTO for withdrawal operations.
  /// </summary>
  public class WithdrawResponse
  {
    public int CompteDepotId { get; set; }
    public decimal MontantInitial { get; set; }
    public decimal InteretCalcule { get; set; }
    public decimal MontantTotal { get; set; }
    public DateTime DateRetire { get; set; }
    public string Message { get; set; } = string.Empty;

    public WithdrawResponse(int compteDepotId, decimal montantInitial, decimal interetCalcule, DateTime dateRetire)
    {
      CompteDepotId = compteDepotId;
      MontantInitial = montantInitial;
      InteretCalcule = interetCalcule;
      MontantTotal = montantInitial + interetCalcule;
      DateRetire = dateRetire;
      Message = $"Retrait effectué avec succès. Montant total: {MontantTotal:F2} MGA (Initial: {MontantInitial:F2} + Intérêts: {InteretCalcule:F2})";
    }
  }
}