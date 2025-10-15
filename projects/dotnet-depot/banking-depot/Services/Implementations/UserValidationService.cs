using BankingDepot.Services.Interfaces;
using System.Text.Json;

namespace BankingDepot.Services.Implementations
{
  /// <summary>
  /// Service implementation for validating users via external Java service.
  /// Communicates with the Java current account service at 127.0.0.2:8080.
  /// </summary>
  public class UserValidationService : IUserValidationService
  {
    private readonly HttpClient _httpClient;
    private readonly ILogger<UserValidationService> _logger;
    private readonly string _javaServiceBaseUrl;

    public UserValidationService(HttpClient httpClient, ILogger<UserValidationService> logger, IConfiguration configuration)
    {
      _httpClient = httpClient;
      _logger = logger;
      _javaServiceBaseUrl = configuration.GetValue<string>("JavaService:BaseUrl") ?? "http://127.0.0.2:8080/api";
    }

    public async Task<bool> ValidateUserExistsAsync(int userId)
    {
      try
      {
        _logger.LogInformation("Validating user existence for userId: {UserId}", userId);

        var response = await _httpClient.GetAsync($"{_javaServiceBaseUrl}/users/{userId}");

        if (response.IsSuccessStatusCode)
        {
          _logger.LogInformation("User {UserId} exists", userId);
          return true;
        }
        else if (response.StatusCode == System.Net.HttpStatusCode.NotFound)
        {
          _logger.LogWarning("User {UserId} not found", userId);
          return false;
        }
        else
        {
          _logger.LogError("Error validating user {UserId}: {StatusCode}", userId, response.StatusCode);
          return false;
        }
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Exception occurred while validating user {UserId}", userId);
        return false;
      }
    }

    public async Task<object?> GetUserAsync(int userId)
    {
      try
      {
        _logger.LogInformation("Getting user information for userId: {UserId}", userId);

        var response = await _httpClient.GetAsync($"{_javaServiceBaseUrl}/users/{userId}");

        if (response.IsSuccessStatusCode)
        {
          var content = await response.Content.ReadAsStringAsync();
          var user = JsonSerializer.Deserialize<object>(content);
          _logger.LogInformation("Successfully retrieved user {UserId}", userId);
          return user;
        }
        else
        {
          _logger.LogWarning("Failed to retrieve user {UserId}: {StatusCode}", userId, response.StatusCode);
          return null;
        }
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Exception occurred while getting user {UserId}", userId);
        return null;
      }
    }
  }
}