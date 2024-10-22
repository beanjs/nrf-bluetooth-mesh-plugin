import { WebPlugin } from '@capacitor/core';
export class NrfMeshWeb extends WebPlugin {
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
    }
    async getCompositionData() {
        console.log('compositionDataGet');
    }
}
//# sourceMappingURL=web.js.map