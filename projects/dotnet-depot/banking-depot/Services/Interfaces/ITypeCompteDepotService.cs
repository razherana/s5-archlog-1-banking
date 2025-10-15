using BankingDepot.Models.Entities;

namespace BankingDepot.Services.Interfaces
{
  /// <summary>
  /// Service interface for TypeCompteDepot operations.
  /// </summary>
  public interface ITypeCompteDepotService
  {
    /// <summary>
    /// Gets all deposit account types.
    /// </summary>
    /// <returns>List of all TypeCompteDepot entities</returns>
    Task<List<TypeCompteDepot>> GetAllAsync();

    /// <summary>
    /// Gets a deposit account type by ID.
    /// </summary>
    /// <param name="id">The type ID</param>
    /// <returns>The TypeCompteDepot entity or null if not found</returns>
    Task<TypeCompteDepot?> GetByIdAsync(int id);

    /// <summary>
    /// Creates a new deposit account type.
    /// </summary>
    /// <param name="nom">The name of the account type</param>
    /// <param name="tauxInteret">The interest rate</param>
    /// <returns>The created TypeCompteDepot entity</returns>
    Task<TypeCompteDepot> CreateAsync(string nom, decimal tauxInteret);

    /// <summary>
    /// Updates an existing deposit account type.
    /// </summary>
    /// <param name="id">The type ID to update</param>
    /// <param name="nom">The new name</param>
    /// <param name="tauxInteret">The new interest rate</param>
    /// <returns>The updated TypeCompteDepot entity or null if not found</returns>
    Task<TypeCompteDepot?> UpdateAsync(int id, string nom, decimal tauxInteret);

    /// <summary>
    /// Deletes a deposit account type.
    /// </summary>
    /// <param name="id">The type ID to delete</param>
    /// <returns>True if deleted successfully, false if not found</returns>
    Task<bool> DeleteAsync(int id);
  }
}