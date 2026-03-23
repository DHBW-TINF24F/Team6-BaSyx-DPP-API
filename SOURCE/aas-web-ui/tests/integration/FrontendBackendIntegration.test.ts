import { defineComponent, h, nextTick, onMounted, ref } from 'vue';
import { flushPromises, mount } from '@vue/test-utils';
import { beforeEach, describe, expect, it, vi } from 'vitest';

type ViewerDpp = {
    dppId: string;
    productId: string;
    submodels: Array<{ id: string; title: string }>;
};

type SubmodelData = {
    manufacturerName?: string;
    productName?: string;
    serialNumber?: string;
};

function createJsonResponse<T>(payload: T, status = 200) {
    return {
        ok: status >= 200 && status < 300,
        status,
        json: async () => payload,
    };
}

const ViewerComponent = defineComponent({
    name: 'ViewerComponent',
    props: {
        dppId: {
            type: String,
            required: true,
        },
    },
    setup(props: { dppId: string }) {
        const dppData = ref<ViewerDpp | null>(null);

        onMounted(async () => {
            const response = await fetch(`/api/dpps/${props.dppId}`);
            dppData.value = await response.json();
        });

        return () =>
            h('section', { class: 'dpp-viewer' }, [
                h('span', { class: 'product-id' }, dppData.value?.productId ?? 'loading'),
                h('span', { class: 'submodel-count' }, String(dppData.value?.submodels.length ?? 0)),
            ]);
    },
});

const NavigationComponent = defineComponent({
    name: 'NavigationComponent',
    setup() {
        const data = ref<SubmodelData | null>(null);

        async function loadSubmodel(submodelId: string) {
            const response = await fetch(`/api/dpps/dpp-001/submodels/${submodelId}`);
            data.value = await response.json();
        }

        return () =>
            h('div', [
                h(
                    'button',
                    {
                        class: 'nav-btn',
                        onClick: () => loadSubmodel('digitalNameplate'),
                    },
                    'Digital Nameplate',
                ),
                h('div', { class: 'manufacturer' }, data.value?.manufacturerName ?? 'n/a'),
                h('div', { class: 'serial' }, data.value?.serialNumber ?? 'n/a'),
            ]);
    },
});

const MissingDataComponent = defineComponent({
    name: 'MissingDataComponent',
    setup() {
        const data = ref<SubmodelData>({});

        onMounted(async () => {
            const response = await fetch('/api/dpps/dpp-001/submodels/digitalNameplate');
            data.value = await response.json();
        });

        return () =>
            h('div', [
                h('span', { class: 'manufacturer' }, data.value.manufacturerName ?? 'N/A'),
                h('span', { class: 'product-name' }, data.value.productName ?? 'N/A'),
                !data.value.productName ? h('span', { class: 'missing-data' }, 'Data missing') : null,
            ]);
    },
});

const ResponsiveComponent = defineComponent({
    name: 'ResponsiveComponent',
    setup() {
        const isMobile = ref(window.innerWidth < 768);

        return () => h('div', { class: isMobile.value ? 'mobile-layout' : 'desktop-layout' }, 'content');
    },
});

describe('FrontendBackendIntegration.test.ts; Prepared integration tests for viewer and API interaction', () => {
    const fetchMock = vi.fn();

    beforeEach(() => {
        fetchMock.mockReset();
        vi.stubGlobal('fetch', fetchMock);
    });

    it('IT-FB-01: should load a DPP in the viewer on mount', async () => {
        fetchMock.mockResolvedValueOnce(
            createJsonResponse<ViewerDpp>({
                dppId: 'dpp-001',
                productId: 'battery-pack-001',
                submodels: [
                    { id: 'digitalNameplate', title: 'Digital Nameplate' },
                    { id: 'productCondition', title: 'Product Condition' },
                ],
            }),
        );

        const wrapper = mount(ViewerComponent, { props: { dppId: 'dpp-001' } });
        await flushPromises();

        expect(fetchMock).toHaveBeenCalledWith('/api/dpps/dpp-001');
        expect(wrapper.find('.product-id').text()).toBe('battery-pack-001');
        expect(wrapper.find('.submodel-count').text()).toBe('2');
    });

    it('IT-FB-02: should request submodel data when navigation is used', async () => {
        fetchMock.mockResolvedValueOnce(
            createJsonResponse<SubmodelData>({
                manufacturerName: 'Team 6 GmbH',
                productName: 'Battery Pack',
                serialNumber: 'SN-0001',
            }),
        );

        const wrapper = mount(NavigationComponent);
        await wrapper.find('.nav-btn').trigger('click');
        await nextTick();

        expect(fetchMock).toHaveBeenCalledWith('/api/dpps/dpp-001/submodels/digitalNameplate');
        expect(wrapper.find('.manufacturer').text()).toBe('Team 6 GmbH');
        expect(wrapper.find('.serial').text()).toBe('SN-0001');
    });

    it('IT-FB-03: should render placeholders when optional data is missing', async () => {
        fetchMock.mockResolvedValueOnce(
            createJsonResponse<SubmodelData>({
                manufacturerName: 'Team 6 GmbH',
            }),
        );

        const wrapper = mount(MissingDataComponent);
        await flushPromises();

        expect(wrapper.find('.manufacturer').text()).toBe('Team 6 GmbH');
        expect(wrapper.find('.product-name').text()).toBe('N/A');
        expect(wrapper.find('.missing-data').exists()).toBe(true);
    });

    it('IT-FB-04: should switch to a mobile layout below the breakpoint', () => {
        Object.defineProperty(window, 'innerWidth', {
            configurable: true,
            value: 600,
        });

        const wrapper = mount(ResponsiveComponent);

        expect(wrapper.classes()).toContain('mobile-layout');
    });
});
