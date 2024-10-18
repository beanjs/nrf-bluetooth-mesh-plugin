import { WebPlugin } from '@capacitor/core';
export class NrfMeshWeb extends WebPlugin {
    async checkPermissions() {
        console.log('checkPermissions');
        return { 'LOCATION': 'granted', 'BLUETOOTH': 'granted' };
    }
    async requestPermissions() {
        console.log('requestPermissions');
        return;
    }
    async scanMeshDevices() {
        console.log('scanMeshDevices');
        return { unprovisioned: [], provisioned: [] };
    }
    async getProvisioningCapabilities() {
        console.log('getProvisioningCapabilities');
        return;
    }
    async provisionDevice() {
        console.log('provisionDevice');
        return { provisioningComplete: true, uuid: '1234' };
    }
    async unprovisionDevice() {
        console.log('unprovisionDevice');
    }
    async createApplicationKey() {
        console.log('createApplicationKey');
    }
    async removeApplicationKey() {
        console.log('removeApplicationKey');
    }
    async addApplicationKeyToNode() {
        console.log('addApplicationKeyToNode');
        return { success: true };
    }
    async bindApplicationKeyToModel() {
        console.log('bindApplicationKeyToModel');
    }
    async compositionDataGet() {
        console.log('compositionDataGet');
    }
    async sendGenericOnOffSet() {
        console.log('sendGenericOnOffSet');
        return { src: 1, dst: 2, opcode: 3, data: {} };
    }
    async sendGenericOnOffGet() {
        console.log('sendGenericOnOffSet');
        return { src: 1, dst: 2, opcode: 3, data: {} };
    }
    async sendGenericPowerLevelSet() {
        console.log('sendGenericPowerLevelSet');
        return { src: 1, dst: 2, opcode: 3, data: {} };
    }
    async sendGenericPowerLevelGet() {
        console.log('sendGenericPowerLevelGet');
        return { src: 1, dst: 2, opcode: 3, data: {} };
    }
    async sendLightHslSet() {
        console.log('sendLightHslSet');
        return { src: 1, dst: 2, opcode: 3, data: {} };
    }
    async sendLightHslGet() {
        console.log('sendLightHslGet');
        return { src: 1, dst: 2, opcode: 3, data: {} };
    }
    initMeshNetwork() {
        return Promise.resolve({ meshNetwork: 'meshNetwork' });
    }
    async exportMeshNetwork() {
        console.log('exportMeshNetwork');
        return { meshNetwork: 'meshNetwork' };
    }
    async importMeshNetwork() {
        console.log('importMeshNetwork');
    }
    async sendVendorModelMessage(options) {
        console.log('sendVendorModelMessage', options);
        return { src: 1, dst: 2, opcode: 3, data: {} };
    }
    sendLightCtlSet() {
        throw new Error('Method not implemented.');
    }
}
//# sourceMappingURL=web.js.map