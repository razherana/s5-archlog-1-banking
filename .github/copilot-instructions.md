# Banking System AI Coding Instructions

## Architecture Overview

This is a multi-project banking system with a **Java EE current account service** (`projects/java-courant/`) as the primary implementation. The architecture follows a 3-tier pattern:

- **API Layer**: JAX-RS resources in `api/` package (REST endpoints at `/api/*`)
- **Service Layer**: EJB stateless services in `application/` package with JTA transaction management
- **Persistence Layer**: JPA entities in `entities/` package using Hibernate + MySQL

### Key Design Patterns

**Balance Calculation**: Accounts have NO stored balance field. Balance is calculated on-demand by summing all transactions where the account is sender (-) or receiver (+). See `CompteCourantService.calculateSolde()`.

**Transaction Model**: Uses a **double-entry-like system** where:
- **Deposits**: `sender = null` (external source), `receiver = account`
- **Withdrawals**: `sender = account`, `receiver = null` (external destination)  
- **Transfers**: `sender = sourceAccount`, `receiver = destinationAccount`

**Service Integration**: Services are EJB-managed with `@EJB` injection. All transaction operations use `@TransactionAttribute(REQUIRED)` for atomicity.

## Development Workflow

### Build & Run
```bash
cd projects/java-courant/banking-courant
mvn clean compile                    # Build
mvn tomee:run                       # Start TomEE server (port 8080)
```

### Testing Strategy
The project uses **curl-based integration tests** instead of unit tests:
```bash
cd tests/
./run-all-tests.sh                 # Run all test suites
./1-test-user-management/user-crud-tests.sh    # Specific module
```

Test structure follows domain boundaries: user management, account management, transaction management, and full integration scenarios.

### Database Management
- **Schema**: `database/s5-banking-prog.sql` (manual setup required)
- **Connection**: MySQL via `mysqlDatabase` JTA datasource (configured in `resources.xml`)
- **JPA Config**: Uses `userPU` persistence unit with Hibernate

## Coding Conventions

### Entity Relationships
- All entities use `@GeneratedValue(IDENTITY)` for IDs
- Lazy loading with `FetchType.LAZY` for relationships
- Use `BigDecimal` for all monetary values with precision (15,2)

### Service Layer Patterns
```java
@Stateless
public class ExampleService {
    @PersistenceContext(unitName = "userPU")
    private EntityManager entityManager;
    
    @EJB
    private OtherService otherService;
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void transactionalMethod() { ... }
}
```

### API Layer Patterns
- All resources use `@Path`, `@Produces(APPLICATION_JSON)`, `@Consumes(APPLICATION_JSON)`
- Return `Response` objects with proper HTTP status codes
- Use DTOs for data transfer, never expose entities directly
- Implement comprehensive error handling with `ErrorDTO`

### Error Handling
```java
try {
    // business logic
} catch (IllegalArgumentException e) {
    ErrorDTO error = new ErrorDTO(e.getMessage(), 400, "Bad Request", request.getPath());
    return Response.status(400).entity(error).build();
}
```

## File Organization

### Package Structure
- `mg.razherana.banking.courant.api.*` - REST resources
- `mg.razherana.banking.courant.application.*` - Business services  
- `mg.razherana.banking.courant.entities.*` - JPA entities
- `mg.razherana.banking.courant.dto.*` - Data transfer objects

### Key Files
- `BankingApplication.java` - JAX-RS application config (`@ApplicationPath("/api")`)
- `persistence.xml` - JPA configuration with MySQL dialect
- `resources.xml` - TomEE datasource configuration
- `tests/README.md` - Comprehensive testing documentation

## Business Rules

### Transaction Validation
- All monetary amounts must be positive (`BigDecimal.compareTo(ZERO) > 0`)
- Withdrawals/transfers check insufficient funds via `calculateSolde()`
- Transactions are atomic - use JTA transaction boundaries

### Account Rules
- Accounts belong to users (required relationship)
- Account numbers are auto-generated
- Initial balance is always 0.00 (calculated from transaction history)

### API Endpoint Patterns
- Users: `/api/users`, `/api/users/{id}`, `/api/users/email/{email}`
- Accounts: `/api/comptes`, `/api/comptes/{id}`, `/api/comptes/user/{userId}`  
- Transactions: `/api/transactions`, `/api/transactions/depot`, `/api/transactions/retrait`, `/api/transactions/transfert`

## Integration Points

### Database Schema
The system is designed for a larger banking ecosystem with unused tables (`compte_depots`, `compte_prets`) indicating future deposit and loan modules.

### Multi-Project Structure
This is part of a larger system at `/1-banque/projects/` with planned `.NET` deposit and `PHP` interface modules.

When extending this system, maintain the transaction-based balance approach and EJB service boundaries for consistency across modules.