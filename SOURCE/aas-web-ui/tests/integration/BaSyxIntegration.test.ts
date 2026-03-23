import { beforeEach, describe, expect, it, vi } from 'vitest';

type DppSubmodel = {
    id: string;
    semanticId: string;
    data: Record<string, unknown>;
};

type DppRecord = {
    dppId: string;
    productId: string;
    submodels: DppSubmodel[];
};

type AasRecord = {
    id: string;
    idShort: string;
    submodels: Array<{ idShort: string; semanticId: string }>;
};

type RegistryEntry = {
    dppId: string;
    aasId: string;
    endpoint: string;
};

type DiscoveryRequest = {
    productId: string;
};

type BaSyxGateway = {
    createAas: ReturnType<typeof vi.fn>;
    registerAas: ReturnType<typeof vi.fn>;
    discoverByProductId: ReturnType<typeof vi.fn>;
};

function createBaSyxGateway(): BaSyxGateway {
    return {
        createAas: vi.fn(),
        registerAas: vi.fn(),
        discoverByProductId: vi.fn(),
    };
}

async function persistDppAsAas(gateway: BaSyxGateway, dpp: DppRecord): Promise<AasRecord> {
    return gateway.createAas({
        id: `aas-${dpp.dppId}`,
        idShort: `DPP-${dpp.productId}`,
        submodels: dpp.submodels.map((submodel) => ({
            idShort: submodel.id,
            semanticId: submodel.semanticId,
        })),
    });
}

async function registerDpp(gateway: BaSyxGateway, entry: RegistryEntry) {
    return gateway.registerAas(entry);
}

async function discoverDpp(gateway: BaSyxGateway, request: DiscoveryRequest) {
    return gateway.discoverByProductId(request);
}

describe('BaSyxIntegration.test.ts; Prepared orchestration tests for planned BaSyx connectivity', () => {
    let gateway: BaSyxGateway;

    beforeEach(() => {
        gateway = createBaSyxGateway();
    });

    it('IT-BX-01: should transform DPP data into the expected AAS structure', async () => {
        const dpp: DppRecord = {
            dppId: 'dpp-001',
            productId: 'battery-pack-001',
            submodels: [
                {
                    id: 'digitalNameplate',
                    semanticId: 'IDTA-02035-1',
                    data: { manufacturerName: 'Team 6 GmbH' },
                },
                {
                    id: 'productCondition',
                    semanticId: 'IDTA-02035-5',
                    data: { healthState: 'good' },
                },
            ],
        };

        gateway.createAas.mockResolvedValue({
            id: 'aas-dpp-001',
            idShort: 'DPP-battery-pack-001',
            submodels: [
                { idShort: 'digitalNameplate', semanticId: 'IDTA-02035-1' },
                { idShort: 'productCondition', semanticId: 'IDTA-02035-5' },
            ],
        } satisfies AasRecord);

        const result = await persistDppAsAas(gateway, dpp);

        expect(gateway.createAas).toHaveBeenCalledWith({
            id: 'aas-dpp-001',
            idShort: 'DPP-battery-pack-001',
            submodels: [
                { idShort: 'digitalNameplate', semanticId: 'IDTA-02035-1' },
                { idShort: 'productCondition', semanticId: 'IDTA-02035-5' },
            ],
        });
        expect(result.submodels).toHaveLength(2);
    });

    it('IT-BX-02: should prepare a registry entry for later BaSyx registration', async () => {
        const entry: RegistryEntry = {
            dppId: 'dpp-001',
            aasId: 'aas-dpp-001',
            endpoint: 'http://localhost:8081/shells/aas-dpp-001',
        };

        gateway.registerAas.mockResolvedValue({ success: true });

        const result = await registerDpp(gateway, entry);

        expect(gateway.registerAas).toHaveBeenCalledWith(entry);
        expect(result).toEqual({ success: true });
    });

    it('IT-BX-03: should prepare discovery queries for product based lookup', async () => {
        gateway.discoverByProductId.mockResolvedValue([
            {
                dppId: 'dpp-001',
                endpoint: 'http://localhost:8081/shells/aas-dpp-001',
            },
        ]);

        const result = await discoverDpp(gateway, { productId: 'battery-pack-001' });

        expect(gateway.discoverByProductId).toHaveBeenCalledWith({ productId: 'battery-pack-001' });
        expect(result).toHaveLength(1);
        expect(result[0].dppId).toBe('dpp-001');
    });
});
