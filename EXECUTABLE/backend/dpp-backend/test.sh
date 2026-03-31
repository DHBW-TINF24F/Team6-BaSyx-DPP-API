#!/bin/bash

# =====================
# Konfiguration
# =====================
BASE_URL="http://localhost:8080"
DPP_ID="urn:uuid:test-dpp-1"
PRODUCT_ID="urn:uuid:prod-123"
DATE="2026-03-31T00:00:00Z"
HARTING_DPP="https://dpp40.harting.com/shells/02095002010200"

# Funktion: schöner Header
function header() {
    echo
    echo "====================
$1
====================
"
}

# =====================
# Health Check
# =====================
header "Health Check"
curl -s "$BASE_URL/api/v1/dpp/health" | jq .

# =====================
# List DPPs
# =====================
header "List DPPs (limit=10)"
curl -s "$BASE_URL/api/v1/dpp/list?limit=10" | jq .

# =====================
# Get DPP by URL
# =====================
#header "Get DPP by URL (HARTING)"
#curl -s "$BASE_URL/api/v1/dpp?id=$HARTING_DPP" | jq .

# =====================
# Create DPP
# =====================
header "Create DPP"
curl -s -X POST "$BASE_URL/dpps" \
     -H "Content-Type: application/json" \
     -d "{\"dppId\": \"$DPP_ID\"}" | jq .

# =====================
# Get DPP by ID
# =====================
header "Get DPP by ID"
curl -s "$BASE_URL/dpps/$DPP_ID" | jq .

# =====================
# Update DPP by ID (PATCH)
# =====================
header "Update DPP by ID (PATCH)"
curl -s -X PATCH "$BASE_URL/dpps/$DPP_ID" \
     -H "Content-Type: application/json" \
     -d "{\"description\": \"Updated via test script\"}" | jq .

# =====================
# Delete DPP by ID
# =====================
header "Delete DPP by ID"
curl -s -X DELETE "$BASE_URL/dpps/$DPP_ID" \
     -w "HTTP: %{http_code}\n" | head -n -1

# =====================
# Get DPP by Product ID
# =====================
header "Get DPP by Product ID"
curl -s "$BASE_URL/dppsByProductId/$PRODUCT_ID" | jq .

# =====================
# Get DPP by Product ID and Date
# =====================
header "Get DPP version by Product ID and Date"
curl -s "$BASE_URL/dppsByProductIdAndDate/$PRODUCT_ID?date=$DATE" | jq .

# =====================
# Get DPP IDs by Product IDs
# =====================
header "Get DPP IDs by Product IDs"
curl -s -X POST "$BASE_URL/dppsByProductIds" \
     -H "Content-Type: application/json" \
     -d "{\"productIds\": [\"$PRODUCT_ID\"]}" | jq .

# =====================
# Register DPP
# =====================
header "Register DPP"
curl -s -X POST "$BASE_URL/registerDPP" \
     -H "Content-Type: application/json" \
     -d "{}" | jq .

echo "====================
Tests complete
====================
"
