using BankingDepot.Data;
using BankingDepot.Models.Entities;
using BankingDepot.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace BankingDepot.Services.Implementations
{
  /// <summary>
  /// Service implementation for TypeCompteDepot operations.
  /// </summary>
  public class TypeCompteDepotService : ITypeCompteDepotService
  {
    private readonly BankingDepotContext _context;
    private readonly ILogger<TypeCompteDepotService> _logger;

    public TypeCompteDepotService(BankingDepotContext context, ILogger<TypeCompteDepotService> logger)
    {
      _context = context;
      _logger = logger;
    }

    public async Task<List<TypeCompteDepot>> GetAllAsync()
    {
      _logger.LogInformation("Getting all TypeCompteDepot entities");
      return await _context.TypeComptesDepots.ToListAsync();
    }

    public async Task<TypeCompteDepot?> GetByIdAsync(int id)
    {
      _logger.LogInformation("Getting TypeCompteDepot by ID: {Id}", id);
      return await _context.TypeComptesDepots.FindAsync(id);
    }

    public async Task<TypeCompteDepot> CreateAsync(string nom, decimal tauxInteret)
    {
      _logger.LogInformation("Creating new TypeCompteDepot: {Nom}, TauxInteret: {TauxInteret}", nom, tauxInteret);

      if (string.IsNullOrWhiteSpace(nom))
        throw new ArgumentException("Le nom ne peut pas être vide", nameof(nom));

      if (tauxInteret <= 0)
        throw new ArgumentException("Le taux d'intérêt doit être positif", nameof(tauxInteret));

      var typeCompteDepot = new TypeCompteDepot
      {
        Nom = nom.Trim(),
        TauxInteret = tauxInteret
      };

      _context.TypeComptesDepots.Add(typeCompteDepot);
      await _context.SaveChangesAsync();

      _logger.LogInformation("TypeCompteDepot created with ID: {Id}", typeCompteDepot.Id);
      return typeCompteDepot;
    }

    public async Task<TypeCompteDepot?> UpdateAsync(int id, string nom, decimal tauxInteret)
    {
      _logger.LogInformation("Updating TypeCompteDepot ID: {Id}", id);

      var typeCompteDepot = await _context.TypeComptesDepots.FindAsync(id);
      if (typeCompteDepot == null)
      {
        _logger.LogWarning("TypeCompteDepot with ID {Id} not found", id);
        return null;
      }

      if (string.IsNullOrWhiteSpace(nom))
        throw new ArgumentException("Le nom ne peut pas être vide", nameof(nom));

      if (tauxInteret <= 0)
        throw new ArgumentException("Le taux d'intérêt doit être positif", nameof(tauxInteret));

      typeCompteDepot.Nom = nom.Trim();
      typeCompteDepot.TauxInteret = tauxInteret;

      await _context.SaveChangesAsync();

      _logger.LogInformation("TypeCompteDepot ID: {Id} updated successfully", id);
      return typeCompteDepot;
    }

    public async Task<bool> DeleteAsync(int id)
    {
      _logger.LogInformation("Deleting TypeCompteDepot ID: {Id}", id);

      var typeCompteDepot = await _context.TypeComptesDepots.FindAsync(id);
      if (typeCompteDepot == null)
      {
        _logger.LogWarning("TypeCompteDepot with ID {Id} not found", id);
        return false;
      }

      _context.TypeComptesDepots.Remove(typeCompteDepot);
      await _context.SaveChangesAsync();

      _logger.LogInformation("TypeCompteDepot ID: {Id} deleted successfully", id);
      return true;
    }
  }
}