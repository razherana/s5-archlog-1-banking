using BankingDepot.Data;
using BankingDepot.Models.Entities;
using BankingDepot.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace BankingDepot.Services.Implementations
{
  /// <summary>
  /// Service implementation for CompteDepot operations.
  /// Handles deposit account management including interest calculations.
  /// </summary>
  public class CompteDepotService : ICompteDepotService
  {
    private readonly BankingDepotContext _context;
    private readonly IUserValidationService _userValidationService;
    private readonly ILogger<CompteDepotService> _logger;

    public CompteDepotService(
        BankingDepotContext context,
        IUserValidationService userValidationService,
        ILogger<CompteDepotService> logger)
    {
      _context = context;
      _userValidationService = userValidationService;
      _logger = logger;
    }

    public async Task<List<CompteDepot>> GetAllAsync()
    {
      _logger.LogInformation("Getting all CompteDepot entities");
      return await _context.ComptesDepots
          .Include(c => c.TypeCompteDepot)
          .OrderByDescending(c => c.DateOuverture)
          .ToListAsync();
    }

    public async Task<CompteDepot?> GetByIdAsync(int id)
    {
      _logger.LogInformation("Getting CompteDepot by ID: {Id}", id);
      return await _context.ComptesDepots
          .Include(c => c.TypeCompteDepot)
          .FirstOrDefaultAsync(c => c.Id == id);
    }

    public async Task<List<CompteDepot>> GetByUserIdAsync(int userId)
    {
      _logger.LogInformation("Getting CompteDepot entities for user: {UserId}", userId);
      return await _context.ComptesDepots
          .Include(c => c.TypeCompteDepot)
          .Where(c => c.UserId == userId)
          .OrderByDescending(c => c.DateOuverture)
          .ToListAsync();
    }

    public async Task<CompteDepot> CreateAsync(int typeCompteDepotId, int userId, DateTime dateEcheance, decimal montant, DateTime? actionDateTime = null)
    {
      _logger.LogInformation("Creating CompteDepot for user {UserId}, type {TypeId}, amount {Montant}", userId, typeCompteDepotId, montant);

      // Validate user exists via Java service
      var userExists = await _userValidationService.ValidateUserExistsAsync(userId);
      if (!userExists)
      {
        throw new ArgumentException($"L'utilisateur avec l'ID {userId} n'existe pas");
      }

      // Validate TypeCompteDepot exists
      var typeCompteDepot = (await _context.TypeComptesDepots.FindAsync(typeCompteDepotId))
        ?? throw new ArgumentException($"Le type de compte avec l'ID {typeCompteDepotId} n'existe pas");

      // Validate business rules
      if (montant <= 0)
        throw new ArgumentException("Le montant doit être positif");

      var dateOuverture = actionDateTime ?? DateTime.Now;
      if (dateEcheance <= dateOuverture)
        throw new ArgumentException("La date d'échéance doit être postérieure à la date d'ouverture");

      var compteDepot = new CompteDepot
      {
        TypeCompteDepotId = typeCompteDepotId,
        UserId = userId,
        DateOuverture = dateOuverture,
        DateEcheance = dateEcheance,
        Montant = montant,
        EstRetire = 0,
        DateRetire = null
      };

      _context.ComptesDepots.Add(compteDepot);
      await _context.SaveChangesAsync();

      // Reload with navigation properties
      await _context.Entry(compteDepot)
          .Reference(c => c.TypeCompteDepot)
          .LoadAsync();

      _logger.LogInformation("CompteDepot created with ID: {Id}", compteDepot.Id);
      return compteDepot;
    }

    public async Task<(CompteDepot compte, decimal interest)> WithdrawAsync(int id, DateTime? actionDateTime = null)
    {
      _logger.LogInformation("Processing withdrawal for CompteDepot ID: {Id}", id);

      var compte = (await GetByIdAsync(id))
        ?? throw new ArgumentException($"Le compte avec l'ID {id} n'existe pas");
      
      var referenceDateTime = actionDateTime ?? DateTime.Now;

      // Check if account is already withdrawn
      if (compte.EstRetire == 1)
      {
        throw new InvalidOperationException("Ce compte a déjà été retiré");
      }

      // Check if withdrawal is allowed (must be at or after maturity date)
      if (!CanWithdraw(compte, referenceDateTime))
      {
        throw new InvalidOperationException($"Le retrait n'est pas autorisé avant la date d'échéance ({compte.DateEcheance:yyyy-MM-dd HH:mm:ss})");
      }

      // Calculate interest
      var interest = CalculateInterest(compte, referenceDateTime);

      // Mark as withdrawn
      compte.EstRetire = 1;
      compte.DateRetire = referenceDateTime;

      await _context.SaveChangesAsync();

      _logger.LogInformation("Withdrawal processed for CompteDepot ID: {Id}, Interest: {Interest}", id, interest);
      return (compte, interest);
    }

    /// <summary>
    /// Calculates interest from date_ouverture to date_echeance.
    /// Simple Interest = Principal × Rate × Time (in years)
    /// This method can be easily replaced for compound interest calculation.
    /// </summary>
    public decimal CalculateInterest(CompteDepot compte, DateTime? actionDateTime = null)
    {
      if (compte.TypeCompteDepot == null)
      {
        throw new InvalidOperationException("TypeCompteDepot information is required for interest calculation");
      }

      // Calculate time period from opening to maturity date
      var timeSpan = compte.DateEcheance - compte.DateOuverture;
      var timeInYears = (decimal)timeSpan.TotalDays / 365.25m; // Using 365.25 to account for leap years

      // Simple Interest = Principal × Rate × Time
      var interest = compte.Montant * compte.TypeCompteDepot.TauxInteret * timeInYears;

      _logger.LogInformation("Interest calculated for CompteDepot ID: {Id}, Principal: {Principal}, Rate: {Rate}, Time: {Time} years, Interest: {Interest}",
          compte.Id, compte.Montant, compte.TypeCompteDepot.TauxInteret, timeInYears, interest);

      return Math.Round(interest, 2);
    }

    public bool CanWithdraw(CompteDepot compte, DateTime actionDateTime)
    {
      return actionDateTime >= compte.DateEcheance && compte.EstRetire == 0;
    }
  }
}