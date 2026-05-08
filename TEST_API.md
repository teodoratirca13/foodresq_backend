# FoodResq API Testing Guide

This guide provides step-by-step instructions for testing the FoodResq API endpoints with proper authentication against the Neon PostgreSQL database.

## Prerequisites

1. **Running Application**: Ensure the Spring Boot application is running and connected to Neon:
   ```bash
   mvn spring-boot:run
   ```
   Verify it's accessible at `http://localhost:8080`

2. **Tools**: You can use any of these tools:
   - cURL (command line)
   - Postman (GUI)
   - HTTPie (command line)
   - Browser extensions (RESTer, Talend API Tester, etc.)
   - PowerShell (as demonstrated in our testing)

## Authentication Workflow

All protected endpoints require a valid JWT token obtained through the authentication endpoints.

### Step 1: Register a New User

**Endpoint**: `POST /api/auth/register`
**Content-Type**: `application/json`

```json
{
  "name": "Test Business",
  "email": "business@example.com",
  "password": "securePassword123!",
  "role": "BUSINESS"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "role": "BUSINESS"
}
```

### Step 2: Login to Get Token

**Endpoint**: `POST /api/auth/login`
**Content-Type**: `application/json`

```json
{
  "email": "business@example.com",
  "password": "securePassword123!"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "role": "BUSINESS"
}
```

> **Note**: You can use either registration (for new users) or login (for existing users) to obtain a token.

### Step 3: Use the Token for Protected Requests

