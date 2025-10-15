using BankingDepot.Models.DTOs;
using BankingDepot.Models.DTOs.Requests;
using BankingDepot.Models.DTOs.Responses;
using BankingDepot.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace BankingDepot.Controllers
{
  /// <summary>
  /// REST API Controller for CompteDepot operations.
  /// Manages deposit accounts (comptes à terme) including withdrawals.
  /// </summary>
  [ApiController]
  [Route("api/[controller]")]
  [Produces("application/json")]
  public class ComptesDepotsController(
      ICompteDepotService compteDepotService,
      ILogger<ComptesDepotsController> logger) : ControllerBase
  {
    private readonly ICompteDepotService _compteDepotService = compteDepotService;
    private readonly ILogger<ComptesDepotsController> _logger = logger;

    /// <summary>
    /// Gets all deposit accounts.
    /// </summary>
    /// <returns>List of all CompteDepot DTOs</returns>
    [HttpGet]
    public async Task<ActionResult<List<CompteDepotDTO>>> GetAll()
    {
      try
      {
        var comptes = await _compteDepotService.GetAllAsync();
        var dtos = comptes.Select(c => CreateCompteDepotDTO(c)).ToList();
        return Ok(dtos);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error retrieving all CompteDepot entities");
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }

    /// <summary>
    /// Gets a deposit account by ID.
    /// </summary>
    /// <param name="id">The account ID</param>
    /// <returns>CompteDepot DTO or 404 if not found</returns>
    [HttpGet("{id}")]
    public async Task<ActionResult<CompteDepotDTO>> GetById(int id)
    {
      try
      {
        var compte = await _compteDepotService.GetByIdAsync(id);
        if (compte == null)
        {
          var notFoundError = new ErrorDTO($"Compte avec l'ID {id} introuvable", 404, "Not Found", Request.Path);
          return NotFound(notFoundError);
        }

        var dto = CreateCompteDepotDTO(compte);
        return Ok(dto);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error retrieving CompteDepot with ID: {Id}", id);
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }

    /// <summary>
    /// Gets all deposit accounts for a specific user.
    /// </summary>
    /// <param name="userId">The user ID</param>
    /// <returns>List of CompteDepot DTOs for the user</returns>
    [HttpGet("user/{userId}")]
    public async Task<ActionResult<List<CompteDepotDTO>>> GetByUserId(int userId)
    {
      try
      {
        var comptes = await _compteDepotService.GetByUserIdAsync(userId);
        var dtos = comptes.Select(c => CreateCompteDepotDTO(c)).ToList();
        return Ok(dtos);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error retrieving CompteDepot entities for user: {UserId}", userId);
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }

    /// <summary>
    /// Creates a new deposit account.
    /// </summary>
    /// <param name="request">The creation request</param>
    /// <returns>Created CompteDepot DTO</returns>
    [HttpPost]
    public async Task<ActionResult<CompteDepotDTO>> Create([FromBody] CreateCompteDepotRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
        {
          var error = new ErrorDTO("Données invalides", 400, "Bad Request", Request.Path);
          return BadRequest(error);
        }

        var compte = await _compteDepotService.CreateAsync(
            request.TypeCompteDepotId,
            request.UserId,
            request.DateEcheance,
            request.Montant,
            request.ActionDateTime);

        var dto = CreateCompteDepotDTO(compte);
        return CreatedAtAction(nameof(GetById), new { id = compte.Id }, dto);
      }
      catch (ArgumentException ex)
      {
        _logger.LogWarning(ex, "Invalid argument for CompteDepot creation");
        var error = new ErrorDTO(ex.Message, 400, "Bad Request", Request.Path);
        return BadRequest(error);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error creating CompteDepot");
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }

    /// <summary>
    /// Withdraws from a deposit account at maturity.
    /// </summary>
    /// <param name="id">The account ID</param>
    /// <param name="request">The withdrawal request</param>
    /// <returns>Withdrawal response with calculated interest</returns>
    [HttpPost("{id}/withdraw")]
    public async Task<ActionResult<WithdrawResponse>> Withdraw(int id, [FromBody] WithdrawRequest request)
    {
      try
      {
        var (compte, interest) = await _compteDepotService.WithdrawAsync(id, request.ActionDateTime);

        var response = new WithdrawResponse(
            compte.Id,
            compte.Montant,
            interest,
            compte.DateRetire ?? DateTime.Now);

        return Ok(response);
      }
      catch (ArgumentException ex)
      {
        _logger.LogWarning(ex, "Invalid argument for withdrawal from CompteDepot ID: {Id}", id);
        var error = new ErrorDTO(ex.Message, 400, "Bad Request", Request.Path);
        return BadRequest(error);
      }
      catch (InvalidOperationException ex)
      {
        _logger.LogWarning(ex, "Invalid operation for withdrawal from CompteDepot ID: {Id}", id);
        var error = new ErrorDTO(ex.Message, 400, "Bad Request", Request.Path);
        return BadRequest(error);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error processing withdrawal for CompteDepot ID: {Id}", id);
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }

    /// <summary>
    /// Gets the calculated interest for a deposit account without withdrawing.
    /// </summary>
    /// <param name="id">The account ID</param>
    /// <param name="actionDateTime">Optional action date time for calculation</param>
    /// <returns>Interest calculation information</returns>
    [HttpGet("{id}/interest")]
    public async Task<ActionResult<object>> GetInterest(int id, [FromQuery] DateTime? actionDateTime = null)
    {
      try
      {
        var compte = await _compteDepotService.GetByIdAsync(id);
        if (compte == null)
        {
          var notFoundError = new ErrorDTO($"Compte avec l'ID {id} introuvable", 404, "Not Found", Request.Path);
          return NotFound(notFoundError);
        }

        var interest = _compteDepotService.CalculateInterest(compte, actionDateTime);
        var total = compte.Montant + interest;

        var result = new
        {
          CompteId = compte.Id,
          MontantInitial = compte.Montant,
          InteretCalcule = interest,
          MontantTotal = total,
          DateCalcul = actionDateTime ?? DateTime.Now,
          CanWithdraw = _compteDepotService.CanWithdraw(compte, actionDateTime ?? DateTime.Now)
        };

        return Ok(result);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error calculating interest for CompteDepot ID: {Id}", id);
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }

    /// <summary>
    /// Helper method to create CompteDepotDTO with calculated interest.
    /// </summary>
    private CompteDepotDTO CreateCompteDepotDTO(Models.Entities.CompteDepot compte)
    {
      var dto = new CompteDepotDTO(compte);

      // Calculate interest if not withdrawn
      if (!dto.EstRetire)
      {
        dto.InteretCalcule = _compteDepotService.CalculateInterest(compte);
        dto.MontantTotal = dto.Montant + dto.InteretCalcule;
      }
      else
      {
        // For withdrawn accounts, calculate interest based on withdrawal date
        dto.InteretCalcule = _compteDepotService.CalculateInterest(compte, compte.DateRetire);
        dto.MontantTotal = dto.Montant + dto.InteretCalcule;
      }

      return dto;
    }
  }
}