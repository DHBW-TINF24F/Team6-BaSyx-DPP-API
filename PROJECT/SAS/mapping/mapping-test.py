from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import httpx
from typing import Optional, Dict, Any
import uvicorn
import base64
import json
from datetime import datetime
import logging

app = FastAPI(title="API Concept Tester")

# Enable CORS for frontend testing
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

DOCKER_SERVICE_URL = "http://localhost:8081"

@app.get("/")
def root():
    return {
        "message": "API Mapper running",
        "docker_service": DOCKER_SERVICE_URL,
        "docs": "/docs"
    }

@app.get("/dpps")
async def get_dpps(dppId: str):
    """
    Fetch all DPPs with input of dppId and return all versions with their submodels data
    """
    encoded_dppId = encode_to_base64(str(dppId))

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{DOCKER_SERVICE_URL}/submodels/{encoded_dppId}",
                timeout=10.0
            )
            response.raise_for_status()
            original_data = response.json()

            # Try different possible structures
            submodel_elements = None
            
            if isinstance(original_data, dict) and 'data' in original_data:
                submodel_elements = original_data['data'].get('submodelElements', [])
            elif isinstance(original_data, dict) and 'submodelElements' in original_data:
                submodel_elements = original_data.get('submodelElements', [])
            elif isinstance(original_data, list):
                submodel_elements = original_data
            
            if not submodel_elements:
                raise HTTPException(status_code=404, detail="No submodel elements found")
            
            def get_timestamp(element):
                """Extract and parse timestamp from SubmodelElementCollection"""
                if element.get('modelType') != 'SubmodelElementCollection':
                    return None
                
                for prop in element.get('value', []):
                    if prop.get('idShort') == 'DPPVersion' and prop.get('modelType') == 'Property':
                        timestamp_str = prop.get('value')
                        try:
                            return datetime.fromisoformat(timestamp_str.replace('Z', '+00:00'))
                        except (ValueError, TypeError, AttributeError):
                            try:
                                return datetime.strptime(timestamp_str, '%Y-%m-%d %H:%M:%S')
                            except (ValueError, TypeError):
                                return None
                return None
            
            # Process all SubmodelElementCollections
            all_dpps = []
            
            for element in submodel_elements:
                if element.get('modelType') != 'SubmodelElementCollection':
                    continue
                
                timestamp = get_timestamp(element)
                if not timestamp:
                    continue
                
                # Extract DPPSubmodels IDs for this DPP version
                submodel_ids = []
                for value_element in element.get('value', []):
                    if value_element.get('idShort') == 'DPPSubmodels' and value_element.get('modelType') == 'SubmodelElementCollection':
                        # Extract all Property elements from DPPSubmodels
                        for prop in value_element.get('value', []):
                            if prop.get('modelType') == 'Property':
                                submodel_id = prop.get('value')
                                if submodel_id:
                                    submodel_ids.append(submodel_id)
                        break
                
                # Fetch submodel data for each ID
                submodels_data = []
                for submodel_id in submodel_ids:
                    try:
                        encoded_submodel_id = encode_to_base64(submodel_id)
                        print(encoded_submodel_id)
                        submodel_response = await client.get(
                            f"{DOCKER_SERVICE_URL}/submodels/{encoded_submodel_id}/submodel-elements",
                            timeout=10.0
                        )
                        submodel_response.raise_for_status()
                        submodel_data = submodel_response.json()
                        
                        submodels_data.append({
                            "id": submodel_id,
                            "data": submodel_data
                        })
                    except (httpx.RequestError, httpx.HTTPStatusError) as e:
                        # Log error but continue with other submodels
                        submodels_data.append({
                            "id": submodel_id,
                            "error": f"Failed to fetch submodel: {str(e)}"
                        })
                
                # Build the DPP version object
                all_dpps.append({
                    "version": element.get('idShort'),
                    "timestamp": timestamp.isoformat(),
                    "submodels": submodels_data
                })
            
            if not all_dpps:
                raise HTTPException(status_code=404, detail="No valid DPP versions found")
            
            # Sort by timestamp (newest first)
            all_dpps.sort(key=lambda x: x['timestamp'], reverse=True)
            
            return {
                "data": all_dpps,
                "count": len(all_dpps)
            }

        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Docker service unavailable: {str(e)}")
        except httpx.HTTPStatusError as e:
            raise HTTPException(status_code=e.response.status_code, detail=str(e))

@app.get("/health")
async def health_check():
    """Check if Docker service is reachable"""
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{DOCKER_SERVICE_URL}/actuator/health",  # Adjust endpoint
                timeout=5.0
            )
            return {
                "mapper": "healthy",
                "docker_service": "healthy",
                "status_code": response.status_code
            }
        except Exception as e:
            return {
                "mapper": "healthy",
                "docker_service": "unavailable",
                "error": str(e)
            }


# --- Base64 Encoding/Decoding Helpers ---
def encode_to_base64(data: Any) -> str:
    """Encode any data to base64 string"""
    if isinstance(data, dict) or isinstance(data, list):
        data = json.dumps(data)
    if isinstance(data, str):
        data = data.encode('utf-8')
    return base64.b64encode(data).decode('utf-8')

def decode_from_base64(encoded: str) -> str:
    """Decode base64 string back to original format"""
    decoded_bytes = base64.b64decode(encoded)
    return decoded_bytes.decode('utf-8')

# --- main ---
if __name__ == "__main__":
    print("🚀 Starting API Concept Tester...")
    print("📖 Docs available at: http://localhost:9999/docs")
    uvicorn.run(app, host="0.0.0.0", port=9999)