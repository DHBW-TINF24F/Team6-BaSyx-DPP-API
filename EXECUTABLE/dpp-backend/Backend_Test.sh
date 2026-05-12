echo "Backend-Funktionen gestartet.. \n
    DPP wird erstellt für folgende AASX Datei: Team6DPPTestShell \n
    Global Asset ID: https://team6.dpp/asset/battery-proto-001"

curl -X POST http://localhost:8080/dpps \
-H "Content-Type: application/json" \
-d '{
  "shell": {
    "id": "https://team6.dpp/asset/battery-proto-001",
    "dpps": [
      {
        "productId": "https://team6.dpp/batterypass/proto-001",
        "version": "1.0.0"
      }
    ]
  }
}'

echo ""