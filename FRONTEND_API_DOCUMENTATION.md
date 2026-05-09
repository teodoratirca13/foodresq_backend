# FoodResq Backend API Documentation for Frontend Developer

## 1. Project Overview

FoodResq is a food rescue platform that connects businesses with surplus food to consumers and NGOs. The backend is a Spring Boot REST API that handles user authentication, food listings, cart management, and order processing.

This API is designed to be consumed by a Next.js frontend application. The backend is deployed on Render.com and accessible via the internet.

**Backend Base URL:**
https://foodresq-backend.onrender.com

**Example API URL:**
https://foodresq-backend.onrender.com/api/listings

The frontend should store the backend base URL in an environment variable and append endpoint paths to it, rather than hardcoding full URLs throughout the application.

## 2. Frontend Integration Summary

The Next.js frontend should call the backend using `fetch`, Axios, or a similar HTTP client.

All API calls should use the backend base URL:
https://foodresq-backend.onrender.com

Example:
```javascript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

fetch(`${API_BASE_URL}/api/listings`)
```

For local Next.js development, the frontend developer can use this in `.env.local`:
```
NEXT_PUBLIC_API_BASE_URL=https://foodresq-backend.onrender.com
```

For Vercel deployment, the frontend developer should add this environment variable:
```
NEXT_PUBLIC_API_BASE_URL=https://foodresq-backend.onrender.com
```

This variable should be configured in Vercel Project Settings → Environment Variables.

## 3. Authentication Overview

The backend uses JWT (JSON Web Token) for authentication.

**Registration:**
- Endpoint: POST `/api/auth/register`
- Full URL: https://foodresq-backend.onrender.com/api/auth/register
- Request body (JSON):
  ```json
  {
    "name": "string (required, 2-100 chars)",
    "email": "string (required, valid email)",
    "password": "string (required, 8-100 chars)",
    "role": "enum (required): USER, BUSINESS, or ONG"
  }
  ```
- Response: `AuthResponse` object containing:
  ```json
  {
    "token": "string (JWT)",
    "role": "enum (USER, BUSINESS, or ONG)"
  }
  ```

**Login:**
- Endpoint: POST `/api/auth/login`
- Full URL: https://foodresq-backend.onrender.com/api/auth/login
- Request body (JSON):
  ```json
  {
    "email": "string (required, valid email)",
    "password": "string (required)"
  }
  ```
- Response: `AuthResponse` object containing:
  ```json
  {
    "token": "string (JWT)",
    "role": "enum (USER, BUSINESS, or ONG)"
  }
  ```

**Token Usage:**
- The token returned is a JWT and should be treated as a Bearer token.
- For authenticated requests, the frontend must include the header:
  `Authorization: Bearer <token>`
- Refresh tokens are not currently implemented. The frontend should treat the JWT as the active session token until it expires or the user logs out.
- Token expiration is configured via `app.jwt.expiration-ms` environment variable (value not inspected in code, but present in configuration).

**Frontend Storage:**
- Upon successful login or registration, the frontend should store the token (e.g., in `localStorage` for simplicity in a student project) and the user role.
- For better security in production, an HttpOnly cookie is preferred, but localStorage is acceptable for this project's context.

## 4. User Roles and Permissions

The backend defines three user roles:

