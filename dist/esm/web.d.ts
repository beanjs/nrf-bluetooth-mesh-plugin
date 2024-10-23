import { WebPlugin } from '@capacitor/core';
import type { BluetoothState, NrfMeshPlugin, Permissions, MeshNetwork, ProvisioningCapabilities, ProvisioningStatus, ScanMeshDevices, NodeResetStatus, CompositionDataStatus, DefaultTTLStatus, NetworkTransmitStatus } from './definitions';
export declare class NrfMeshWeb extends WebPlugin implements NrfMeshPlugin {
    checkPermissions(): Promise<Permissions>;
    requestPermissions(): Promise<any>;
    isBluetoothEnabled(): Promise<BluetoothState>;
    requestBluetoothEnable(): Promise<BluetoothState>;
    initMeshNetwork(): Promise<void>;
    getMeshNetwork(): Promise<MeshNetwork>;
    scanMeshDevices(): Promise<ScanMeshDevices>;
    getProvisioningCapabilities(): Promise<ProvisioningCapabilities>;
    provisionDevice(): Promise<ProvisioningStatus>;
    unprovisionDevice(): Promise<NodeResetStatus>;
    getCompositionData(): Promise<CompositionDataStatus>;
    getDefaultTTL(): Promise<DefaultTTLStatus>;
    setDefaultTTL(): Promise<DefaultTTLStatus>;
    getNetworkTransmit(): Promise<NetworkTransmitStatus>;
    setNetworkTransmit(): Promise<NetworkTransmitStatus>;
}
