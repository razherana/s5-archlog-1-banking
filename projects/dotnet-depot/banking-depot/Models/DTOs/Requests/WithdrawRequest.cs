using System.ComponentModel.DataAnnotations;

namespace BankingDepot.Models.DTOs.Requests
{
  /// <summary>
  /// Request DTO for withdrawing from a CompteDepot.
  /// </summary>
  public class WithdrawRequest
  {
    public DateTime? ActionDateTime { get; set; } // For backtracking support
  }
}