!/bin/bash

# JWT & Role-Based Authorization - Test Script
# This script demonstrates how to test the authentication and authorization system

set -e

BASE_URL="http://localhost:8080/api"

echo "=================================="
echo "JWT Authentication Test Suite"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test 1: Login with valid LAWYER credentials
echo -e "${BLUE}TEST 1: Login with LAWYER user${NC}"
LAWYER_TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }' | jq -r '.token')

if [ -z "$LAWYER_TOKEN" ] || [ "$LAWYER_TOKEN" == "null" ]; then
  echo -e "${RED}❌ Failed to get token${NC}"
else
  echo -e "${GREEN}✓ Successfully logged in as LAWYER${NC}"
  echo "Token: ${LAWYER_TOKEN:0:50}..."
fi
echo ""

# Test 2: Try login with invalid password
echo -e "${BLUE}TEST 2: Login with invalid password (should fail)${NC}"
RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "wrongpassword"
  }' -w "\n%{http_code}")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" == "401" ] || [ "$HTTP_CODE" == "403" ]; then
  echo -e "${GREEN}✓ Correctly rejected invalid password (HTTP $HTTP_CODE)${NC}"
else
  echo -e "${RED}❌ Unexpected response (HTTP $HTTP_CODE)${NC}"
fi
echo ""

# Test 3: Try login with non-existent user
echo -e "${BLUE}TEST 3: Login with non-existent user (should fail)${NC}"
RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@example.com",
    "password": "password123"
  }' -w "\n%{http_code}")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" == "404" ]; then
  echo -e "${GREEN}✓ Correctly rejected non-existent user (HTTP $HTTP_CODE)${NC}"
else
  echo -e "${RED}❌ Unexpected response (HTTP $HTTP_CODE)${NC}"
fi
echo ""

# Test 4: Create gig with valid JWT token (LAWYER)
echo -e "${BLUE}TEST 4: Create gig with valid LAWYER token${NC}"
RESPONSE=$(curl -s -X POST "$BASE_URL/gigs/me" \
  -H "Authorization: Bearer $LAWYER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "minPrice": 150.00,
    "aboutThisGig": "Legal consultation for contracts",
    "isPublic": true
  }' -w "\n%{http_code}")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "201" ]; then
  echo -e "${GREEN}✓ Successfully created gig (HTTP $HTTP_CODE)${NC}"
  echo "Response: $(echo $BODY | jq -r '.id' | cut -c1-36)..."
else
  echo -e "${RED}❌ Failed to create gig (HTTP $HTTP_CODE)${NC}"
  echo "Response: $BODY"
fi
echo ""

# Test 5: Access protected endpoint without token
echo -e "${BLUE}TEST 5: Access protected endpoint without token (should fail)${NC}"
RESPONSE=$(curl -s -X POST "$BASE_URL/gigs/me" \
  -H "Content-Type: application/json" \
  -d '{
    "minPrice": 100.00,
    "aboutThisGig": "Test",
    "isPublic": true
  }' -w "\n%{http_code}")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" == "403" ] || [ "$HTTP_CODE" == "401" ]; then
  echo -e "${GREEN}✓ Correctly rejected request without token (HTTP $HTTP_CODE)${NC}"
else
  echo -e "${RED}❌ Unexpected response (HTTP $HTTP_CODE)${NC}"
fi
echo ""

# Test 6: Access protected endpoint with expired/invalid token
echo -e "${BLUE}TEST 6: Access with invalid token (should fail)${NC}"
RESPONSE=$(curl -s -X POST "$BASE_URL/gigs/me" \
  -H "Authorization: Bearer invalid.token.here" \
  -H "Content-Type: application/json" \
  -d '{
    "minPrice": 100.00,
    "aboutThisGig": "Test",
    "isPublic": true
  }' -w "\n%{http_code}")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" == "403" ] || [ "$HTTP_CODE" == "401" ]; then
  echo -e "${GREEN}✓ Correctly rejected invalid token (HTTP $HTTP_CODE)${NC}"
else
  echo -e "${RED}❌ Unexpected response (HTTP $HTTP_CODE)${NC}"
fi
echo ""

# Test 7: Get user's gigs with valid token
echo -e "${BLUE}TEST 7: Get user's gigs with valid LAWYER token${NC}"
RESPONSE=$(curl -s -X GET "$BASE_URL/gigs/me" \
  -H "Authorization: Bearer $LAWYER_TOKEN" \
  -w "\n%{http_code}")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" == "200" ]; then
  echo -e "${GREEN}✓ Successfully retrieved gigs (HTTP $HTTP_CODE)${NC}"
  echo "Number of gigs: $(echo $BODY | jq 'length')"
else
  echo -e "${RED}❌ Failed to retrieve gigs (HTTP $HTTP_CODE)${NC}"
fi
echo ""

# Test 8: Login with CLIENT user and try to create gig (should fail)
echo -e "${BLUE}TEST 8: CLIENT user tries to create gig (should fail - no LAWYER role)${NC}"
CLIENT_TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "password": "password123"
  }' | jq -r '.token')

if [ -z "$CLIENT_TOKEN" ] || [ "$CLIENT_TOKEN" == "null" ]; then
  echo -e "${BLUE}ℹ Client user not found (expected for test)${NC}"
else
  RESPONSE=$(curl -s -X POST "$BASE_URL/gigs/me" \
    -H "Authorization: Bearer $CLIENT_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "minPrice": 100.00,
      "aboutThisGig": "Test",
      "isPublic": true
    }' -w "\n%{http_code}")

  HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
  if [ "$HTTP_CODE" == "403" ]; then
    echo -e "${GREEN}✓ Correctly rejected CLIENT from creating gig (HTTP $HTTP_CODE)${NC}"
  else
    echo -e "${RED}❌ Unexpected response (HTTP $HTTP_CODE)${NC}"
  fi
fi
echo ""

# Test 9: Malformed Authorization header
echo -e "${BLUE}TEST 9: Malformed Authorization header (should fail)${NC}"
RESPONSE=$(curl -s -X GET "$BASE_URL/gigs/me" \
  -H "Authorization: InvalidFormat $LAWYER_TOKEN" \
  -w "\n%{http_code}")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
# This might succeed if filter is lenient, or fail if strict
echo "Response code: $HTTP_CODE"
echo ""

# Test 10: Missing Bearer prefix
echo -e "${BLUE}TEST 10: Authorization header without Bearer prefix${NC}"
RESPONSE=$(curl -s -X GET "$BASE_URL/gigs/me" \
  -H "Authorization: $LAWYER_TOKEN" \
  -w "\n%{http_code}")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" == "403" ] || [ "$HTTP_CODE" == "401" ]; then
  echo -e "${GREEN}✓ Correctly rejected token without Bearer prefix (HTTP $HTTP_CODE)${NC}"
else
  echo -e "${RED}❌ Unexpected response (HTTP $HTTP_CODE)${NC}"
fi
echo ""

echo "=================================="
echo "Test Suite Complete"
echo "=================================="
echo ""
echo "Notes:"
echo "- Tests 1-6: Core authentication/authorization"
echo "- Tests 7-8: Role-based access control"
echo "- Tests 9-10: Header validation"
echo ""
echo "Required setup:"
echo "1. Database with users table"
echo "2. Insert test users:"
echo "   - LAWYER: john@example.com / password123"
echo "   - CLIENT: jane@example.com / password123"
echo "3. Application running on http://localhost:8080"

