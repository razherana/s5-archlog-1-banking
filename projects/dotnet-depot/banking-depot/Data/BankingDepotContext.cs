using BankingDepot.Models.Entities;
using Microsoft.EntityFrameworkCore;

namespace BankingDepot.Data
{
  /// <summary>
  /// Entity Framework Core DbContext for the banking deposit module.
  /// Manages database connections and entity configurations.
  /// </summary>
  public class BankingDepotContext : DbContext
  {
    public BankingDepotContext(DbContextOptions<BankingDepotContext> options)
        : base(options)
    {
    }

    public DbSet<TypeCompteDepot> TypeComptesDepots { get; set; }
    public DbSet<CompteDepot> ComptesDepots { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
      base.OnModelCreating(modelBuilder);

      // Configure TypeCompteDepot
      modelBuilder.Entity<TypeCompteDepot>(entity =>
      {
        entity.HasKey(e => e.Id);
        entity.Property(e => e.Id).ValueGeneratedOnAdd();
        entity.Property(e => e.Nom).IsRequired().HasMaxLength(255);
        entity.Property(e => e.TauxInteret).IsRequired().HasPrecision(5, 4);

        // Configure the relationship
        entity.HasMany(e => e.ComptesDepots)
                    .WithOne(e => e.TypeCompteDepot)
                    .HasForeignKey(e => e.TypeCompteDepotId)
                    .OnDelete(DeleteBehavior.Cascade);
      });

      // Configure CompteDepot
      modelBuilder.Entity<CompteDepot>(entity =>
      {
        entity.HasKey(e => e.Id);
        entity.Property(e => e.Id).ValueGeneratedOnAdd();
        entity.Property(e => e.TypeCompteDepotId).IsRequired();
        entity.Property(e => e.UserId).IsRequired();
        entity.Property(e => e.DateOuverture).IsRequired();
        entity.Property(e => e.DateEcheance).IsRequired();
        entity.Property(e => e.Montant).IsRequired().HasPrecision(15, 2);
        entity.Property(e => e.EstRetire).IsRequired().HasDefaultValue(0);
        entity.Property(e => e.DateRetire).IsRequired(false);
      });
    }
  }
}