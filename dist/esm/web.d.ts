import { WebPlugin } from '@capacitor/core';
import type { BluetoothState, NrfMeshPlugin, Permissions, ProvisioningCapabilities, ProvisioningStatus, ScanMeshDevices } from './definitions';
export declare class NrfMeshWeb extends WebPlugin implements NrfMeshPlugin {
    checkPermissions(): Promise<Permissions>;
    requestPermissions(): Promise<any>;
    isBluetoothEnabled(): Promise<BluetoothState>;
    requestBluetoothEnable(): Promise<BluetoothState>;
    initMeshNetwork(): Promise<void>;
    scanMeshDevices(): Promise<ScanMeshDevices>;
    getProvisioningCapabilities(): Promise<ProvisioningCapabilities>;
    provisionDevice(): Promise<ProvisioningStatus>;
    unprovisionDevice(): Promise<void>;
    getCompositionData(): Promise<any>;
}
