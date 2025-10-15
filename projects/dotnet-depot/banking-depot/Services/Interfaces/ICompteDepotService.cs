using BankingDepot.Models.Entities;

namespace BankingDepot.Services.Interfaces
{
  /// <summary>
  /// Service interface for CompteDepot operations.
  /// </summary>
  public interface ICompteDepotService
  {
    /// <summary>
    /// Gets all deposit accounts.
    /// </summary>
    /// <returns>List of all CompteDepot entities</returns>
    Task<List<CompteDepot>> GetAllAsync();

    /// <summary>
    /// Gets a deposit account by ID.
    /// </summary>
    /// <param name="id">The account ID</param>
    /// <returns>The CompteDepot entity or null if not found</returns>
    Task<CompteDepot?> GetByIdAsync(int id);

    /// <summary>
    /// Gets all deposit accounts for a specific user.
    /// </summary>
    /// <param name="userId">The user ID</param>
    /// <returns>List of CompteDepot entities for the user</returns>
    Task<List<CompteDepot>> GetByUserIdAsync(int userId);

    /// <summary>
    /// Creates a new deposit account.
    /// </summary>
    /// <param name="typeCompteDepotId">The account type ID</param>
    /// <param name="userId">The user ID</param>
    /// <param name="dateEcheance">The maturity date</param>
    /// <param name="montant">The deposit amount</param>
    /// <param name="actionDateTime">The action date time (for backtracking)</param>
    /// <returns>The created CompteDepot entity</returns>
    Task<CompteDepot> CreateAsync(int typeCompteDepotId, int userId, DateTime dateEcheance, decimal montant, DateTime? actionDateTime = null);

    /// <summary>
    /// Withdraws from a deposit account at maturity.
    /// </summary>
    /// <param name="id">The account ID</param>
    /// <param name="actionDateTime">The action date time (for backtracking)</param>
    /// <returns>Tuple containing the account and calculated interest</returns>
    Task<(CompteDepot compte, decimal interest)> WithdrawAsync(int id, DateTime? actionDateTime = null);

    /// <summary>
    /// Calculates simple interest for a deposit account.
    /// </summary>
    /// <param name="compte">The deposit account</param>
    /// <param name="actionDateTime">The calculation reference date (for backtracking)</param>
    /// <returns>The calculated simple interest</returns>
    decimal CalculateInterest(CompteDepot compte, DateTime? actionDateTime = null);

    /// <summary>
    /// Checks if a deposit account can be withdrawn at the given date.
    /// </summary>
    /// <param name="compte">The deposit account</param>
    /// <param name="actionDateTime">The action date time</param>
    /// <returns>True if withdrawal is allowed, false otherwise</returns>
    bool CanWithdraw(CompteDepot compte, DateTime actionDateTime);
  }
}