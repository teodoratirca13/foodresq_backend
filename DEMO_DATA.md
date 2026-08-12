# FoodResq Demo Data

This document describes the demo data inserted into the Neon PostgreSQL database for frontend testing.

## Connection

Data was inserted directly into the Neon PostgreSQL database using `psql`.

## Demo Accounts

| Email | Password | Role |
|---|---|---|
| admin@foodresq.com | Password123! | USER |
| business1@foodresq.com | Password123! | BUSINESS |
| business2@foodresq.com | Password123! | BUSINESS |
| ong1@foodresq.com | Password123! | ONG |
| user1@foodresq.com | Password123! | USER |
| user2@foodresq.com | Password123! | USER |

All passwords are BCrypt-hashed. The shared demo password is `Password123!`.

## Records Inserted

| Table | Records |
|---|---|
| users | 6 demo users inserted (0 duplicates) |
| listing | 22 new listings inserted |
| cart | 1 cart for user1 |
| cart_item | 2 cart items |
| orders | 2 demo orders |
| order_item | 2 order items |

Total tables in database: `users`, `listing`, `cart`, `cart_item`, `orders`, `order_item`

## Listings Breakdown

| Type | Count |
|---|---|
| SALE | 13 |
| DONATION | 9 |
| **Total** | **22** |

### Sale Listings (SALE)
1. Fresh Sourdough Bread (BAKERY) - 3.50 EUR
2. Margherita Pizza (Large) (PIZZA) - 8.99 EUR
3. Assorted Pastries Box (BAKERY) - 6.00 EUR
4. Pepperoni Pizza (Medium) (PIZZA) - 6.99 EUR
5. Craft Beer 6-Pack (DRINKS) - 10.00 EUR
6. Chocolate Croissants (8pc) (BAKERY) - 4.00 EUR
7. Organic Vegetable Box (VEGETABLES) - 12.00 EUR
8. Fresh Fruit Basket (FRUITS) - 7.50 EUR
9. Farm Fresh Eggs (30 pack) (DAIRY) - 5.00 EUR
10. Chicken Meat Bundle (MEAT) - 9.00 EUR
11. Yogurt & Cheese Pack (DAIRY) - 4.50 EUR
12. Organic Apples (5kg) (FRUITS) - 6.00 EUR
13. Beef Stew Meat (2kg) (MEAT) - 14.00 EUR

### Donation Listings (DONATION)
1. 50 Portions Rice & Beans (OTHER) - free
2. 50 Sandwiches (FAST_FOOD) - free
3. 40 Portions Pasta Salad (OTHER) - free
4. 30 Liters Soup (OTHER) - free
5. 15kg Mixed Meat Products (MEAT) - free
6. 20kg Surplus Vegetables (VEGETABLES) - free
7. 30kg Fresh Fruits (FRUITS) - free
8. 25kg Bread & Bakery Items (BAKERY) - free
9. 60 Portions Curry Rice (OTHER) - free

## Data by Category

Categories covered: PIZZA, BAKERY, MEAT, VEGETABLES, FRUITS, DRINKS, DAIRY, FAST_FOOD, OTHER

## How to Verify

### Public Endpoints (no auth required)

```bash
# All listings
GET https://foodresq-backend.onrender.com/api/listings

# Sale listings only
GET https://foodresq-backend.onrender.com/api/listings?type=SALE

# Donation listings only
GET https://foodresq-backend.onrender.com/api/listings?type=DONATION

# Swagger UI
https://foodresq-backend.onrender.com/swagger-ui/index.html
```

### Authenticated Endpoints

Login to get a JWT token:

```bash
POST https://foodresq-backend.onrender.com/api/auth/login
Content-Type: application/json

{"email":"user1@foodresq.com","password":"Password123!"}
```

Response: `{"token":"<JWT>","role":"USER"}`

Use the token in the `Authorization: Bearer <token>` header:

```bash
# Get my cart (USER role required)
GET https://foodresq-backend.onrender.com/api/cart
Authorization: Bearer <token>

# Get my orders (USER role required)
GET https://foodresq-backend.onrender.com/api/orders/my
Authorization: Bearer <token>
```

## Frontend Configuration

In your Next.js `.env.local`:

```
NEXT_PUBLIC_API_BASE_URL=https://foodresq-backend.onrender.com
```

## Notes

- The database already contained some existing listings and users from previous runs.
- All demo data uses `ON CONFLICT DO NOTHING` to avoid duplicates if the seeder is re-run.
- No data was deleted or overwritten.
- The `.env` file is excluded from git (already in `.gitignore`).
- No table had a sequence reset. IDs are generated normally.