1. **USER** - Regular consumers who can:
   - Reserve food listings (POST /api/listings/{id}/reserve)
   - Manage their cart (GET /api/cart, POST /api/cart/items, DELETE /api/cart/items/*, POST /api/cart/checkout)
   - View their orders (GET /api/orders/my)
   - Cancel listings they reserved (POST /api/listings/{id}/cancel)
   - Complete listings they reserved (POST /api/listings/{id}/complete)

2. **BUSINESS** - Food providers who can:
   - Create food listings (POST /api/listings)
   - Delete their own listings (DELETE /api/listings/**)
   - Complete listings (POST /api/listings/{id}/complete) - when they fulfill the order

3. **ONG** (Non-Governmental Organization) - Organizations that can:
   - Claim food donations (POST /api/listings/{id}/claim)
   - Cancel claimed listings (POST /api/listings/{id}/cancel)
   - Complete claimed listings (POST /api/listings/{id}/complete)

**Authentication Requirements:**
- All endpoints under `/api/auth/**` are public (no authentication required)
- GET endpoints under `/api/listings` and `/api/listings/**` are public (no authentication required)
- All other endpoints require authentication (valid JWT)
- Specific role-based restrictions are enforced as described above

The frontend should store the user role received in the AuthResponse during login/registration and use it to conditionally show/hide UI elements based on the user's permissions.

## 5. API Usage Rules for Frontend

**Common Rules:**
- Public endpoints do not require authentication token.
- Protected endpoints require:
  `Authorization: Bearer <JWT_TOKEN>`
- Request body should be JSON format.
- Content-Type header should be:
  `application/json`
- For authenticated calls, include both headers:
  ```
  Content-Type: application/json
  Authorization: Bearer <token>
  ```

**TypeScript Fetch Helper:**
```typescript
async function apiFetch(path: string, options: RequestInit = {}) {
  // Get token from localStorage (adjust based on your storage choice)
  const token = typeof window !== "undefined" ? localStorage.getItem("token") : null;

  return fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
  });
}
```

**Usage Example:**
```typescript
// Public endpoint (no auth needed)
apiFetch("/api/listings");

// Protected endpoint (auth required)
apiFetch("/api/cart", { method: "GET" });

// With request body
apiFetch("/api/auth/login", {
  method: "POST",
  body: JSON.stringify({ email: "user@example.com", password: "password123" })
});
```

**Security Note:**
- For better security, an HttpOnly cookie approach is preferred in production, but if the backend currently returns JWT in the response body, localStorage can be used for a simple student/hobby project.
- If using localStorage, protect routes on the frontend and clear token on logout.

## 6. Endpoints

### 6.1 Auth Module

#### Register (Create New Account)

**Method:** POST
**URL:** `/api/auth/register`
**Full URL:** https://foodresq-backend.onrender.com/api/auth/register
**Authentication:** Public (No token required)
**Role:** Not applicable

**Description:** Register a new user with name, email, password, and role.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "USER"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER"
}
```

**Frontend Usage:** Call this when the user fills out the registration form. Store the token and role in localStorage upon success.

**Possible Errors:**
- 400 Bad Request - Invalid input (missing fields, invalid email format, password too short)
- 409 Conflict - Email already registered

---

#### Login

**Method:** POST
**URL:** `/api/auth/login`
**Full URL:** https://foodresq-backend.onrender.com/api/auth/login
**Authentication:** Public (No token required)
**Role:** Not applicable

**Description:** Authenticate an existing user and get JWT token.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER"
}
```

**Frontend Usage:** Call this when the user logs in. Store the token and role in localStorage.

**Possible Errors:**
- 401 Unauthorized - Invalid email or password

---

### 6.2 Listings Module

#### Get All Active Listings

**Method:** GET
**URL:** `/api/listings`
**Full URL:** https://foodresq-backend.onrender.com/api/listings
**Authentication:** Public (No token required)
**Role:** Not applicable

**Description:** Retrieve all active food listings, optionally filtered by type and category.

**Query Parameters (optional):**
- `type` - Filter by listing type (SALE or DONATION)
- `category` - Filter by product category (e.g., FRUITS, VEGETABLES, BAKERY, DAIRY, MEAT, PREPARED, OTHER)

**Example:** `/api/listings?type=SALE&category=BAKERY`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Fresh Bread",
    "description": "Day-old bread available",
    "quantity": 20,
    "price": 2.50,
    "expirationDate": "2026-05-10T18:00:00",
    "latitude": 45.7600,
    "longitude": 21.2400,
    "type": "SALE",
    "status": "ACTIVE",
    "ownerId": 5,
    "ownerName": "Bakery Shop",
    "createdAt": "2026-05-09T10:00:00",
    "category": "BAKERY"
  },
  {
    "id": 2,
    "title": "Surplus Vegetables",
    "description": "Various vegetables",
    "quantity": 15,
    "price": 0.00,
    "expirationDate": "2026-05-11T12:00:00",
    "latitude": 45.7650,
    "longitude": 21.2450,
    "type": "DONATION",
    "status": "ACTIVE",
    "ownerId": 8,
    "ownerName": "Grocery Store",
    "createdAt": "2026-05-09T11:00:00",
    "category": "VEGETABLES"
  }
]
```

**Frontend Usage:** Call this to display all available food listings on the marketplace page.

**Possible Errors:**
- 500 Internal Server Error - Backend error

---

#### Get Listing by ID

**Method:** GET
**URL:** `/api/listings/{id}`
**Full URL:** https://foodresq-backend.onrender.com/api/listings/1
**Authentication:** Public (No token required)
**Role:** Not applicable

**Description:** Retrieve a specific listing by its ID.

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Fresh Bread",
  "description": "Day-old bread available",
  "quantity": 20,
  "price": 2.50,
  "expirationDate": "2026-05-10T18:00:00",
  "latitude": 45.7600,
  "longitude": 21.2400,
  "type": "SALE",
  "status": "ACTIVE",
  "ownerId": 5,
  "ownerName": "Bakery Shop",
  "createdAt": "2026-05-09T10:00:00",
  "category": "BAKERY"
}
```

**Frontend Usage:** Call this when the user clicks on a specific listing to view details.

**Possible Errors:**
- 404 Not Found - Listing does not exist

---

#### Get Nearby Listings

**Method:** GET
**URL:** `/api/listings/nearby?lat={latitude}&lng={longitude}&radiusKm={radius}`
**Full URL:** https://foodresq-backend.onrender.com/api/listings/nearby?lat=45.7600&lng=21.2400&radiusKm=5
**Authentication:** Public (No token required)
**Role:** Not applicable

**Description:** Get listings within a specified radius (in kilometers) from a given location.

**Query Parameters:**
- `lat` (required) - Latitude of the center point
- `lng` (required) - Longitude of the center point
- `radiusKm` (optional, default: 5) - Search radius in kilometers

**Response (200 OK):** Array of ListingResponse objects (same as Get All Listings).

**Frontend Usage:** Call this to show listings near the user's location on a map view.

**Possible Errors:**
- 400 Bad Request - Missing lat/lng parameters
- 500 Internal Server Error - Backend error

---

#### Get Listings by Owner

**Method:** GET
**URL:** `/api/listings/owner/{ownerId}`
**Full URL:** https://foodresq-backend.onrender.com/api/listings/owner/5
**Authentication:** Public (No token required)
**Role:** Not applicable

**Description:** Get all listings created by a specific business owner.

**Response (200 OK):** Array of ListingResponse objects.

**Frontend Usage:** Call this to display a business's profile with their listings.

**Possible Errors:**
- 404 Not Found - Owner not found

---

#### Create New Listing

**Method:** POST
**URL:** `/api/listings`
**Full URL:** https://foodresq-backend.onrender.com/api/listings
**Authentication:** Requires JWT
**Role:** BUSINESS only

**Description:** Create a new food listing. Only users with BUSINESS role can create listings.

**Request Body:**
```json
{
  "title": "Fresh Pastries",
  "description": "Assorted pastries from today",
  "quantity": 30,
  "price": 3.00,
  "minimumPrice": 1.00,
  "discountPercentage": 10,
  "expirationDate": "2026-05-10T20:00:00",
  "latitude": 45.7600,
  "longitude": 21.2400,
  "type": "SALE",
  "category": "BAKERY"
}
```

**Response (201 Created):** ListingResponse object.

**Frontend Usage:** Call this when a business user wants to list food for sale or donation. Show a form with all these fields.

**Possible Errors:**
- 400 Bad Request - Invalid input
- 401 Unauthorized - Not logged in
- 403 Forbidden - User does not have BUSINESS role

---

#### Reserve a Listing (For Purchase)

**Method:** POST
**URL:** `/api/listings/{id}/reserve`
**Full URL:** https://foodresq-backend.onrender.com/api/listings/1/reserve
**Authentication:** Requires JWT
**Role:** USER only

**Description:** Reserve a SALE listing for purchase. Only users with USER role can reserve.

**Request Body:** None (empty)

**Response (200 OK):** Updated ListingResponse object.

**Frontend Usage:** Call this when a regular user wants to reserve a food item for purchase. Show a confirmation dialog first.

**Possible Errors:**
- 401 Unauthorized - Not logged in
- 403 Forbidden - User does not have USER role
- 404 Not Found - Listing not found
- 409 Conflict - Listing already reserved or not available

---

#### Claim a Listing (For Donation)

**Method:** POST
**URL:** `/api/listings/{id}/claim`
**Full URL:** https://foodresq-backend.onrender.com/api/listings/1/claim
**Authentication:** Requires JWT
**Role:** ONG only

**Description:** Claim a DONATION listing. Only users with ONG role can claim donations.

**Request Body:** None (empty)

**Response (200 OK):** Updated ListingResponse object.

**Frontend Usage:** Call this when an NGO user wants to claim a donated food item.

**Possible Errors:**
- 401 Unauthorized - Not logged in
- 403 Forbidden - User does not have ONG role
- 404 Not Found - Listing not found
- 409 Conflict - Listing already claimed or not a donation

---

#### Complete a Listing

**Method:** POST
**URL:** `/api/listings/{id}/complete`
**Full URL:** https://foodresq-backend.onrender.com/api/listings/1/complete
**Authentication:** Requires JWT (any authenticated user)
**Role:** Any authenticated role

**Description:** Mark a listing as completed (fulfilled/picked up).

**Request Body:** None (empty)

**Response (200 OK):** Updated ListingResponse object.

**Frontend Usage:** Call this when the transaction is complete (food picked up by user/ONG).

**Possible Errors:**
- 401 Unauthorized - Not logged in
- 404 Not Found - Listing not found
- 400 Bad Request - Listing cannot be completed (e.g., not reserved/claimed)

---

#### Cancel a Listing

**Method:** POST
**URL:** `/api/listings/{id}/cancel`
**Full URL:** https://foodresq-backend.onrender.com/api/listings/1/cancel
**Authentication:** Requires JWT (any authenticated user)
**Role:** Any authenticated role

**Description:** Cancel a reservation or claim on a listing.

**Request Body:** None (empty)

**Response (200 OK):** Updated ListingResponse object.

**Frontend Usage:** Call this when a user wants to cancel their reservation/claim.

**Possible Errors:**
- 401 Unauthorized - Not logged in
- 404 Not Found - Listing not found
- 400 Bad Request - Listing cannot be cancelled

---

#### Delete a Listing

**Method:** DELETE
**URL:** `/api/listings/{id}`
**Full URL:** https://foodresq-backend.onrender.com/api/listings/1
**Authentication:** Requires JWT
**Role:** Any authenticated role (owner can delete their own)

**Description:** Delete a listing. Typically the owner can delete their own listing.

**Request Body:** None

**Response (204 No Content):** No content returned on success.

**Frontend Usage:** Call this when a business wants to remove their listing.

**Possible Errors:**
- 401 Unauthorized - Not logged in
- 403 Forbidden - Not the owner of the listing
- 404 Not Found - Listing not found

---

### 6.3 Cart Module

#### Get My Cart

**Method:** GET
**URL:** `/api/cart`
**Full URL:** https://foodresq-backend.onrender.com/api/cart
**Authentication:** Requires JWT
**Role:** USER only

**Description:** Retrieve the current user's shopping cart.

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 10,
  "items": [
    {
      "id": 1,
      "listingId": 5,
      "listingTitle": "Fresh Bread",
      "quantity": 2,
      "price": 2.50
    },
    {
      "id": 2,
      "listingId": 8,
      "listingTitle": "Surplus Vegetables",
      "quantity": 1,
      "price": 0.00
    }
  ],
  "totalItems": 3,
  "totalPrice": 5.00
}
```

**Frontend Usage:** Call this when the user views their shopping cart.

**Possible Errors:**
- 401 Unauthorized - Not logged in
- 403 Forbidden - User does not have USER role

---

#### Add Item to Cart

**Method:** POST
**URL:** `/api/cart/items`
**Full URL:** https://foodresq-backend.onrender.com/api/cart/items
**Authentication:** Requires JWT
**Role:** USER only

**Description:** Add a listing to the shopping cart.

**Request Body:**
```json
{
  "listingId": 5,
  "quantity": 2
}
```

**Response (200 OK):** No content returned on success.

**Frontend Usage:** Call this when a user adds an item to their cart from a listing page.

**Possible Errors:**
- 400 Bad Request - Invalid input (e.g., quantity > available)
- 401 Unauthorized - Not logged in
- 403 Forbidden - User does not have USER role
- 404 Not Found - Listing not found
- 409 Conflict - Listing not available or already reserved

---

#### Remove Item from Cart

**Method:** DELETE
**URL:** `/api/cart/items/{itemId}`
**Full URL:** https://foodresq-backend.onrender.com/api/cart/items/1
**Authentication:** Requires JWT
**Role:** USER only

**Description:** Remove a specific item from the shopping cart.

**Request Body:** None

**Response (204 No Content):** No content returned on success.

**Frontend Usage:** Call this when the user wants to remove an item from their cart.

**Possible Errors:**
- 401 Unauthorized - Not logged in
- 403 Forbidden - User does not have USER role
- 404 Not Found - Cart item not found

---

#### Clear Cart

**Method:** DELETE
**URL:** `/api/cart`
**Full URL:** https://foodresq-backend.onrender.com/api/cart
**Authentication:** Requires JWT
**Role:** USER only

**Description:** Remove all items from the shopping cart.

**Request Body:** None

**Response (204 No Content):** No content returned on success.

**Frontend Usage:** Call this when the user wants to empty their entire cart.

**Possible Errors:**
- 401 Unauthorized - Not logged in
- 403 Forbidden - User does not have USER role

---

#### Checkout (Create Order)

**Method:** POST
**URL:** `/api/cart/checkout`
**Full URL:** https://foodresq-backend.onrender.com/api/cart/checkout
**Authentication:** Requires JWT
**Role:** USER only

**Description:** Checkout the cart and create an order.

**Request Body:** None

**Response (200 OK):** Returns OrderResponse object.

**Frontend Usage:** Call this when the user completes the purchase. Should redirect to order confirmation after success.

**Possible Errors:**
- 400 Bad Request - Cart is empty or items no longer available
- 401 Unauthorized - Not logged in
- 403 Forbidden - User does not have USER role
- 500 Internal Server Error - Checkout processing error

---

### 6.4 Orders Module

#### Get My Orders

**Method:** GET
**URL:** `/api/orders/my`
**Full URL:** https://foodresq-backend.onrender.com/api/orders/my
**Authentication:** Requires JWT
**Role:** USER only

**Description:** Retrieve the current user's order history.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 10,
    "orderDate": "2026-05-09T14:30:00",
    "status": "COMPLETED",
    "totalAmount": 15.00,
    "items": [
      {
        "listingId": 5,
        "listingTitle": "Fresh Bread",
        "quantity": 2,
        "pricePerUnit": 2.50,
        "subtotal": 5.00
      },
      {
        "listingId": 8,
        "listingTitle": "Surplus Vegetables",
        "quantity": 1,
        "pricePerUnit": 0.00,
        "subtotal": 0.00
      }
    ]
  }
]
```

**Frontend Usage:** Call this to display the user's order history on their profile or orders page.

**Possible Errors:**
- 401 Unauthorized - Not logged in
- 403 Forbidden - User does not have USER role

## 7. Suggested Frontend Pages and API Mapping

Based on the backend endpoints, here is a suggested Next.js frontend structure:

| Page/Route | HTTP Method | Endpoint | Full URL |
|------------|-------------|----------|----------|
| `/login` | POST | `/api/auth/login` | https://foodresq-backend.onrender.com/api/auth/login |
| `/register` | POST | `/api/auth/register` | https://foodresq-backend.onrender.com/api/auth/register |
| `/` (Home) | GET | `/api/listings` | https://foodresq-backend.onrender.com/api/listings |
| `/listings` | GET | `/api/listings` | https://foodresq-backend.onrender.com/api/listings |
| `/listings/[id]` | GET | `/api/listings/{id}` | https://foodresq-backend.onrender.com/api/listings/1 |
| `/listings/nearby` | GET | `/api/listings/nearby?lat=...&lng=...` | https://foodresq-backend.onrender.com/api/listings/nearby?lat=45.76&lng=21.24 |
| `/listings/create` (BUSINESS only) | POST | `/api/listings` | https://foodresq-backend.onrender.com/api/listings |
| `/listings/[id]/reserve` (USER only) | POST | `/api/listings/{id}/reserve` | https://foodresq-backend.onrender.com/api/listings/1/reserve |
| `/listings/[id]/claim` (ONG only) | POST | `/api/listings/{id}/claim` | https://foodresq-backend.onrender.com/api/listings/1/claim |
| `/cart` | GET | `/api/cart` | https://foodresq-backend.onrender.com/api/cart |
| `/cart/add` | POST | `/api/cart/items` | https://foodresq-backend.onrender.com/api/cart/items |
| `/cart/checkout` | POST | `/api/cart/checkout` | https://foodresq-backend.onrender.com/api/cart/checkout |
| `/orders` | GET | `/api/orders/my` | https://foodresq-backend.onrender.com/api/orders/my |

**Note:** Routes marked with role restrictions (BUSINESS, USER, ONG) should be protected on the frontend based on the stored user role.

## 8. Data Models Useful for Frontend

### AuthResponse
```typescript
interface AuthResponse {
  token: string;  // JWT token
  role: "USER" | "BUSINESS" | "ONG";
}
```

### ListingResponse
```typescript
interface ListingResponse {
  id: number;
  title: string;
  description: string | null;
  quantity: number;
  price: number;  // BigDecimal as number
  expirationDate: string;  // ISO 8601 datetime
  latitude: number;
  longitude: number;
  type: "SALE" | "DONATION";
  status: "ACTIVE" | "RESERVED" | "CLAIMED" | "COMPLETED" | "CANCELLED";
  ownerId: number;
  ownerName: string;
  reservedByUserId: number | null;
  reservedByUserName: string | null;
  createdAt: string;  // ISO 8601 datetime
  category: ProductCategory | null;
}
```

### ProductCategory (Enum)
- FRUITS
- VEGETABLES
- BAKERY
- DAIRY
- MEAT
- PREPARED
- OTHER

### ListingType (Enum)
- SALE
- DONATION

### ListingStatus (Enum)
- ACTIVE
- RESERVED
- CLAIMED
- COMPLETED
- CANCELLED

### CartResponse
```typescript
interface CartResponse {
  id: number;
  userId: number;
  items: CartItem[];
  totalItems: number;
  totalPrice: number;
}

interface CartItem {
  id: number;
  listingId: number;
  listingTitle: string;
  quantity: number;
  price: number;
}
```

### OrderResponse
```typescript
interface OrderResponse {
  id: number;
  userId: number;
  orderDate: string;  // ISO 8601 datetime
  status: "PENDING" | "COMPLETED" | "CANCELLED";
  totalAmount: number;
  items: OrderItem[];
}

interface OrderItem {
  listingId: number;
  listingTitle: string;
  quantity: number;
  pricePerUnit: number;
  subtotal: number;
}
```

## 9. Error Handling

The backend does not currently expose a fully standardized error response format. The frontend should initially handle errors based on HTTP status codes and fallback to generic messages.

**Backend Error Patterns (Observations):**
- 400 Bad Request: Usually accompanied by message like "Invalid input" or specific validation errors
- 401 Unauthorized: "Invalid email or password" for login, or token-related issues
- 403 Forbidden: "Access denied" or role-related errors
- 404 Not Found: "Resource not found"
- 409 Conflict: "Email already registered" or resource conflict
- 500 Internal Server Error: Generic server error

**Recommended Frontend Error Strategy:**
- **400**: Show validation message to user (e.g., "Please fill in all required fields")
- **401**: Redirect to login page / clear invalid token from storage
- **403**: Show "You do not have permission to perform this action"
- **404**: Show "Resource not found"
- **409**: Show specific conflict message from response if available
- **500**: Show generic "Server error. Please try again later."

**Example Error Handling in TypeScript:**
```typescript
async function handleApiError(response: Response): Promise<string> {
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    
    switch (response.status) {
      case 400:
        return data.message || "Invalid input. Please check your data.";
      case 401:
        // Clear token and redirect to login
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.href = "/login";
        return "Session expired. Please log in again.";
      case 403:
        return "You do not have permission to perform this action.";
      case 404:
        return "Resource not found.";
      case 409:
        return data.message || "Conflict occurred.";
      default:
        return "An error occurred. Please try again.";
    }
  }
  return "";
}
```

## 10. CORS Configuration

The backend Spring Security configuration includes a CORS configuration that allows requests from specific origins.

**Allowed Origins:**
- Local frontend: `http://localhost:3000` (always allowed by default)
- Additional origins can be configured via environment variable `CORS_ALLOWED_ORIGINS`

**Configuring Additional Origins:**
On Render dashboard, add an environment variable:
```
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

For multiple origins, comma-separate them:
```
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://your-frontend.vercel.app
```

**Current CORS Settings:**
- Allowed Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Allowed Headers: Authorization, Content-Type, Accept, Origin, X-Requested-With
- Exposed Headers: Authorization
- Credentials: false (tokens are sent via Authorization header, not cookies)
- Preflight cache: 3600 seconds

**Testing CORS:**
Preflight check:
```bash
curl -i -X OPTIONS "https://foodresq-backend.onrender.com/api/listings" \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET"
```

Expected response headers:
```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
Access-Control-Allow-Headers: Authorization,Content-Type,Accept,Origin,X-Requested-With
```

**Note:** If you see CORS errors after deployment, make sure to add your Vercel domain to the `CORS_ALLOWED_ORIGINS` environment variable in the Render dashboard.

## 11. Swagger / API Docs

Swagger UI is configured and publicly accessible (permitted in SecurityConfig).

**Access URLs:**
- Swagger UI: https://foodresq-backend.onrender.com/swagger-ui/index.html
- OpenAPI Docs: https://foodresq-backend.onrender.com/v3/api-docs

The frontend developer can use Swagger UI to explore the API interactively and see the exact request/response formats.

## 12. Render Backend Deployment Notes

**Backend Information:**
- **Base URL:** https://foodresq-backend.onrender.com
- **Example Endpoint:** https://foodresq-backend.onrender.com/api/listings

**Important Notes:**
- Render Free Tier may have cold starts. The first request after a period of inactivity may be slow (can take 10-30 seconds).
- The backend uses environment variables for database and JWT configuration (already configured on Render).
- The frontend should not call `localhost` in production - it must call the Render URL.
- The backend is configured to accept requests from the frontend domain (CORS should be properly configured).

## 13. Recommended Vercel Deployment for Frontend

**Recommended Hosting:** Vercel (for Next.js)

**Environment Variable Configuration:**

In Vercel Project Settings → Environment Variables, add:
```
NEXT_PUBLIC_API_BASE_URL=https://foodresq-backend.onrender.com
```

**Local Development:**
Create a `.env.local` file in your Next.js project:
```
NEXT_PUBLIC_API_BASE_URL=https://foodresq-backend.onrender.com
```

**Testing Against Local Backend (Optional):**
If you want to test the frontend against a local backend running on port 8080 instead of the deployed backend, temporarily change the variable:
```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

**Important:** For the integrated deployed application, always use:
```
NEXT_PUBLIC_API_BASE_URL=https://foodresq-backend.onrender.com
```

## 14. Critical Integration Gaps

Based on the backend inspection, here are potential gaps that could affect frontend integration:

1. **CORS Configuration** - Priority: Fixed
   - **Status:** Resolved. The backend SecurityConfig now includes a CorsConfigurationSource bean.
   - **Allowed Origins:** `http://localhost:3000` is always allowed. Additional origins can be set via `CORS_ALLOWED_ORIGINS` environment variable on Render.
   - **Vercel Integration:** Add your Vercel domain to the Render environment variable `CORS_ALLOWED_ORIGINS`.

2. **Missing Logout Endpoint** - Priority: Important
   - **Issue:** No backend endpoint to invalidate JWT tokens.
   - **Impact:** Frontend must handle logout by just clearing localStorage (client-side only).
   - **Solution:** Consider implementing a server-side logout or token blacklist if session invalidation is required.

3. **Missing Get Current User Endpoint** - Priority: Important
   - **Issue:** No endpoint to fetch the currently authenticated user's details.
   - **Impact:** Frontend cannot easily retrieve user profile information after initial login.
   - **Solution:** Add a GET /api/users/me or GET /api/auth/me endpoint (needs backend modification).

4. **No Refresh Token Mechanism** - Priority: Important
   - **Issue:** JWT expires and there is no refresh token to renew it.
   - **Impact:** User must re-login after token expiration.
   - **Solution:** For a student/hobby project, this is acceptable. For production, implement refresh token rotation.

5. **Listing Status Not Fully Clear** - Priority: Nice to have
   - **Issue:** ListingResponse includes a status field but the exact workflow for status transitions is not fully documented.
   - **Impact:** Frontend might need to experiment to understand the listing lifecycle.
   - **Solution:** More detailed backend documentation or user flow diagrams.

## 15. Minimal Frontend Implementation Plan

Follow this order for a working frontend integration:

1. **Set up Next.js project**
   ```bash
   npx create-next-app@latest foodresq-frontend
   ```

2. **Configure API base URL**
   - Create `.env.local` with:
     ```
     NEXT_PUBLIC_API_BASE_URL=https://foodresq-backend.onrender.com
     ```

3. **Build authentication pages**
   - Create `/login` page calling `POST /api/auth/login`
   - Create `/register` page calling `POST /api/auth/register`
   - Store token and role in localStorage

4. **Create API client helper**
   - Implement the `apiFetch` function shown in Section 5
   - Add global error handling for 401 to redirect to login

5. **Build public listing pages**
   - Create homepage `/` calling `GET /api/listings`
   - Create `/listings/[id]` detail page calling `GET /api/listings/{id}`

6. **Build protected pages for USER role**
   - Create `/cart` page with cart operations
   - Create `/orders` page calling `GET /api/orders/my`

7. **Build protected pages for BUSINESS role**
   - Create `/listings/create` page calling `POST /api/listings`

8. **Build protected pages for ONG role**
   - Use existing listing pages with claim functionality

9. **Add role-based UI**
   - Show/hide navigation items based on stored user role
   - Protect routes with `middleware.ts` or client-side checks

10. **Deploy to Vercel**
    - Push code to GitHub
    - Import project in Vercel
    - Add `NEXT_PUBLIC_API_BASE_URL` environment variable

11. **Test against Render backend**
    - Verify all flows work in production
    - Check CORS from Vercel domain

## 16. Final Checklist for Frontend Developer

- [ ] Backend Render URL added to `.env.local`
- [ ] `NEXT_PUBLIC_API_BASE_URL=https://foodresq-backend.onrender.com`
- [ ] Backend Render URL added to Vercel environment variables
- [ ] Login works (POST /api/auth/login)
- [ ] Register works (POST /api/auth/register)
- [ ] JWT token is saved to localStorage
- [ ] Authenticated requests send `Authorization: Bearer <token>`
- [ ] 401 errors redirect to login page
- [ ] Public listings load from `https://foodresq-backend.onrender.com/api/listings`
- [ ] Protected actions work (reserve, cart, orders, create listing)
- [ ] CORS works from `localhost:3000` (for local testing)
- [ ] CORS works from Vercel domain (production)
- [ ] Production frontend does not call localhost
- [ ] Swagger UI checked at https://foodresq-backend.onrender.com/swagger-ui/index.html