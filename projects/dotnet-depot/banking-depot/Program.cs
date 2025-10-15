using BankingDepot.Data;
using BankingDepot.Services.Interfaces;
using BankingDepot.Services.Implementations;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Add Entity Framework
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
builder.Services.AddDbContext<BankingDepotContext>(options =>
    options.UseMySql(connectionString, ServerVersion.AutoDetect(connectionString)));

// Add HTTP Client for external service calls
builder.Services.AddHttpClient<IUserValidationService, UserValidationService>();

// Add application services
builder.Services.AddScoped<ITypeCompteDepotService, TypeCompteDepotService>();
builder.Services.AddScoped<ICompteDepotService, CompteDepotService>();
builder.Services.AddScoped<IUserValidationService, UserValidationService>();

// Add logging
builder.Services.AddLogging();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run();
