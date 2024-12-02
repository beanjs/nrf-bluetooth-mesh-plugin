import type { PluginListenerHandle } from '@capacitor/core';

export interface BluetoothState {
  enabled: boolean;
}

export interface BluetoothConnectionState {
  connected: boolean;
  macAddress?: string;
  isProxy?: boolean;
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

export interface MeshGroup {
  name: string;
  address: number;
  devices: number;
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
      publish: number;
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

export interface ModelSubscribeStatus extends Status {
  data: {
    status: number;
    statusName: string;
    elementAddress: number;
    subscriptionAddress: number;
    modelIdentifier: number;
  };
}

export interface ModelPublishStatus extends Status {
  status: number;
  statusName: string;
  elementAddress: number;
  publishAddress: number;
  appKeyIndex: number;
  credentialFlag: boolean;
  publishTtl: number;
  publicationSteps: number;
  publicationResolution: number;
  publishRetransmitCount: number;
  modelId: number;
}

export interface OnOffStatus extends Status {
  data: {
    onOff: boolean;
  };
}

export interface SensorStatus extends Status {
  data: Array<number>;
}

export interface SensorDescriptorStatus extends Status {
  data: Array<{
    propertyId: number;
    positiveTolerance: number;
    negativeTolerance: number;
    samplingFunction: number;
    measurementPeriod: number;
    updateInterval: number;
  }>;
}

export interface SensorColumnStatus extends Status {
  data: {
    propertyId: number;
    columns: Array<number>;
  };
}

export interface SensorSeriesStatus extends Status {
  data: {
    propertyId: number;
    series: Array<number>;
  };
}

export interface SensorCadenceStatus extends Status {
  data: {
    propertyId: number;
    periodDivisor?: number;
    triggerType?: number;
    minInterval?: number;
    triggerDeltaDown?: Array<number>;
    triggerDeltaUp?: Array<number>;
    fastCadenceLow?: Array<number>;
    fastCadenceHigh?: Array<number>;
  };
}

export interface SensorSettingsStatus extends Status {
  data: {
    propertyId: number;
    settings: Array<number>;
  };
}

export interface SensorSettingStatus extends Status {
  data: {
    propertyId: number;
    sensorSettingPropertyId: number;
    sensorSettingAccess?: number;
    sensorSetting?: Array<number>;
  };
}

export interface NrfMeshPlugin {
  checkPermissions(): Promise<Permissions>;
  requestPermissions(): Promise<Permissions>;
  isBluetoothEnabled(): Promise<BluetoothState>;
  requestBluetoothEnable(): Promise<BluetoothState>;
  isConnected(): Promise<BluetoothConnectionState>;
  disconnect(): Promise<void>;

  initMeshNetwork(): Promise<void>;
  exportMeshNetwork(): Promise<MeshNetworkExport>;
  importMeshNetwork(network: MeshNetworkExport): Promise<void>;

  getMeshNetwork(): Promise<MeshNetwork>;
  createAppKey(): Promise<MeshAppKey>;
  removeAppKey(options: { index: number }): Promise<void>;
  getNode(options: { unicastAddress: number }): Promise<MeshNode | undefined>;
  createGroup(options: { name: string }): Promise<MeshGroup>;
  removeGroup(options: { groupAddress: number }): Promise<void>;
  getGroup(options: { groupAddress: number }): Promise<MeshGroup | undefined>;

  scanMeshDevices(options: {
    timeout?: number;
    provisionedOnly?: boolean;
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
  delAppKey(options: {
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
  subscribe(options: {
    unicastAddress: number;
    elementAddress: number;
    subscriptionAddress: number;
    modelId: number;
  }): Promise<ModelSubscribeStatus>;
  unsubscribe(options: {
    unicastAddress: number;
    elementAddress: number;
    subscriptionAddress: number;
    modelId: number;
  }): Promise<ModelSubscribeStatus>;
  unsubscribeAll(options: {
    unicastAddress: number;
    elementAddress: number;
    subscriptionAddress: number;
  }): Promise<ModelSubscribeStatus>;
  publish(options: {
    unicastAddress: number;
    elementAddress: number;
    modelId: number;
    appKeyIndex?: number;
    publishAddress?: number;
    credentialFlag?: boolean;
    publishTtl?: boolean;
    publicationSteps?: boolean;
    publicationResolution?: boolean;
    retransmitCount?: boolean;
    retransmitIntervalSteps?: boolean;
  }): Promise<ModelPublishStatus>;

  getOnOff(options: {
    elementAddress: number;
    appKeyIndex: number;
  }): Promise<OnOffStatus>;
  setOnOff(options: {
    elementAddress: number;
    appKeyIndex: number;
    onOff?: boolean; // default false
    acknowledgement?: boolean; // default true
    transitionSteps?: number;
    transitionResolution?: number;
    delay?: number;
  }): Promise<OnOffStatus>;

  getSensor(options: {
    elementAddress: number;
    appKeyIndex: number;
    propertyId?: number;
  }): Promise<SensorStatus>;
  getSensorDescriptor(options: {
    elementAddress: number;
    appKeyIndex: number;
    propertyId?: number;
  }): Promise<SensorDescriptorStatus>;
  getSensorColumn(options: {
    elementAddress: number;
    appKeyIndex: number;
    propertyId: number;
    rawValueX: Array<number>;
  }): Promise<SensorColumnStatus>;
  getSensorSeries(options: {
    elementAddress: number;
    appKeyIndex: number;
    propertyId: number;
    rawValueX1?: Array<number>;
    rawValueX2?: Array<number>;
  }): Promise<SensorSeriesStatus>;
  getSensorCadence(options: {
    elementAddress: number;
    appKeyIndex: number;
    propertyId: number;
  }): Promise<SensorCadenceStatus>;
  getSensorSettings(options: {
    elementAddress: number;
    appKeyIndex: number;
    propertyId: number;
  }): Promise<SensorSettingsStatus>;
  getSensorSetting(options: {
    elementAddress: number;
    appKeyIndex: number;
    propertyId: number;
    sensorSettingPropertyId: number;
  }): Promise<SensorSettingStatus>;
  setSensorSetting(options: {
    elementAddress: number;
    appKeyIndex: number;
    propertyId: number;
    sensorSettingPropertyId: number;
    values: Array<number>;
    acknowledgement?: boolean;
  }): Promise<SensorSettingStatus>;

  addListener(
    event: string,
    callback: (arg: any) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'model',
    callback: (arg: any) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'adapter',
    callback: (arg: { enabled: boolean }) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'connection',
    callback: (arg: { connected: boolean }) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'node',
    callback: (arg: { action: 'delete'; unicastAddress: number }) => void,
  ): Promise<PluginListenerHandle>;
  removeAllListeners(): Promise<void>;
}
