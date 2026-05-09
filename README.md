# FoodResq Backend

A Java Spring Boot backend for the FoodResq application - a food rescue platform connecting businesses with surplus food to consumers and NGOs.

## Project Structure

- `auth` - Authentication and authorization
- `user` - User management
- `listing` - Food listings management
- `cart` - Shopping cart functionality
- `order` - Order processing
- `security` - Security configuration
- `config` - Application configuration
- `common` - Shared components (exceptions, utilities)

## Technology Stack

- **Java 17**
- **Spring Boot 4.0.6**
- **Spring Data JPA / Hibernate**
- **Spring Security**
- **JWT Authentication**
- **PostgreSQL**
- **Lombok**
- **Springdoc OpenAPI (Swagger UI)**

## Database Migration to Neon PostgreSQL

This application has been migrated to use Neon cloud PostgreSQL instead of a local Docker PostgreSQL instance.

### Configuration

The database connection is configured through environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | JDBC URL for Neon PostgreSQL | `jdbc:postgresql://ep-xxxxxx.neon.tech/dbname?sslmode=require` |
| `DB_USERNAME` | Database username | `your_username` |
| `DB_PASSWORD` | Database password | `your_password` |
| `APP_JWT_SECRET` | Base64-encoded JWT secret (min 32 bytes) | `your_secret_here` |
| `APP_JWT_EXPIRATION_MS` | JWT expiration in milliseconds | `86400000` (24 hours) |
| `SERVER_PORT` | Server port (optional) | `8080` |

### Environment Setup

1. Create a `.env` file in the project root based on `.env.example`:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` with your actual Neon database credentials:
   ```properties
   # Database Connection
   DB_URL=jdbc:postgresql://**.c-3.eu-central-1.aws.neon.tech/neondb?sslmode=require
   DB_USERNAME=**
   DB_PASSWORD=**
   
   # JWT Configuration (override in production)
   APP_JWT_SECRET=
   APP_JWT_EXPIRATION_MS=86400000
   
   # Server Configuration
   SERVER_PORT=8080
   ```

> **Important**: The `.env` file is excluded from version control via `.gitignore` to protect your secrets.

### Running the Application

#### Using Maven (with environment variables)
```bash
# Export environment variables (Linux/Mac)
export DB_URL=your_neon_url
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export APP_JWT_SECRET=your_jwt_secret
export APP_JWT_EXPIRATION_MS=86400000
export SERVER_PORT=8080

# Then run
mvn spring-boot:run
```

#### Using Maven (with .env file)
If you have the [dotenv-maven-plugin](https://mvnrepository.com/artifact/io.github.cdiego/dotenv-maven-plugin) configured:
```bash
mvn spring-boot:run
```

#### Using Docker (if Dockerfile is added later)
```bash
docker build -t foodresq-backend .
docker run -p 8080:8080 --env-file .env foodresq-backend
```

### Database Schema Management

The application uses Hibernate's automatic DDL generation with `spring.jpa.hibernate.ddl-auto=update`. This means:

- On startup, Hibernate will compare your JPA entities with the database schema
- It will create tables, columns, and constraints that don't exist
- It will NOT drop existing tables or columns (safe for production)
- For destructive changes, manual migration scripts would be needed

**Note**: For production environments, consider using a dedicated migration tool like Flyway or Liquibase for more controlled schema evolution.

### API Documentation

Once the application is running, you can access the Swagger UI at:
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/v3/api-docs (OpenAPI JSON)

### Key Endpoints

#### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and receive JWT token

#### Listings
- `GET /api/listings` - Get all active listings
- `GET /api/listings/{id}` - Get listing by ID
- `POST /api/listings` - Create new listing (auth required)
- `PUT /api/listings/{id}` - Update listing (auth required)
- `DELETE /api/listings/{id}` - Delete listing (auth required)

#### Cart
- `GET /api/cart` - Get current user's cart
- `POST /api/cart/items` - Add item to cart
- `PUT /api/cart/items/{id}` - Update cart item quantity
- `DELETE /api/cart/items/{id}` - Remove item from cart

#### Orders
- `POST /api/orders` - Create order from cart
- `GET /api/orders` - Get user's order history
- `GET /api/orders/{id}` - Get order by ID

## Development Notes

### Hibernate Dialect
The application automatically detects and uses `PostgreSQLDialect` which is compatible with PostgreSQL 17.x as used by Neon.

### Connection Pooling
Uses HikariCP (Spring Boot's default) for efficient database connection management.

### SSL Requirement
Neon requires SSL connections, which is enforced via `sslmode=require` in the JDBC URL.

### Environment Specific Configuration
Currently, there's only one `application.properties` file. For more complex deployments, consider:
- `application-dev.properties` for development
- `application-prod.properties` for production
- Using Spring profiles (`--spring.profiles.active=prod`)

## Troubleshooting

### Common Issues

1. **Connection refused / timeout**
   - Verify your Neon hostname is correct
   - Ensure your IP is allowed in Neon's settings
   - Check that `sslmode=require` is present in the JDBC URL

2. **Authentication failed**
   - Double-check username and password
   - Ensure the user has appropriate permissions in Neon

3. **Schema creation errors**
   - Check application logs for Hibernate SQL statements
   - Ensure the database user has CREATE permissions
   - Verify no conflicting table/column names

4. **JWT issues**
   - Ensure the secret is at least 32 bytes when base64 decoded
   - Check token expiration settings

### Logging
To see more detailed SQL output, you can adjust logging levels:
```properties
# In application.properties or via environment
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing-feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is proprietary and confidential.

## Contact

For questions or support, please open an issue in the repository.