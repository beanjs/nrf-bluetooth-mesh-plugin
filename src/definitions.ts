import type { PluginListenerHandle } from '@capacitor/core';

export interface BluetoothState {
  enabled: boolean;
}

export interface Permissions {
  [key: string]: string;
}

export interface MeshAppKey {
  name: string;
  index: number;
  key: string;
  oldKey?: string;
  boundNetKeyIndex: number;
}

export interface MeshNetKey {
  name: string;
  key: string;
  oldKey?: string;
  index: number;
  phase: number;
  security: 'secure' | 'insecure';
  lastModified: string;
}

export interface MeshProvisioner {
  name: string;
  ttl: number;
  unicastAddress?: number;
  unicast: [
    {
      lowerAddress: number;
      highAddress: number;
    },
  ];
  group: [
    {
      lowerAddress: number;
      highAddress: number;
    },
  ];
  scene: [
    {
      firstScene: number;
      lastScene: number;
    },
  ];
}

export interface MeshNode {
  name: string;
  deviceKey: string;
  unicastAddress: number;
  security: string;
  ttl: number;
  excluded: boolean;
  features: {
    friend: number;
    lowPower: number;
    proxy: number;
    relay: number;
  };
  netKeys: Array<{
    index: number;
    updated: boolean;
  }>;
  appKeys: Array<{
    index: number;
    updated: boolean;
  }>;
  elements: Array<{
    name: string;
    elementAddress: number;
    location: number;
    models: Array<{
      modelId: number;
      bind: Array<number>;
      subscribe: Array<number>;
    }>;
  }>;

  networkTransmit?: {
    count: number;
    interval: number;
    steps: number;
  };

  cid?: string;
  pid?: string;
  vid?: string;
  crpl?: string;
}

export interface MeshNetwork {
  name: string;
  lastModified: string;
  provisioners: Array<MeshProvisioner>;
  netKeys: Array<MeshNetKey>;
  appKeys: Array<MeshAppKey>;
  nodes: Array<MeshNode>;
  networkExclusions: Array<{
    ivIndex: number;
    addresses: Array<number>;
  }>;
}

export interface MeshNetworkExport {
  meshNetwork: string;
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
  unprovisioned: Array<UnprovisionedDevice>;
  provisioned: Array<ProvisionedDevice>;
  proxy: Array<ProxyDevice>;
}

export interface ProvisioningCapabilities {
  numberOfElements: number;
  availableOOBTypes: Array<string>;
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
    elements: Array<{
      name: string;
      elementAddress: number;
      sigModelCount: number;
      vendorModelCount: number;
      location: number;
      models: Array<{
        modelId: number;
        modelName: string;
        boundAppKeyIndexes: Array<number>;
      }>;
    }>;
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

export interface AppKeyStatus extends Status {
  data: {
    status: number;
    statusName: string;
    netKeyIndex: number;
    appKeyIndex: number;
  };
}

export interface AppKeyListStatus extends Status {
  data: {
    status: number;
    statusName: string;
    netKeyIndex: number;
    appKeyIndexes: Array<number>;
  };
}

export interface ModelAppStatus extends Status {
  data: {
    status: number;
    statusName: string;
    elementAddress: number;
    modelId: number;
    appKeyIndex: number;
  };
}

export interface OnOffStatus extends Status {
  data: {
    onOff: boolean;
  };
}

export interface NrfMeshPlugin {
  checkPermissions(): Promise<Permissions>;
  requestPermissions(): Promise<Permissions>;
  isBluetoothEnabled(): Promise<BluetoothState>;
  requestBluetoothEnable(): Promise<BluetoothState>;
  initMeshNetwork(): Promise<void>;
  exportMeshNetwork(): Promise<MeshNetworkExport>;
  importMeshNetwork(network: MeshNetworkExport): Promise<void>;

  // getProvisioners(): Promise<Array<MeshProvisioner>>;
  getMeshNetwork(): Promise<MeshNetwork>;
  createAppKey(): Promise<MeshAppKey>;
  removeAppKey(options: { index: number }): Promise<void>;
  getNode(options: { unicastAddress: number }): Promise<MeshNode | undefined>;

  scanMeshDevices(options: { timeout: number }): Promise<ScanMeshDevices>;
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
  getDefaultTTL(options: { unicastAddress: number }): Promise<DefaultTTLStatus>;
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
  addAppKey(options: {
    unicastAddress: number;
    appKeyIndex: number;
  }): Promise<AppKeyStatus>;
  deleteAppKey(options: {
    unicastAddress: number;
    appKeyIndex: number;
  }): Promise<AppKeyStatus>;
  getAppKeys(options: { unicastAddress: number }): Promise<AppKeyListStatus>;
  bindAppKey(options: {
    unicastAddress: number;
    elementAddress: number;
    modelId: number;
    appKeyIndex: number;
  }): Promise<ModelAppStatus>;
  unbindAppKey(options: {
    unicastAddress: number;
    elementAddress: number;
    modelId: number;
    appKeyIndex: number;
  }): Promise<ModelAppStatus>;

  getOnOff(options: {
    elementAddress: number;
    appKeyIndex: number;
  }): Promise<OnOffStatus>;
  setOnOff(options: {
    elementAddress: number;
    appKeyIndex: number;
    onOff?: boolean; // default false
    acknowledgement?: boolean; // default true
  }): Promise<OnOffStatus>;

  // sendGenericOnOffSet(options: {
  //   unicastAddress: number;
  //   appKeyIndex: number;
  //   onOff: boolean;
  //   acknowledgement?: boolean;
  // }): Promise<ModelMessageStatus | PluginCallRejection>;
  // sendGenericOnOffGet(options: {
  //   unicastAddress: number;
  //   appKeyIndex: number;
  // }): Promise<ModelMessageStatus | PluginCallRejection>;
  // sendGenericPowerLevelSet(options: {
  //   unicastAddress: number;
  //   appKeyIndex: number;
  //   powerLevel: number;
  // }): Promise<ModelMessageStatus | PluginCallRejection>;
  // sendGenericPowerLevelGet(options: {
  //   unicastAddress: number;
  //   appKeyIndex: number;
  // }): Promise<ModelMessageStatus | PluginCallRejection>;
  // sendLightHslSet(options: {
  //   unicastAddress: number;
  //   appKeyIndex: number;
  //   hue: number;
  //   saturation: number;
  //   lightness: number;
  // }): Promise<ModelMessageStatus | PluginCallRejection>;
  // sendLightHslGet(options: {
  //   unicastAddress: number;
  //   appKeyIndex: number;
  // }): Promise<ModelMessageStatus | PluginCallRejection>;
  // sendLightCtlSet(options: {
  //   unicastAddress: number;
  //   appKeyIndex: number;
  //   lightness: number;
  //   temperature: number;
  //   deltaUv: number;
  // }): Promise<ModelMessageStatus | PluginCallRejection>;
  // sendVendorModelMessage(options: {
  //   unicastAddress: number;
  //   appKeyIndex: number;
  //   modelId: number;
  //   opcode: number;
  //   payload?: Uint8Array;
  //   opPairCode?: number;
  // }): Promise<ModelMessageStatus | PluginCallRejection>;
  // exportMeshNetwork(): Promise<MeshNetworkObject>;
  // importMeshNetwork(options: { meshNetwork: string }): Promise<void>;
  addListener(
    eventName: string,
    listenerFunc: (event: any) => void,
  ): Promise<PluginListenerHandle>;
  removeAllListeners(): Promise<void>;
}
