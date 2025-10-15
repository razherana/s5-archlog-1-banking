using BankingDepot.Models.DTOs;
using BankingDepot.Models.DTOs.Requests;
using BankingDepot.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace BankingDepot.Controllers
{
  /// <summary>
  /// REST API Controller for TypeCompteDepot operations.
  /// Manages deposit account types with their interest rates.
  /// </summary>
  [ApiController]
  [Route("api/[controller]")]
  [Produces("application/json")]
  public class TypeComptesDepotsController : ControllerBase
  {
    private readonly ITypeCompteDepotService _typeCompteDepotService;
    private readonly ILogger<TypeComptesDepotsController> _logger;

    public TypeComptesDepotsController(
        ITypeCompteDepotService typeCompteDepotService,
        ILogger<TypeComptesDepotsController> logger)
    {
      _typeCompteDepotService = typeCompteDepotService;
      _logger = logger;
    }

    /// <summary>
    /// Gets all deposit account types.
    /// </summary>
    /// <returns>List of all TypeCompteDepot DTOs</returns>
    [HttpGet]
    public async Task<ActionResult<List<TypeCompteDepotDTO>>> GetAll()
    {
      try
      {
        var types = await _typeCompteDepotService.GetAllAsync();
        var dtos = types.Select(t => new TypeCompteDepotDTO(t)).ToList();
        return Ok(dtos);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error retrieving all TypeCompteDepot entities");
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }

    /// <summary>
    /// Gets a deposit account type by ID.
    /// </summary>
    /// <param name="id">The type ID</param>
    /// <returns>TypeCompteDepot DTO or 404 if not found</returns>
    [HttpGet("{id}")]
    public async Task<ActionResult<TypeCompteDepotDTO>> GetById(int id)
    {
      try
      {
        var type = await _typeCompteDepotService.GetByIdAsync(id);
        if (type == null)
        {
          var notFoundError = new ErrorDTO($"Type de compte avec l'ID {id} introuvable", 404, "Not Found", Request.Path);
          return NotFound(notFoundError);
        }

        var dto = new TypeCompteDepotDTO(type);
        return Ok(dto);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error retrieving TypeCompteDepot with ID: {Id}", id);
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }

    /// <summary>
    /// Creates a new deposit account type.
    /// </summary>
    /// <param name="request">The creation request</param>
    /// <returns>Created TypeCompteDepot DTO</returns>
    [HttpPost]
    public async Task<ActionResult<TypeCompteDepotDTO>> Create([FromBody] CreateTypeCompteDepotRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
        {
          var error = new ErrorDTO("Données invalides", 400, "Bad Request", Request.Path);
          return BadRequest(error);
        }

        var type = await _typeCompteDepotService.CreateAsync(request.Nom, request.TauxInteret);
        var dto = new TypeCompteDepotDTO(type);

        return CreatedAtAction(nameof(GetById), new { id = type.Id }, dto);
      }
      catch (ArgumentException ex)
      {
        _logger.LogWarning(ex, "Invalid argument for TypeCompteDepot creation");
        var error = new ErrorDTO(ex.Message, 400, "Bad Request", Request.Path);
        return BadRequest(error);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error creating TypeCompteDepot");
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }

    /// <summary>
    /// Updates an existing deposit account type.
    /// </summary>
    /// <param name="id">The type ID to update</param>
    /// <param name="request">The update request</param>
    /// <returns>Updated TypeCompteDepot DTO or 404 if not found</returns>
    [HttpPut("{id}")]
    public async Task<ActionResult<TypeCompteDepotDTO>> Update(int id, [FromBody] CreateTypeCompteDepotRequest request)
    {
      try
      {
        if (!ModelState.IsValid)
        {
          var error = new ErrorDTO("Données invalides", 400, "Bad Request", Request.Path);
          return BadRequest(error);
        }

        var type = await _typeCompteDepotService.UpdateAsync(id, request.Nom, request.TauxInteret);
        if (type == null)
        {
          var notFoundError = new ErrorDTO($"Type de compte avec l'ID {id} introuvable", 404, "Not Found", Request.Path);
          return NotFound(notFoundError);
        }

        var dto = new TypeCompteDepotDTO(type);
        return Ok(dto);
      }
      catch (ArgumentException ex)
      {
        _logger.LogWarning(ex, "Invalid argument for TypeCompteDepot update");
        var error = new ErrorDTO(ex.Message, 400, "Bad Request", Request.Path);
        return BadRequest(error);
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error updating TypeCompteDepot with ID: {Id}", id);
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }

    /// <summary>
    /// Deletes a deposit account type.
    /// </summary>
    /// <param name="id">The type ID to delete</param>
    /// <returns>204 No Content if successful, 404 if not found</returns>
    [HttpDelete("{id}")]
    public async Task<ActionResult> Delete(int id)
    {
      try
      {
        var deleted = await _typeCompteDepotService.DeleteAsync(id);
        if (!deleted)
        {
          var notFoundError = new ErrorDTO($"Type de compte avec l'ID {id} introuvable", 404, "Not Found", Request.Path);
          return NotFound(notFoundError);
        }

        return NoContent();
      }
      catch (Exception ex)
      {
        _logger.LogError(ex, "Error deleting TypeCompteDepot with ID: {Id}", id);
        var error = new ErrorDTO("Erreur interne du serveur", 500, "Internal Server Error", Request.Path);
        return StatusCode(500, error);
      }
    }
  }
}