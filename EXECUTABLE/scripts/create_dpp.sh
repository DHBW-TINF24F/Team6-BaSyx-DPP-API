curl -X POST http://localhost:8080/dpps \
-H "Content-Type: application/json" \
-d '{
  "shell": {
    "id": "urn:uuid:7e51f712-429a-419a-9e8c-8f43c393850b",
    "dpps": [
      {
        "productId": "PID",
        "version": "1.0.0",
        "submodels": [
          {
            "name": "CarbonFootPrint",
            "version": "1.2.0",
            "reference": "urn:submodel:cfp:001"
          },
          {
            "name": "DigitalNamePlate",
            "version": "1.0.1",
            "reference": "urn:submodel:dnp:002"
          }
        ]
      }
    ]
  }
}'
