# nrf-bluetooth-mesh

Capacitor plugin for Bluetooth Mesh, based on nRF Mesh Libraries

## Install

```bash
npm install nrf-bluetooth-mesh
npx cap sync
```

## API

<docgen-index>

* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [`isBluetoothEnabled()`](#isbluetoothenabled)
* [`requestBluetoothEnable()`](#requestbluetoothenable)
* [`initMeshNetwork()`](#initmeshnetwork)
* [`scanMeshDevices(...)`](#scanmeshdevices)
* [`getProvisioningCapabilities(...)`](#getprovisioningcapabilities)
* [`provisionDevice(...)`](#provisiondevice)
* [`unprovisionDevice(...)`](#unprovisiondevice)
* [`getCompositionData(...)`](#getcompositiondata)
* [`addListener(string, ...)`](#addlistenerstring-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### checkPermissions()

```typescript
checkPermissions() => Promise<Permissions>
```

**Returns:** <code>Promise&lt;<a href="#permissions">Permissions</a>&gt;</code>

--------------------


### requestPermissions()

```typescript
requestPermissions() => Promise<Permissions>
```

**Returns:** <code>Promise&lt;<a href="#permissions">Permissions</a>&gt;</code>

--------------------


### isBluetoothEnabled()

```typescript
isBluetoothEnabled() => Promise<BluetoothState>
```

**Returns:** <code>Promise&lt;<a href="#bluetoothstate">BluetoothState</a>&gt;</code>

--------------------


### requestBluetoothEnable()

```typescript
requestBluetoothEnable() => Promise<BluetoothState>
```

**Returns:** <code>Promise&lt;<a href="#bluetoothstate">BluetoothState</a>&gt;</code>

--------------------


### initMeshNetwork()

```typescript
initMeshNetwork() => Promise<void>
```

--------------------


### scanMeshDevices(...)

```typescript
scanMeshDevices(options: { timeout: number; }) => Promise<ScanMeshDevices>
```

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ timeout: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#scanmeshdevices">ScanMeshDevices</a>&gt;</code>

--------------------


### getProvisioningCapabilities(...)

```typescript
getProvisioningCapabilities(options: { macAddress: string; uuid: string; }) => Promise<ProvisioningCapabilities>
```

| Param         | Type                                               |
| ------------- | -------------------------------------------------- |
| **`options`** | <code>{ macAddress: string; uuid: string; }</code> |

**Returns:** <code>Promise&lt;<a href="#provisioningcapabilities">ProvisioningCapabilities</a>&gt;</code>

--------------------


### provisionDevice(...)

```typescript
provisionDevice(options: { macAddress: string; uuid: string; }) => Promise<ProvisioningStatus>
```

| Param         | Type                                               |
| ------------- | -------------------------------------------------- |
| **`options`** | <code>{ macAddress: string; uuid: string; }</code> |

**Returns:** <code>Promise&lt;<a href="#provisioningstatus">ProvisioningStatus</a>&gt;</code>

--------------------


### unprovisionDevice(...)

```typescript
unprovisionDevice(options: { unicastAddress: number; }) => Promise<void>
```

| Param         | Type                                     |
| ------------- | ---------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; }</code> |

--------------------


### getCompositionData(...)

```typescript
getCompositionData(options: { unicastAddress: number; }) => Promise<any>
```

| Param         | Type                                     |
| ------------- | ---------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; }</code> |

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### addListener(string, ...)

```typescript
addListener(eventName: string, listenerFunc: (event: any) => void) => Promise<PluginListenerHandle>
```

| Param              | Type                                 |
| ------------------ | ------------------------------------ |
| **`eventName`**    | <code>string</code>                  |
| **`listenerFunc`** | <code>(event: any) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### Interfaces


#### Permissions


#### BluetoothState

| Prop          | Type                 |
| ------------- | -------------------- |
| **`enabled`** | <code>boolean</code> |


#### ScanMeshDevices

| Prop                | Type                               |
| ------------------- | ---------------------------------- |
| **`unprovisioned`** | <code>UnprovisionedDevice[]</code> |
| **`provisioned`**   | <code>ProvisionedDevice[]</code>   |
| **`proxy`**         | <code>ProxyDevice[]</code>         |


#### UnprovisionedDevice

| Prop             | Type                |
| ---------------- | ------------------- |
| **`uuid`**       | <code>string</code> |
| **`name`**       | <code>string</code> |
| **`rssi`**       | <code>number</code> |
| **`macAddress`** | <code>string</code> |


#### ProvisionedDevice

| Prop                        | Type                                                                                 |
| --------------------------- | ------------------------------------------------------------------------------------ |
| **`name`**                  | <code>string</code>                                                                  |
| **`provisionedTime`**       | <code>string</code>                                                                  |
| **`unicastAddress`**        | <code>number</code>                                                                  |
| **`security`**              | <code>boolean</code>                                                                 |
| **`deviceKey`**             | <code>string</code>                                                                  |
| **`companyIdentifier`**     | <code>string</code>                                                                  |
| **`productIdentifier`**     | <code>string</code>                                                                  |
| **`productVersion`**        | <code>string</code>                                                                  |
| **`replayProtectionCount`** | <code>string</code>                                                                  |
| **`nodeFeaturesSupported`** | <code>{ relay: boolean; proxy: boolean; friend: boolean; lowPower: boolean; }</code> |
| **`nodeFeatures`**          | <code>{ relay: boolean; proxy: boolean; friend: boolean; lowPower: boolean; }</code> |


#### ProxyDevice

| Prop             | Type                |
| ---------------- | ------------------- |
| **`name`**       | <code>string</code> |
| **`rssi`**       | <code>number</code> |
| **`macAddress`** | <code>string</code> |


#### ProvisioningCapabilities

| Prop                    | Type                  |
| ----------------------- | --------------------- |
| **`numberOfElements`**  | <code>number</code>   |
| **`availableOOBTypes`** | <code>string[]</code> |
| **`algorithms`**        | <code>number</code>   |
| **`publicKeyType`**     | <code>number</code>   |
| **`staticOobTypes`**    | <code>number</code>   |
| **`outputOobSize`**     | <code>number</code>   |
| **`outputOobActions`**  | <code>number</code>   |
| **`inputOobSize`**      | <code>number</code>   |
| **`inputOobActions`**   | <code>number</code>   |


#### ProvisioningStatus

| Prop                       | Type                 |
| -------------------------- | -------------------- |
| **`provisioningComplete`** | <code>boolean</code> |
| **`uuid`**                 | <code>string</code>  |
| **`unicastAddress`**       | <code>number</code>  |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>
