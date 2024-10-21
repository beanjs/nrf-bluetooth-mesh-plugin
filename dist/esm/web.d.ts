import { WebPlugin } from '@capacitor/core';
import type { AddAppKeyStatus, BluetoothState, MeshNetworkObject, ModelMessageStatus, NrfMeshPlugin, Permissions, PluginCallRejection, ProvisioningCapabilities, ProvisioningStatus, ScanMeshDevices } from './definitions';
export declare class NrfMeshWeb extends WebPlugin implements NrfMeshPlugin {
    isBluetoothEnabled(): Promise<BluetoothState>;
    requestBluetoothEnable(): Promise<BluetoothState>;
    checkPermissions(): Promise<Permissions>;
    requestPermissions(): Promise<any>;
    scanMeshDevices(): Promise<ScanMeshDevices>;
    getProvisioningCapabilities(): Promise<ProvisioningCapabilities | void>;
    provisionDevice(): Promise<ProvisioningStatus>;
    unprovisionDevice(): Promise<void>;
    createApplicationKey(): Promise<void>;
    removeApplicationKey(): Promise<void>;
    addApplicationKeyToNode(): Promise<AddAppKeyStatus>;
    bindApplicationKeyToModel(): Promise<void>;
    compositionDataGet(): Promise<void>;
    sendGenericOnOffSet(): Promise<ModelMessageStatus | PluginCallRejection>;
    sendGenericOnOffGet(): Promise<ModelMessageStatus | PluginCallRejection>;
    sendGenericPowerLevelSet(): Promise<ModelMessageStatus | PluginCallRejection>;
    sendGenericPowerLevelGet(): Promise<ModelMessageStatus | PluginCallRejection>;
    sendLightHslSet(): Promise<ModelMessageStatus | PluginCallRejection>;
    sendLightHslGet(): Promise<ModelMessageStatus | PluginCallRejection>;
    initMeshNetwork(): Promise<MeshNetworkObject>;
    exportMeshNetwork(): Promise<MeshNetworkObject>;
    importMeshNetwork(): Promise<void>;
    sendVendorModelMessage(options: {
        unicastAddress: number;
        appKeyIndex: number;
        modelId: number;
        companyIdentifier: number;
        opcode: number;
        payload: Uint8Array;
    }): Promise<ModelMessageStatus | PluginCallRejection>;
    sendLightCtlSet(): Promise<ModelMessageStatus | PluginCallRejection>;
}
