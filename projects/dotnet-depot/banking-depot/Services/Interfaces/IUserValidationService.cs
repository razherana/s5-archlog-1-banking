namespace BankingDepot.Services.Interfaces
{
  /// <summary>
  /// Service interface for external user validation.
  /// Communicates with the Java current account service for user verification.
  /// </summary>
  public interface IUserValidationService
  {
    /// <summary>
    /// Validates if a user exists by calling the Java current account service.
    /// </summary>
    /// <param name="userId">The user ID to validate</param>
    /// <returns>True if the user exists, false otherwise</returns>
    Task<bool> ValidateUserExistsAsync(int userId);

    /// <summary>
    /// Gets user information from the Java current account service.
    /// </summary>
    /// <param name="userId">The user ID to retrieve</param>
    /// <returns>User information or null if not found</returns>
    Task<object?> GetUserAsync(int userId);
  }
}