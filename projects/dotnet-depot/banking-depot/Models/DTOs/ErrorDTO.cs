namespace BankingDepot.Models.DTOs
{
  /// <summary>
  /// Error Data Transfer Object for API error responses.
  /// Provides a consistent error response format across all API endpoints.
  /// </summary>
  public class ErrorDTO
  {
    public string Message { get; set; } = string.Empty;
    public int Status { get; set; }
    public string Error { get; set; } = string.Empty;
    public string Path { get; set; } = string.Empty;
    public long Timestamp { get; set; }

    public ErrorDTO()
    {
      Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
    }

    public ErrorDTO(string message, int status, string error, string path)
    {
      Message = message;
      Status = status;
      Error = error;
      Path = path;
      Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
    }
  }
}