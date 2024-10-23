import type { PluginListenerHandle } from '@capacitor/core';
export interface BluetoothState {
    enabled: boolean;
}
export interface Permissions {
    [key: string]: string;
}
export interface MeshNetwork {
    name: string;
    provisioners: [
        {
            name: string;
            ttl: number;
            unicastAddress?: number;
            unicast: [
                {
                    lowerAddress: number;
                    highAddress: number;
                    lowerBound: number;
                    upperBound: number;
                }
            ];
            group: [
                {
                    lowerAddress: number;
                    highAddress: number;
                    lowerBound: number;
                    upperBound: number;
                }
            ];
            scene: [
                {
                    firstScene: number;
                    lastScene: number;
                    lowerBound: number;
                    upperBound: number;
                }
            ];
        }
    ];
    netKeys: [
        {
            name: string;
            key: string;
            oldKey?: string;
            index: number;
            phase: number;
            security: 'secure' | 'insecure';
            lastModified: string;
        }
    ];
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
export interface Status {
    src: number;
    dst: number;
    opcode: number;
}
export interface NodeResetStatus extends Status {
    data: {
        status: number;
        statusName: string;
    };
}
export interface CompositionDataStatus extends Status {
    data: {
        status: number;
        statusName: string;
        companyIdentifier: string;
        productIdentifier: string;
        productVersion: string;
        nodeFeaturesSupported: {
            relay: boolean;
            proxy: boolean;
            friend: boolean;
            lowPower: boolean;
        };
        elements: [
            {
                name: string;
                elementAddress: number;
                sigModelCount: number;
                vendorModelCount: number;
                locationDescriptor: number;
                models: [
                    {
                        modelId: number;
                        modelName: string;
                        boundAppKeyIndexes: [];
                    }
                ];
            }
        ];
    };
}
export interface DefaultTTLStatus extends Status {
    data: {
        status: number;
        statusName: string;
        ttl: number;
    };
}
export interface NetworkTransmitStatus extends Status {
    data: {
        status: number;
        statusName: string;
        networkTransmitCount: number;
        networkTransmitIntervalSteps: number;
    };
}
export interface NrfMeshPlugin {
    checkPermissions(): Promise<Permissions>;
    requestPermissions(): Promise<Permissions>;
    isBluetoothEnabled(): Promise<BluetoothState>;
    requestBluetoothEnable(): Promise<BluetoothState>;
    initMeshNetwork(): Promise<void>;
    getMeshNetwork(): Promise<MeshNetwork>;
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
    unprovisionDevice(options: {
        unicastAddress: number;
    }): Promise<NodeResetStatus>;
    getCompositionData(options: {
        unicastAddress: number;
    }): Promise<CompositionDataStatus>;
    getDefaultTTL(options: {
        unicastAddress: number;
    }): Promise<DefaultTTLStatus>;
    setDefaultTTL(options: {
        unicastAddress: number;
        ttl: number;
    }): Promise<DefaultTTLStatus>;
    getNetworkTransmit(options: {
        unicastAddress: number;
    }): Promise<NetworkTransmitStatus>;
    setNetworkTransmit(options: {
        unicastAddress: number;
        networkTransmitCount: number;
        networkTransmitIntervalSteps: number;
    }): Promise<NetworkTransmitStatus>;
    addListener(eventName: string, listenerFunc: (event: any) => void): Promise<PluginListenerHandle>;
    removeAllListeners(): Promise<void>;
}
