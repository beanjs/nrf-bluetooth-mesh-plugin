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
* [`exportMeshNetwork()`](#exportmeshnetwork)
* [`importMeshNetwork(...)`](#importmeshnetwork)
* [`getMeshNetwork()`](#getmeshnetwork)
* [`createAppKey()`](#createappkey)
* [`removeAppKey(...)`](#removeappkey)
* [`getNode(...)`](#getnode)
* [`scanMeshDevices(...)`](#scanmeshdevices)
* [`getProvisioningCapabilities(...)`](#getprovisioningcapabilities)
* [`provisionDevice(...)`](#provisiondevice)
* [`unprovisionDevice(...)`](#unprovisiondevice)
* [`getCompositionData(...)`](#getcompositiondata)
* [`getDefaultTTL(...)`](#getdefaultttl)
* [`setDefaultTTL(...)`](#setdefaultttl)
* [`getNetworkTransmit(...)`](#getnetworktransmit)
* [`setNetworkTransmit(...)`](#setnetworktransmit)
* [`addAppKey(...)`](#addappkey)
* [`deleteAppKey(...)`](#deleteappkey)
* [`getAppKeys(...)`](#getappkeys)
* [`bindAppKey(...)`](#bindappkey)
* [`unbindAppKey(...)`](#unbindappkey)
* [`getOnOff(...)`](#getonoff)
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


### exportMeshNetwork()

```typescript
exportMeshNetwork() => Promise<MeshNetworkExport>
```

**Returns:** <code>Promise&lt;<a href="#meshnetworkexport">MeshNetworkExport</a>&gt;</code>

--------------------


### importMeshNetwork(...)

```typescript
importMeshNetwork(network: MeshNetworkExport) => Promise<void>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`network`** | <code><a href="#meshnetworkexport">MeshNetworkExport</a></code> |

--------------------


### getMeshNetwork()

```typescript
getMeshNetwork() => Promise<MeshNetwork>
```

**Returns:** <code>Promise&lt;<a href="#meshnetwork">MeshNetwork</a>&gt;</code>

--------------------


### createAppKey()

```typescript
createAppKey() => Promise<MeshAppKey>
```

**Returns:** <code>Promise&lt;<a href="#meshappkey">MeshAppKey</a>&gt;</code>

--------------------


### removeAppKey(...)

```typescript
removeAppKey(options: { index: number; }) => Promise<void>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ index: number; }</code> |

--------------------


### getNode(...)

```typescript
getNode(options: { unicastAddress: number; }) => Promise<MeshNode | undefined>
```

| Param         | Type                                     |
| ------------- | ---------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#meshnode">MeshNode</a>&gt;</code>

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
unprovisionDevice(options: { unicastAddress: number; }) => Promise<NodeResetStatus>
```

| Param         | Type                                     |
| ------------- | ---------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#noderesetstatus">NodeResetStatus</a>&gt;</code>

--------------------


### getCompositionData(...)

```typescript
getCompositionData(options: { unicastAddress: number; }) => Promise<CompositionDataStatus>
```

| Param         | Type                                     |
| ------------- | ---------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#compositiondatastatus">CompositionDataStatus</a>&gt;</code>

--------------------


### getDefaultTTL(...)

```typescript
getDefaultTTL(options: { unicastAddress: number; }) => Promise<DefaultTTLStatus>
```

| Param         | Type                                     |
| ------------- | ---------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#defaultttlstatus">DefaultTTLStatus</a>&gt;</code>

--------------------


### setDefaultTTL(...)

```typescript
setDefaultTTL(options: { unicastAddress: number; ttl: number; }) => Promise<DefaultTTLStatus>
```

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; ttl: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#defaultttlstatus">DefaultTTLStatus</a>&gt;</code>

--------------------


### getNetworkTransmit(...)

```typescript
getNetworkTransmit(options: { unicastAddress: number; }) => Promise<NetworkTransmitStatus>
```

| Param         | Type                                     |
| ------------- | ---------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#networktransmitstatus">NetworkTransmitStatus</a>&gt;</code>

--------------------


### setNetworkTransmit(...)

```typescript
setNetworkTransmit(options: { unicastAddress: number; networkTransmitCount: number; networkTransmitIntervalSteps: number; }) => Promise<NetworkTransmitStatus>
```

| Param         | Type                                                                                                         |
| ------------- | ------------------------------------------------------------------------------------------------------------ |
| **`options`** | <code>{ unicastAddress: number; networkTransmitCount: number; networkTransmitIntervalSteps: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#networktransmitstatus">NetworkTransmitStatus</a>&gt;</code>

--------------------


### addAppKey(...)

```typescript
addAppKey(options: { unicastAddress: number; appKeyIndex: number; }) => Promise<AppKeyStatus>
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; appKeyIndex: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#appkeystatus">AppKeyStatus</a>&gt;</code>

--------------------


### deleteAppKey(...)

```typescript
deleteAppKey(options: { unicastAddress: number; appKeyIndex: number; }) => Promise<AppKeyStatus>
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; appKeyIndex: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#appkeystatus">AppKeyStatus</a>&gt;</code>

--------------------


### getAppKeys(...)

```typescript
getAppKeys(options: { unicastAddress: number; }) => Promise<AppKeyListStatus>
```

| Param         | Type                                     |
| ------------- | ---------------------------------------- |
| **`options`** | <code>{ unicastAddress: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#appkeyliststatus">AppKeyListStatus</a>&gt;</code>

--------------------


### bindAppKey(...)

```typescript
bindAppKey(options: { unicastAddress: number; elementAddress: number; modelId: number; appKeyIndex: number; }) => Promise<ModelAppStatus>
```

| Param         | Type                                                                                                   |
| ------------- | ------------------------------------------------------------------------------------------------------ |
| **`options`** | <code>{ unicastAddress: number; elementAddress: number; modelId: number; appKeyIndex: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#modelappstatus">ModelAppStatus</a>&gt;</code>

--------------------


### unbindAppKey(...)

```typescript
unbindAppKey(options: { unicastAddress: number; elementAddress: number; modelId: number; appKeyIndex: number; }) => Promise<ModelAppStatus>
```

| Param         | Type                                                                                                   |
| ------------- | ------------------------------------------------------------------------------------------------------ |
| **`options`** | <code>{ unicastAddress: number; elementAddress: number; modelId: number; appKeyIndex: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#modelappstatus">ModelAppStatus</a>&gt;</code>

--------------------


### getOnOff(...)

```typescript
getOnOff(options: { elementAddress: number; appKeyIndex: number; }) => Promise<OnOffStatus>
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code>{ elementAddress: number; appKeyIndex: number; }</code> |

**Returns:** <code>Promise&lt;<a href="#onoffstatus">OnOffStatus</a>&gt;</code>

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


#### MeshNetworkExport

| Prop              | Type                |
| ----------------- | ------------------- |
| **`meshNetwork`** | <code>string</code> |


#### MeshNetwork

| Prop                    | Type                                                                                                                     |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| **`name`**              | <code>string</code>                                                                                                      |
| **`lastModified`**      | <code>string</code>                                                                                                      |
| **`provisioners`**      | <code><a href="#array">Array</a>&lt;<a href="#meshprovisioner">MeshProvisioner</a>&gt;</code>                            |
| **`netKeys`**           | <code><a href="#array">Array</a>&lt;<a href="#meshnetkey">MeshNetKey</a>&gt;</code>                                      |
| **`appKeys`**           | <code><a href="#array">Array</a>&lt;<a href="#meshappkey">MeshAppKey</a>&gt;</code>                                      |
| **`nodes`**             | <code><a href="#array">Array</a>&lt;<a href="#meshnode">MeshNode</a>&gt;</code>                                          |
| **`networkExclusions`** | <code><a href="#array">Array</a>&lt;{ ivIndex: number; addresses: <a href="#array">Array</a>&lt;number&gt;; }&gt;</code> |


#### Array

| Prop         | Type                | Description                                                                                            |
| ------------ | ------------------- | ------------------------------------------------------------------------------------------------------ |
| **`length`** | <code>number</code> | Gets or sets the length of the array. This is a number one higher than the highest index in the array. |

| Method             | Signature                                                                                                                     | Description                                                                                                                                                                                                                                 |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **toString**       | () =&gt; string                                                                                                               | Returns a string representation of an array.                                                                                                                                                                                                |
| **toLocaleString** | () =&gt; string                                                                                                               | Returns a string representation of an array. The elements are converted to string using their toLocalString methods.                                                                                                                        |
| **pop**            | () =&gt; T \| undefined                                                                                                       | Removes the last element from an array and returns it. If the array is empty, undefined is returned and the array is not modified.                                                                                                          |
| **push**           | (...items: T[]) =&gt; number                                                                                                  | Appends new elements to the end of an array, and returns the new length of the array.                                                                                                                                                       |
| **concat**         | (...items: <a href="#concatarray">ConcatArray</a>&lt;T&gt;[]) =&gt; T[]                                                       | Combines two or more arrays. This method returns a new array without modifying any existing arrays.                                                                                                                                         |
| **concat**         | (...items: (T \| <a href="#concatarray">ConcatArray</a>&lt;T&gt;)[]) =&gt; T[]                                                | Combines two or more arrays. This method returns a new array without modifying any existing arrays.                                                                                                                                         |
| **join**           | (separator?: string \| undefined) =&gt; string                                                                                | Adds all the elements of an array into a string, separated by the specified separator string.                                                                                                                                               |
| **reverse**        | () =&gt; T[]                                                                                                                  | Reverses the elements in an array in place. This method mutates the array and returns a reference to the same array.                                                                                                                        |
| **shift**          | () =&gt; T \| undefined                                                                                                       | Removes the first element from an array and returns it. If the array is empty, undefined is returned and the array is not modified.                                                                                                         |
| **slice**          | (start?: number \| undefined, end?: number \| undefined) =&gt; T[]                                                            | Returns a copy of a section of an array. For both start and end, a negative index can be used to indicate an offset from the end of the array. For example, -2 refers to the second to last element of the array.                           |
| **sort**           | (compareFn?: ((a: T, b: T) =&gt; number) \| undefined) =&gt; this                                                             | Sorts an array in place. This method mutates the array and returns a reference to the same array.                                                                                                                                           |
| **splice**         | (start: number, deleteCount?: number \| undefined) =&gt; T[]                                                                  | Removes elements from an array and, if necessary, inserts new elements in their place, returning the deleted elements.                                                                                                                      |
| **splice**         | (start: number, deleteCount: number, ...items: T[]) =&gt; T[]                                                                 | Removes elements from an array and, if necessary, inserts new elements in their place, returning the deleted elements.                                                                                                                      |
| **unshift**        | (...items: T[]) =&gt; number                                                                                                  | Inserts new elements at the start of an array, and returns the new length of the array.                                                                                                                                                     |
| **indexOf**        | (searchElement: T, fromIndex?: number \| undefined) =&gt; number                                                              | Returns the index of the first occurrence of a value in an array, or -1 if it is not present.                                                                                                                                               |
| **lastIndexOf**    | (searchElement: T, fromIndex?: number \| undefined) =&gt; number                                                              | Returns the index of the last occurrence of a specified value in an array, or -1 if it is not present.                                                                                                                                      |
| **every**          | &lt;S extends T&gt;(predicate: (value: T, index: number, array: T[]) =&gt; value is S, thisArg?: any) =&gt; this is S[]       | Determines whether all the members of an array satisfy the specified test.                                                                                                                                                                  |
| **every**          | (predicate: (value: T, index: number, array: T[]) =&gt; unknown, thisArg?: any) =&gt; boolean                                 | Determines whether all the members of an array satisfy the specified test.                                                                                                                                                                  |
| **some**           | (predicate: (value: T, index: number, array: T[]) =&gt; unknown, thisArg?: any) =&gt; boolean                                 | Determines whether the specified callback function returns true for any element of an array.                                                                                                                                                |
| **forEach**        | (callbackfn: (value: T, index: number, array: T[]) =&gt; void, thisArg?: any) =&gt; void                                      | Performs the specified action for each element in an array.                                                                                                                                                                                 |
| **map**            | &lt;U&gt;(callbackfn: (value: T, index: number, array: T[]) =&gt; U, thisArg?: any) =&gt; U[]                                 | Calls a defined callback function on each element of an array, and returns an array that contains the results.                                                                                                                              |
| **filter**         | &lt;S extends T&gt;(predicate: (value: T, index: number, array: T[]) =&gt; value is S, thisArg?: any) =&gt; S[]               | Returns the elements of an array that meet the condition specified in a callback function.                                                                                                                                                  |
| **filter**         | (predicate: (value: T, index: number, array: T[]) =&gt; unknown, thisArg?: any) =&gt; T[]                                     | Returns the elements of an array that meet the condition specified in a callback function.                                                                                                                                                  |
| **reduce**         | (callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) =&gt; T) =&gt; T                           | Calls the specified callback function for all the elements in an array. The return value of the callback function is the accumulated result, and is provided as an argument in the next call to the callback function.                      |
| **reduce**         | (callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) =&gt; T, initialValue: T) =&gt; T          |                                                                                                                                                                                                                                             |
| **reduce**         | &lt;U&gt;(callbackfn: (previousValue: U, currentValue: T, currentIndex: number, array: T[]) =&gt; U, initialValue: U) =&gt; U | Calls the specified callback function for all the elements in an array. The return value of the callback function is the accumulated result, and is provided as an argument in the next call to the callback function.                      |
| **reduceRight**    | (callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) =&gt; T) =&gt; T                           | Calls the specified callback function for all the elements in an array, in descending order. The return value of the callback function is the accumulated result, and is provided as an argument in the next call to the callback function. |
| **reduceRight**    | (callbackfn: (previousValue: T, currentValue: T, currentIndex: number, array: T[]) =&gt; T, initialValue: T) =&gt; T          |                                                                                                                                                                                                                                             |
| **reduceRight**    | &lt;U&gt;(callbackfn: (previousValue: U, currentValue: T, currentIndex: number, array: T[]) =&gt; U, initialValue: U) =&gt; U | Calls the specified callback function for all the elements in an array, in descending order. The return value of the callback function is the accumulated result, and is provided as an argument in the next call to the callback function. |


#### ConcatArray

| Prop         | Type                |
| ------------ | ------------------- |
| **`length`** | <code>number</code> |

| Method    | Signature                                                          |
| --------- | ------------------------------------------------------------------ |
| **join**  | (separator?: string \| undefined) =&gt; string                     |
| **slice** | (start?: number \| undefined, end?: number \| undefined) =&gt; T[] |


#### MeshProvisioner

| Prop                 | Type                                                          |
| -------------------- | ------------------------------------------------------------- |
| **`name`**           | <code>string</code>                                           |
| **`ttl`**            | <code>number</code>                                           |
| **`unicastAddress`** | <code>number</code>                                           |
| **`unicast`**        | <code>[{ lowerAddress: number; highAddress: number; }]</code> |
| **`group`**          | <code>[{ lowerAddress: number; highAddress: number; }]</code> |
| **`scene`**          | <code>[{ firstScene: number; lastScene: number; }]</code>     |


#### MeshNetKey

| Prop               | Type                                |
| ------------------ | ----------------------------------- |
| **`name`**         | <code>string</code>                 |
| **`key`**          | <code>string</code>                 |
| **`oldKey`**       | <code>string</code>                 |
| **`index`**        | <code>number</code>                 |
| **`phase`**        | <code>number</code>                 |
| **`security`**     | <code>'secure' \| 'insecure'</code> |
| **`lastModified`** | <code>string</code>                 |


#### MeshAppKey

| Prop                   | Type                |
| ---------------------- | ------------------- |
| **`name`**             | <code>string</code> |
| **`index`**            | <code>number</code> |
| **`key`**              | <code>string</code> |
| **`oldKey`**           | <code>string</code> |
| **`boundNetKeyIndex`** | <code>number</code> |


#### MeshNode

| Prop                  | Type                                                                                                                                                                                                                                                                            |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`name`**            | <code>string</code>                                                                                                                                                                                                                                                             |
| **`deviceKey`**       | <code>string</code>                                                                                                                                                                                                                                                             |
| **`unicastAddress`**  | <code>number</code>                                                                                                                                                                                                                                                             |
| **`security`**        | <code>string</code>                                                                                                                                                                                                                                                             |
| **`ttl`**             | <code>number</code>                                                                                                                                                                                                                                                             |
| **`excluded`**        | <code>boolean</code>                                                                                                                                                                                                                                                            |
| **`features`**        | <code>{ friend: number; lowPower: number; proxy: number; relay: number; }</code>                                                                                                                                                                                                |
| **`netKeys`**         | <code><a href="#array">Array</a>&lt;{ index: number; updated: boolean; }&gt;</code>                                                                                                                                                                                             |
| **`appKeys`**         | <code><a href="#array">Array</a>&lt;{ index: number; updated: boolean; }&gt;</code>                                                                                                                                                                                             |
| **`elements`**        | <code><a href="#array">Array</a>&lt;{ name: string; elementAddress: number; location: number; models: <a href="#array">Array</a>&lt;{ modelId: number; bind: <a href="#array">Array</a>&lt;number&gt;; subscribe: <a href="#array">Array</a>&lt;number&gt;; }&gt;; }&gt;</code> |
| **`networkTransmit`** | <code>{ count: number; interval: number; steps: number; }</code>                                                                                                                                                                                                                |
| **`cid`**             | <code>string</code>                                                                                                                                                                                                                                                             |
| **`pid`**             | <code>string</code>                                                                                                                                                                                                                                                             |
| **`vid`**             | <code>string</code>                                                                                                                                                                                                                                                             |
| **`crpl`**            | <code>string</code>                                                                                                                                                                                                                                                             |


#### ScanMeshDevices

| Prop                | Type                                                                                                  |
| ------------------- | ----------------------------------------------------------------------------------------------------- |
| **`unprovisioned`** | <code><a href="#array">Array</a>&lt;<a href="#unprovisioneddevice">UnprovisionedDevice</a>&gt;</code> |
| **`provisioned`**   | <code><a href="#array">Array</a>&lt;<a href="#provisioneddevice">ProvisionedDevice</a>&gt;</code>     |
| **`proxy`**         | <code><a href="#array">Array</a>&lt;<a href="#proxydevice">ProxyDevice</a>&gt;</code>                 |


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

| Prop                    | Type                                                  |
| ----------------------- | ----------------------------------------------------- |
| **`numberOfElements`**  | <code>number</code>                                   |
| **`availableOOBTypes`** | <code><a href="#array">Array</a>&lt;string&gt;</code> |
| **`algorithms`**        | <code>number</code>                                   |
| **`publicKeyType`**     | <code>number</code>                                   |
| **`staticOobTypes`**    | <code>number</code>                                   |
| **`outputOobSize`**     | <code>number</code>                                   |
| **`outputOobActions`**  | <code>number</code>                                   |
| **`inputOobSize`**      | <code>number</code>                                   |
| **`inputOobActions`**   | <code>number</code>                                   |


#### ProvisioningStatus

| Prop                       | Type                 |
| -------------------------- | -------------------- |
| **`provisioningComplete`** | <code>boolean</code> |
| **`uuid`**                 | <code>string</code>  |
| **`unicastAddress`**       | <code>number</code>  |


#### NodeResetStatus

| Prop       | Type                                                 |
| ---------- | ---------------------------------------------------- |
| **`data`** | <code>{ status: number; statusName: string; }</code> |


#### CompositionDataStatus

| Prop       | Type                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`data`** | <code>{ status: number; statusName: string; companyIdentifier: string; productIdentifier: string; productVersion: string; nodeFeaturesSupported: { relay: boolean; proxy: boolean; friend: boolean; lowPower: boolean; }; elements: { name: string; elementAddress: number; sigModelCount: number; vendorModelCount: number; location: number; models: { modelId: number; modelName: string; boundAppKeyIndexes: number[]; }[]; }[]; }</code> |


#### DefaultTTLStatus

| Prop       | Type                                                              |
| ---------- | ----------------------------------------------------------------- |
| **`data`** | <code>{ status: number; statusName: string; ttl: number; }</code> |


#### NetworkTransmitStatus

| Prop       | Type                                                                                                                     |
| ---------- | ------------------------------------------------------------------------------------------------------------------------ |
| **`data`** | <code>{ status: number; statusName: string; networkTransmitCount: number; networkTransmitIntervalSteps: number; }</code> |


#### AppKeyStatus

| Prop       | Type                                                                                           |
| ---------- | ---------------------------------------------------------------------------------------------- |
| **`data`** | <code>{ status: number; statusName: string; netKeyIndex: number; appKeyIndex: number; }</code> |


#### AppKeyListStatus

| Prop       | Type                                                                                               |
| ---------- | -------------------------------------------------------------------------------------------------- |
| **`data`** | <code>{ status: number; statusName: string; netKeyIndex: number; appKeyIndexes: number[]; }</code> |


#### ModelAppStatus

| Prop       | Type                                                                                                               |
| ---------- | ------------------------------------------------------------------------------------------------------------------ |
| **`data`** | <code>{ status: number; statusName: string; elementAddress: number; modelId: number; appKeyIndex: number; }</code> |


#### OnOffStatus

| Prop       | Type                             |
| ---------- | -------------------------------- |
| **`data`** | <code>{ onOff: boolean; }</code> |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>
