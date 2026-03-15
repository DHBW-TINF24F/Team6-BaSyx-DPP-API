import { beforeEach, describe, expect, it, vi } from 'vitest';

type DppPayload = {
    productId: string;
    name: string;
    submodels: Array<{ id: string; semanticId?: string; data: Record<string, unknown> }>;
};

type DppResponse = {
    dppId: string;
    productId: string;
    version?: string;
    timestamp?: string;
    submodels?: Array<{ id: string; data: Record<string, unknown> }>;
};

type JsonResponse<T> = {
    ok: boolean;
    status: number;
    json: () => Promise<T>;
};

function createJsonResponse<T>(payload: T, status = 200): JsonResponse<T> {
    return {
        ok: status >= 200 && status < 300,
        status,
        json: async () => payload,
    };
}

function createDppApiClient(baseUrl = '/api/dpps') {
    return {
        async createDpp(payload: DppPayload) {
            const response = await fetch(baseUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });

            return response.json();
        },
        async getDppById(dppId: string) {
            const response = await fetch(`${baseUrl}/${dppId}`);
            return response.json();
        },
        async getHistoricalDpp(productId: string, timestamp: string) {
            const query = new URLSearchParams({ productId, timestamp });
            const response = await fetch(`${baseUrl}?${query.toString()}`);
            return response.json();
        },
        async updateElement(dppId: string, elementPath: string, value: Record<string, unknown>) {
            const response = await fetch(`${baseUrl}/${dppId}/elements/${elementPath}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(value),
            });

            return response.json();
        },
        async getRawResponse(url: string) {
            return fetch(url);
        },
    };
}

describe('DppApiIntegration.test.ts; Prepared integration tests for the planned DPP API', () => {
    const fetchMock = vi.fn();
    const apiClient = createDppApiClient();

    beforeEach(() => {
        fetchMock.mockReset();
        vi.stubGlobal('fetch', fetchMock);
    });

    it('IT-API-01: should create a DPP and send the expected POST payload', async () => {
        const payload: DppPayload = {
            productId: 'battery-pack-001',
            name: 'Battery Pack',
            submodels: [
                {
                    id: 'digitalNameplate',
                    semanticId: 'IDTA-02035-1',
                    data: { manufacturerName: 'Team 6 GmbH' },
                },
            ],
        };

        fetchMock.mockResolvedValueOnce(createJsonResponse({ dppId: 'dpp-001', productId: payload.productId }, 201));

        const result = await apiClient.createDpp(payload);

        expect(fetchMock).toHaveBeenCalledWith('/api/dpps', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
        });
        expect(result).toEqual({ dppId: 'dpp-001', productId: 'battery-pack-001' });
    });

    it('IT-API-02: should retrieve a DPP by id and validate the returned structure', async () => {
        const responsePayload: DppResponse = {
            dppId: 'dpp-001',
            productId: 'battery-pack-001',
            submodels: [
                {
                    id: 'digitalNameplate',
                    data: { manufacturerName: 'Team 6 GmbH' },
                },
            ],
        };

        fetchMock.mockResolvedValueOnce(createJsonResponse(responsePayload));

        const result = await apiClient.getDppById('dpp-001');

        expect(fetchMock).toHaveBeenCalledWith('/api/dpps/dpp-001');
        expect(result.dppId).toBe('dpp-001');
        expect(result.submodels).toHaveLength(1);
    });

    it('IT-API-03: should request a historical DPP version via query parameters', async () => {
        fetchMock.mockResolvedValueOnce(
            createJsonResponse<DppResponse>({
                dppId: 'dpp-001',
                productId: 'battery-pack-001',
                version: '1.0.0',
                timestamp: '2025-01-01T00:00:00Z',
            }),
        );

        const result = await apiClient.getHistoricalDpp('battery-pack-001', '2025-01-01T00:00:00Z');

        expect(fetchMock).toHaveBeenCalledWith('/api/dpps?productId=battery-pack-001&timestamp=2025-01-01T00%3A00%3A00Z');
        expect(result.version).toBe('1.0.0');
    });

    it('IT-API-04: should update a single element using the expected endpoint contract', async () => {
        fetchMock.mockResolvedValueOnce(createJsonResponse({ success: true }));

        const result = await apiClient.updateElement('dpp-001', 'digitalNameplate.manufacturerName', {
            value: 'Updated Manufacturer',
        });

        expect(fetchMock).toHaveBeenCalledWith('/api/dpps/dpp-001/elements/digitalNameplate.manufacturerName', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ value: 'Updated Manufacturer' }),
        });
        expect(result).toEqual({ success: true });
    });

    it('IT-API-05: should preserve backend error responses for invalid requests', async () => {
        fetchMock.mockResolvedValueOnce(createJsonResponse({ error: 'Invalid dppId' }, 400));

        const response = await apiClient.getRawResponse('/api/dpps/invalid-id');
        const errorPayload = await response.json();

        expect(response.ok).toBe(false);
        expect(response.status).toBe(400);
        expect(errorPayload).toEqual({ error: 'Invalid dppId' });
    });
});
