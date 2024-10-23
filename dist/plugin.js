var capacitorNrfMesh = (function (exports, core) {
    'use strict';

    const NrfMesh = core.registerPlugin('NrfMesh', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.NrfMeshWeb()),
    });

    class NrfMeshWeb extends core.WebPlugin {
        async checkPermissions() {
            console.log('checkPermissions');
            return { LOCATION: 'granted', BLUETOOTH: 'granted' };
        }
        async requestPermissions() {
            console.log('requestPermissions');
            return;
        }
        async isBluetoothEnabled() {
            console.log('isBluetoothEnabled');
            return { enabled: true };
        }
        async requestBluetoothEnable() {
            console.log('requestBluetoothEnabled');
            return { enabled: true };
        }
        async initMeshNetwork() {
            console.log('initMeshNetwork');
        }
        async getMeshNetwork() {
            console.log('getMeshNetwork');
            return {};
        }
        async exportMeshNetwork() {
            console.log('exportMeshNetwork');
            return {};
        }
        async createApplicationKey() {
            console.log('createApplicationKey');
            return {};
        }
        async removeApplicationKey() {
            console.log('removeApplicationKey');
        }
        async scanMeshDevices() {
            console.log('scanMeshDevices');
            return { unprovisioned: [], provisioned: [], proxy: [] };
        }
        async getProvisioningCapabilities() {
            console.log('getProvisioningCapabilities');
            return {};
        }
        async provisionDevice() {
            console.log('provisionDevice');
            return { provisioningComplete: true, uuid: '1234' };
        }
        async unprovisionDevice() {
            console.log('unprovisionDevice');
            return {};
        }
        async getCompositionData() {
            console.log('getCompositionData');
            return {};
        }
        async getDefaultTTL() {
            console.log('getDefaultTTL');
            return {};
        }
        async setDefaultTTL() {
            console.log('setDefaultTTL');
            return {};
        }
        async getNetworkTransmit() {
            console.log('getNetworkTransmit');
            return {};
        }
        async setNetworkTransmit() {
            console.log('setNetworkTransmit');
            return {};
        }
        async addAppKey() {
            console.log('addAppKey');
            return {};
        }
        async deleteAppKey() {
            console.log('deleteAppKey');
            return {};
        }
        async getAppKeys() {
            console.log('getAppKeys');
            return {};
        }
        async bindAppKey() {
            console.log('bindAppKey');
            return {};
        }
        async unbindAppKey() {
            console.log('unbindAppKey');
            return {};
        }
    }

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        NrfMeshWeb: NrfMeshWeb
    });

    exports.NrfMesh = NrfMesh;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
