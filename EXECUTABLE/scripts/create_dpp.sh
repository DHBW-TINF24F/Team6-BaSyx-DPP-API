curl -X POST http://localhost:8080/dpps \
-H "Content-Type: application/json" \
-d '{
  "shell": {
    "id": "urn:uuid:7e51f712-429a-419a-9e8c-8f43c393850b",
    "dpps": [
      {
        "productId": "https://team6.dpp/batterypass/proto-001",
        "version": "1.0.0"
      }
    ]
  }
}'