Include the token in the Authorization header:
```
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

## Testing Listings Endpoints

### Create a Listing (Requires BUSINESS role)

**Endpoint**: `POST /api/listings`
**Content-Type**: `application/json`
**Headers**: `Authorization: Bearer <your_token>`

**Request Body**:
```json
{
  "title": "Chicken Burgers",
  "description": "Crispy chicken burgers with fries",
  "quantity": 6,
  "price": 28,
  "minimumPrice": 15,
  "expirationDate": "2026-12-01T22:00:00",
  "latitude": 44.4371,
  "longitude": 26.1090,
  "type": "SALE",
  "category": "FAST_FOOD"
}
```

**Expected Response** (200 OK):
```json
{
  "id": 1,
  "title": "Chicken Burgers",
  "description": "Crispy chicken burgers with fries",
  "quantity": 6,
  "price": 28,
  "minimumPrice": 15,
  "expirationDate": "2026-12-01T22:00:00",
  "latitude": 44.4371,
  "longitude": 26.1090,
  "type": "SALE",
  "category": "FAST_FOOD",
  "status": "ACTIVE",
  "createdAt": "2026-05-08T20:54:22.1589726",
  "ownerId": 4,
  "ownerName": "Test Business",
  "reservedByUserId": null,
  "reservedByUserName": null
}
```

### Get All Listings (Public Access)

**Endpoint**: `GET /api/listings`
**Headers**: None required (public endpoint)

**Response**:
```json
[
  {
    "id": 1,
    "title": "Chicken Burgers",
    // ... other fields
  },
  {
    "id": 2,
    "title": "Vegan Pizza",
    // ... other fields
  }
]
```

### Get Listing by ID (Public Access)

**Endpoint**: `GET /api/listings/{id}`
**Headers**: None required

**Example**: `GET /api/listings/1`

### Update Listing (Requires authentication - owner only)

**Endpoint**: `PUT /api/listings/{id}`
**Headers**: `Authorization: Bearer <your_token>`
**Content-Type**: `application/json`

Same request body as creation, but only the listing owner can update.

### Delete Listing (Requires authentication - owner only)

**Endpoint**: `DELETE /api/listings/{id}`
**Headers**: `Authorization: Bearer <your_token>`

### Special Listing Actions

#### Reserve a Listing for Sale (Requires USER role)

**Endpoint**: `POST /api/listings/{id}/reserve`
**Headers**: `Authorization: Bearer <user_token>`
**Content-Type**: `application/json` (empty body)

> Only users can reserve items marked as SALE

#### Claim a Donation Listing (Requires ONG role)

**Endpoint**: `POST /api/listings/{id}/claim`
**Headers**: `Authorization: Bearer <ong_token>`
**Content-Type**: `application/json` (empty body)

> Only NGOs can claim items marked as DONATION

#### Complete a Listing (Requires authentication - owner or reserving user)

**Endpoint**: `POST /api/listings/{id}/complete`
**Headers**: `Authorization: Bearer <token>`
**Content-Type**: `application/json` (empty body)

> Can be completed by either the owner or the user who reserved/claimed it

#### Cancel a Listing (Requires authentication - owner only)

**Endpoint**: `POST /api/listings/{id}/cancel`
**Headers**: `Authorization: Bearer <owner_token>`
**Content-Type**: `application/json` (empty body)

## Testing Cart Endpoints (Requires USER role)

### Get Current User's Cart

**Endpoint**: `GET /api/cart`
**Headers**: `Authorization: Bearer <user_token>`

### Add Item to Cart

**Endpoint**: `POST /api/cart/items`
**Headers**: `Authorization: Bearer <user_token>`
**Content-Type**: `application/json`

**Request Body**:
```json
{
  "listingId": 1,
  "quantity": 2
}
```

### Update Cart Item Quantity

**Endpoint**: `PUT /api/cart/items/{itemId}`
**Headers**: `Authorization: Bearer <user_token>`
**Content-Type**: `application/json`

**Request Body**:
```json
{
  "quantity": 3
}
```

### Remove Item from Cart

**Endpoint**: `DELETE /api/cart/items/{itemId}`
**Headers**: `Authorization: Bearer <user_token>`

### Checkout (Create Order from Cart)

**Endpoint**: `POST /api/cart/checkout`
**Headers**: `Authorization: Bearer <user_token>`
**Content-Type**: `application/json` (empty body)

## Testing Order Endpoints (Requires USER role)

### Get User's Order History

**Endpoint**: `GET /api/orders/my`
**Headers**: `Authorization: Bearer <user_token>`

### Get Order by ID

**Endpoint**: `GET /api/orders/{id}`
**Headers**: `Authorization: Bearer <user_token>`

## Expected HTTP Status Codes

| Code | Meaning | When to Expect |
|------|---------|----------------|
| 200 | OK | Successful GET, PUT, DELETE |
| 201 | Created | Successful POST (sometimes used instead of 200) |
| 400 | Bad Request | Invalid request data, validation failures |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Valid token but insufficient permissions (wrong role) |
| 404 | Not Found | Resource doesn't exist (invalid ID) |
| 409 | Conflict | Resource conflict (e.g., email already registered) |
| 500 | Internal Server Error | Unexpected server error |

## Common Error Responses

### Authentication Errors:
- **401 Unauthorized**: Missing or malformed Authorization header
- **401 Unauthorized**: Invalid token (expired, wrong signature, etc.)
- **403 Forbidden**: Valid token but user lacks required role for endpoint

### Validation Errors:
- **400 Bad Request**: Missing required fields
- **400 Bad Request**: Invalid data types (string where number expected, etc.)
- **400 Bad Request**: Validation constraint violations (email format, password length, etc.)
- **400 Bad Request**: Business rule violations (e.g., expiration date in past, price validation)

### Resource Errors:
- **404 Not Found**: Trying to access non-existent resource by ID
- **409 Conflict**: Trying to register with existing email

## Testing Tips

1. **Token Management**: Tokens are valid for 24 hours by default. For extended testing sessions, you may need to refresh your token.

2. **Role Testing**: To properly test role-based access:
   - Register three users with different roles (USER, BUSINESS, ONG)
   - Obtain tokens for each
   - Test each endpoint with the appropriate role tokens
   - Verify that incorrect role tokens receive 403 Forbidden

3. **Data Isolation**: For clean testing:
   - Use unique email addresses for each test run
   - Or delete test data between runs using the DELETE endpoints
   - Note: Listings can only be deleted by their owner

4. **Endpoint Discovery**: The Swagger UI is available at:
   - http://localhost:8080/swagger-ui.html
   - http://localhost:8080/v3/api-docs (OpenAPI JSON)

5. **Neon Verification**: To confirm data is actually stored in Neon:
   - Check that listing IDs increment sequentially
   - Verify data persists between application restarts
   - Use Neon's console or SQL editor to query tables directly

## Example Test Sequence

Here's a complete test sequence demonstrating the core functionality:

1. **Register Business User**:
   ```http
   POST /api/auth/register
   {
     "name": "Test Burger Shop",
     "email": "burgers@example.com",
     "password": "burgerPass123!",
     "role": "BUSINESS"
   }
   ```

2. **Login to Get Token**:
   ```http
   POST /api/auth/login
   {
     "email": "burgers@example.com",
     "password": "burgerPass123!"
   }
   ```
   → Save returned token as `BUSINESS_TOKEN`

3. **Create Listing**:
   ```http
   POST /api/listings
   Authorization: Bearer BUSINESS_TOKEN
   Content-Type: application/json
   
   {
     "title": "Friday Special Burgers",
     "description": "Weekend special - buy 2 get 1 free",
     "quantity": 10,
     "price": 25,
     "minimumPrice": 20,
     "expirationDate": "2026-12-31T23:59:59",
     "latitude": 40.7128,
     "longitude": -74.0060,
     "type": "SALE",
     "category": "FAST_FOOD"
   }
   ```
   → Save returned ID as `LISTING_ID`

4. **Register Regular User**:
   ```http
   POST /api/auth/register
   {
     "name": "Hungry Customer",
     "email": "customer@example.com",
     "password": "custPass123!",
     "role": "USER"
   }
   ```
   → Save returned token as `USER_TOKEN`

5. **Reserve Listing**:
   ```http
   POST /api/listings/LISTING_ID/reserve
   Authorization: Bearer USER_TOKEN
   ```

6. **Verify Reservation**:
   ```http
   GET /api/listings/LISTING_ID
   ```
   → Check that status is "RESERVED" and reservedByUserId matches the user's ID

7. **Complete Listing**:
   ```http
   POST /api/listings/LISTING_ID/complete
   Authorization: Bearer USER_TOKEN
   ```

8. **Verify Completion**:
   ```http
   GET /api/listings/LISTING_ID
   ```
   → Check that status is "COMPLETED"

## Troubleshooting

### If You Get 401/403 Errors:
1. Verify you're including the Authorization header correctly: `Authorization: Bearer <token>`
2. Check that your token hasn't expired (get a fresh one if needed)
3. Confirm you have the correct role for the endpoint
4. Check application logs for authentication error details

### If You Get 400 Errors:
1. Verify your JSON is valid
2. Check that all required fields are present
3. Ensure data types match expectations (numbers not strings, etc.)
4. Look at the error message in the response body for specific validation failures

### If Listings Don't Appear:
1. Verify you're using the correct base URL (http://localhost:8080)
2. Check that the application is running and connected to Neon
3. Look for Hibernate SQL logs in the console showing INSERT statements
4. Try retrieving the listing directly by ID after creation

### Connection Issues:
1. Verify Neon hostname and credentials in your environment variables
2. Test database connectivity independently if needed
3. Check that `sslmode=require` is present in your JDBC URL
4. Review application startup logs for connection errors

## Security Notes

- Never commit actual tokens, passwords, or secrets to version control
- Use environment variables or secure secret stores for configuration
- In production, consider shorter token expiration times
- Always validate and sanitize input data on both client and server sides
- Use HTTPS in production to protect tokens in transit

This guide covers the essential testing scenarios for verifying your FoodResq backend is properly connected to Neon PostgreSQL with functioning authentication and authorization systems.