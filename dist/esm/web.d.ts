import { WebPlugin } from '@capacitor/core';
import type { BluetoothState, NrfMeshPlugin, Permissions, MeshNetwork, MeshNetworkExport, MeshAppKey, ProvisioningCapabilities, ProvisioningStatus, ScanMeshDevices, NodeResetStatus, CompositionDataStatus, DefaultTTLStatus, NetworkTransmitStatus, AppKeyStatus, AppKeyListStatus, ModelAppStatus } from './definitions';
export declare class NrfMeshWeb extends WebPlugin implements NrfMeshPlugin {
    checkPermissions(): Promise<Permissions>;
    requestPermissions(): Promise<any>;
    isBluetoothEnabled(): Promise<BluetoothState>;
    requestBluetoothEnable(): Promise<BluetoothState>;
    initMeshNetwork(): Promise<void>;
    getMeshNetwork(): Promise<MeshNetwork>;
    exportMeshNetwork(): Promise<MeshNetworkExport>;
    importMeshNetwork(): Promise<void>;
    createApplicationKey(): Promise<MeshAppKey>;
    removeApplicationKey(): Promise<void>;
    scanMeshDevices(): Promise<ScanMeshDevices>;
    getProvisioningCapabilities(): Promise<ProvisioningCapabilities>;
    provisionDevice(): Promise<ProvisioningStatus>;
    unprovisionDevice(): Promise<NodeResetStatus>;
    getCompositionData(): Promise<CompositionDataStatus>;
    getDefaultTTL(): Promise<DefaultTTLStatus>;
    setDefaultTTL(): Promise<DefaultTTLStatus>;
    getNetworkTransmit(): Promise<NetworkTransmitStatus>;
    setNetworkTransmit(): Promise<NetworkTransmitStatus>;
    addAppKey(): Promise<AppKeyStatus>;
    deleteAppKey(): Promise<AppKeyStatus>;
    getAppKeys(): Promise<AppKeyListStatus>;
    bindAppKey(): Promise<ModelAppStatus>;
    unbindAppKey(): Promise<ModelAppStatus>;
}
