# Banking Depot Module - ASP.NET Core

## Overview

This is the **deposit account module** of the multi-project banking system. It's built with **ASP.NET Core** and handles "compte à terme" (term deposit accounts) functionality.

### Architecture

The module follows a **3-tier architecture** consistent with the Java modules:

- **API Layer**: ASP.NET Core Controllers in `Controllers/` (REST endpoints at `/api/*`)
- **Service Layer**: Business logic services in `Services/` with dependency injection
- **Persistence Layer**: Entity Framework Core entities in `Models/Entities/` using MySQL

### Key Features

- **Deposit Account Types**: Manage different types of deposit accounts with varying interest rates
- **Term Deposits**: Create deposit accounts with specific maturity dates
- **Simple Interest Calculation**: Calculate interest from opening date to maturity date
- **Withdrawal Management**: Allow withdrawals only at maturity with interest
- **User Validation**: Integrate with Java current account service for user verification
- **Backtracking Support**: Support `actionDateTime` parameter for historical operations

## Database Schema

The module uses its own MySQL database: `s5_archlog_1_banking_depot`

### Tables

- **`type_compte_depots`**: Account types with interest rates
- **`compte_depots`**: Individual deposit accounts

## Business Rules

### Deposit Account Creation

- User must exist in the Java current account service
- Account type must exist
- Maturity date must be after opening date
- Deposit amount must be positive

### Interest Calculation

- **Simple Interest Formula**: `Interest = Principal × Rate × Time (in years)`
- Time calculated from `date_ouverture` to `date_echeance`
- Interest applied only at withdrawal time
- Calculated on-demand, not stored

### Withdrawal Rules

- Withdrawal allowed only at or after `date_echeance`
- Cannot withdraw from already withdrawn accounts
- Returns original deposit + calculated interest
- Sets `est_retire = 1` and `date_retire = actionDateTime`

## API Endpoints

### TypeComptesDepots (Account Types)

- `GET /api/typecomptesdepots` - Get all account types
- `GET /api/typecomptesdepots/{id}` - Get account type by ID
- `POST /api/typecomptesdepots` - Create new account type
- `PUT /api/typecomptesdepots/{id}` - Update account type
- `DELETE /api/typecomptesdepots/{id}` - Delete account type

### ComptesDepots (Deposit Accounts)

- `GET /api/comptesdepots` - Get all deposit accounts
- `GET /api/comptesdepots/{id}` - Get account by ID
- `GET /api/comptesdepots/user/{userId}` - Get accounts by user ID
- `POST /api/comptesdepots` - Create new deposit account
- `POST /api/comptesdepots/{id}/withdraw` - Withdraw from account
- `GET /api/comptesdepots/{id}/interest` - Calculate interest without withdrawing

## Configuration

### Database Connection

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=localhost;Database=s5_archlog_1_banking_depot;User=root;Password=;"
  }
}
```

### Java Service Integration

```json
{
  "JavaService": {
    "BaseUrl": "http://127.0.0.2:8080/api"
  }
}
```

## Development Workflow

### Prerequisites

- .NET 9.0 SDK
- MySQL Server
- Java current account service running on `127.0.0.2:8080`

### Build & Run

```bash
cd banking-depot
dotnet restore                    # Restore packages
dotnet build                      # Build project
dotnet run                        # Start development server (port 5000/5001)
```

### Database Setup

1- Create the database using the SQL script:

```bash
mysql -u root -p < db/depot.sql
```

2- The application will auto-create tables on first run via Entity Framework.

### Testing

The API can be tested using:

- **Swagger UI**: Available at `https://localhost:5001/swagger` in development
- **HTTP files**: Use `banking-depot.http` for API testing
- **Postman/Curl**: All endpoints return JSON

## Project Structure

```txt
banking-depot/
├── Controllers/                 # REST API Controllers
│   ├── TypeComptesDepotsController.cs
│   └── ComptesDepotsController.cs
├── Models/
│   ├── Entities/               # EF Core Entities
│   │   ├── TypeCompteDepot.cs
│   │   └── CompteDepot.cs
│   └── DTOs/                   # Data Transfer Objects
│       ├── ErrorDTO.cs
│       ├── TypeCompteDepotDTO.cs
│       ├── CompteDepotDTO.cs
│       ├── CreateTypeCompteDepotRequest.cs
│       ├── CreateCompteDepotRequest.cs
│       ├── WithdrawRequest.cs
│       └── WithdrawResponse.cs
├── Services/
│   ├── Interfaces/             # Service Contracts
│   │   ├── IUserValidationService.cs
│   │   ├── ITypeCompteDepotService.cs
│   │   └── ICompteDepotService.cs
│   └── Implementations/        # Service Implementations
│       ├── UserValidationService.cs
│       ├── TypeCompteDepotService.cs
│       └── CompteDepotService.cs
├── Data/
│   └── BankingDepotContext.cs  # EF Core DbContext
├── db/
│   └── depot.sql               # Database Schema
├── tests/
│   ├── sample_data.sql         # Test Data
│   └── clear_data.sql          # Cleanup Script
├── Program.cs                  # Application Entry Point
├── appsettings.json           # Configuration
└── banking-depot.csproj       # Project File
```

## Integration Points

### External Dependencies

- **Java Current Account Service**: User validation via HTTP calls to `127.0.0.2:8080/api/users/{id}`

### Error Handling

- Consistent `ErrorDTO` format across all endpoints
- Proper HTTP status codes (400, 404, 500)
- Comprehensive logging with structured messages

## Technical Stack

- **.NET 9.0**: Latest LTS framework
- **ASP.NET Core**: Web API framework
- **Entity Framework Core**: ORM with MySQL provider
- **Pomelo.EntityFrameworkCore.MySql**: MySQL database provider
- **Swashbuckle**: Swagger/OpenAPI documentation
- **Built-in Dependency Injection**: Service registration and lifecycle management
- **ILogger**: Structured logging throughout the application

## Future Enhancements

The interest calculation method is designed to be easily replaceable:

```csharp
// Current: Simple Interest
public decimal CalculateSimpleInterest(CompteDepot compte, DateTime? actionDateTime = null)

// Future: Compound Interest
public decimal CalculateCompoundInterest(CompteDepot compte, DateTime? actionDateTime = null)
```

This modular design allows switching between interest calculation methods without affecting the rest of the system.
