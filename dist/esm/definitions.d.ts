import type { PluginListenerHandle } from '@capacitor/core';
export interface BluetoothState {
    enabled: boolean;
}
export interface Permissions {
    [key: string]: string;
}
export interface UnprovisionedDevice {
    uuid: string;
    name: string;
    rssi: number;
    macAddress: string;
}
export interface ProxyDevice {
    name: string;
    rssi: number;
    macAddress: string;
}
export interface ProvisionedDevice {
    name: string;
    provisionedTime: string;
    unicastAddress: number;
    security: boolean;
    deviceKey: string;
    companyIdentifier?: string;
    productIdentifier?: string;
    productVersion?: string;
    replayProtectionCount?: string;
    nodeFeaturesSupported?: {
        relay: boolean;
        proxy: boolean;
        friend: boolean;
        lowPower: boolean;
    };
    nodeFeatures?: {
        relay: boolean;
        proxy: boolean;
        friend: boolean;
        lowPower: boolean;
    };
}
export interface ScanMeshDevices {
    unprovisioned: UnprovisionedDevice[];
    provisioned: ProvisionedDevice[];
    proxy: ProxyDevice[];
}
export interface ProvisioningCapabilities {
    numberOfElements: number;
    availableOOBTypes: string[];
    algorithms: number;
    publicKeyType: number;
    staticOobTypes: number;
    outputOobSize: number;
    outputOobActions: number;
    inputOobSize: number;
    inputOobActions: number;
}
export interface ProvisioningStatus {
    provisioningComplete: boolean;
    uuid: string;
    unicastAddress?: number;
}
export interface NrfMeshPlugin {
    checkPermissions(): Promise<Permissions>;
    requestPermissions(): Promise<Permissions>;
    isBluetoothEnabled(): Promise<BluetoothState>;
    requestBluetoothEnable(): Promise<BluetoothState>;
    initMeshNetwork(): Promise<void>;
    scanMeshDevices(options: {
        timeout: number;
    }): Promise<ScanMeshDevices>;
    getProvisioningCapabilities(options: {
        macAddress: string;
        uuid: string;
    }): Promise<ProvisioningCapabilities>;
    provisionDevice(options: {
        macAddress: string;
        uuid: string;
    }): Promise<ProvisioningStatus>;
    addListener(eventName: string, listenerFunc: (event: any) => void): Promise<PluginListenerHandle>;
    removeAllListeners(): Promise<void>;
}
