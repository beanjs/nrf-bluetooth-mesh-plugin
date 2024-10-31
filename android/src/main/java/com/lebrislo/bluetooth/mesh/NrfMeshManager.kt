package com.lebrislo.bluetooth.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.lebrislo.bluetooth.mesh.ble.BleCallbacksManager
import com.lebrislo.bluetooth.mesh.ble.BleMeshManager
import com.lebrislo.bluetooth.mesh.models.BleMeshDevice
import com.lebrislo.bluetooth.mesh.models.ExtendedBluetoothDevice
import com.lebrislo.bluetooth.mesh.plugin.PluginCallManager
import com.lebrislo.bluetooth.mesh.scanner.ScannerRepository
import com.lebrislo.bluetooth.mesh.utils.Utils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ble.DisconnectRequest
import no.nordicsemi.android.mesh.ApplicationKey
import no.nordicsemi.android.mesh.Group
import no.nordicsemi.android.mesh.MeshManagerApi
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty
import no.nordicsemi.android.mesh.transport.ConfigAppKeyAdd
import no.nordicsemi.android.mesh.transport.ConfigAppKeyDelete
import no.nordicsemi.android.mesh.transport.ConfigAppKeyGet
import no.nordicsemi.android.mesh.transport.ConfigAppKeyList
import no.nordicsemi.android.mesh.transport.ConfigAppKeyStatus
import no.nordicsemi.android.mesh.transport.ConfigAppKeyUpdate
import no.nordicsemi.android.mesh.transport.ConfigCompositionDataGet
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlGet
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlSet
import no.nordicsemi.android.mesh.transport.ConfigModelAppBind
import no.nordicsemi.android.mesh.transport.ConfigModelAppUnbind
import no.nordicsemi.android.mesh.transport.ConfigModelPublicationGet
import no.nordicsemi.android.mesh.transport.ConfigModelPublicationSet
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionAdd
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionDelete
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionDeleteAll
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitGet
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitSet
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitStatus
import no.nordicsemi.android.mesh.transport.ConfigNodeReset
import no.nordicsemi.android.mesh.transport.GenericLevelSet
import no.nordicsemi.android.mesh.transport.GenericLevelSetUnacknowledged
import no.nordicsemi.android.mesh.transport.GenericOnOffGet
import no.nordicsemi.android.mesh.transport.GenericOnOffSet
import no.nordicsemi.android.mesh.transport.GenericOnOffSetUnacknowledged
import no.nordicsemi.android.mesh.transport.LightCtlSet
import no.nordicsemi.android.mesh.transport.LightCtlSetUnacknowledged
import no.nordicsemi.android.mesh.transport.LightHslGet
import no.nordicsemi.android.mesh.transport.LightHslSet
import no.nordicsemi.android.mesh.transport.LightHslSetUnacknowledged
import no.nordicsemi.android.mesh.transport.MeshMessage
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode
import no.nordicsemi.android.mesh.transport.SensorCadenceGet
import no.nordicsemi.android.mesh.transport.SensorCadenceSet
import no.nordicsemi.android.mesh.transport.SensorColumnGet
import no.nordicsemi.android.mesh.transport.SensorDescriptorGet
import no.nordicsemi.android.mesh.transport.SensorGet
import no.nordicsemi.android.mesh.transport.SensorSeriesGet
import no.nordicsemi.android.mesh.transport.SensorSettingGet
import no.nordicsemi.android.mesh.transport.SensorSettingSet
import no.nordicsemi.android.mesh.transport.SensorSettingSetUnacknowledged
import no.nordicsemi.android.mesh.transport.SensorSettingsGet
import no.nordicsemi.android.mesh.transport.VendorModelMessageAcked
import no.nordicsemi.android.mesh.transport.VendorModelMessageUnacked
import no.nordicsemi.android.mesh.utils.CompositionDataParser
import no.nordicsemi.android.mesh.utils.MeshParserUtils
import java.text.DateFormat
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random


class NrfMeshManager(private val context: Context) {
    private val tag: String = NrfMeshManager::class.java.simpleName

    private val meshCallbacksManager: MeshCallbacksManager
    private val meshProvisioningCallbacksManager: MeshProvisioningCallbacksManager
    private val meshStatusCallbacksManager: MeshStatusCallbacksManager
    private val bleCallbacksManager: BleCallbacksManager
    private val scannerRepository: ScannerRepository

    private var bleMeshManager: BleMeshManager = BleMeshManager(context)
    var meshManagerApi: MeshManagerApi = MeshManagerApi(context)

    init {
        meshCallbacksManager = MeshCallbacksManager(bleMeshManager)
        meshProvisioningCallbacksManager = MeshProvisioningCallbacksManager(this)
        meshStatusCallbacksManager = MeshStatusCallbacksManager(this)
        bleCallbacksManager = BleCallbacksManager(meshManagerApi)
        scannerRepository = ScannerRepository(context, meshManagerApi)

        meshManagerApi.setMeshManagerCallbacks(meshCallbacksManager)
        meshManagerApi.setProvisioningStatusCallbacks(meshProvisioningCallbacksManager)
        meshManagerApi.setMeshStatusCallbacks(meshStatusCallbacksManager)
        bleMeshManager.setGattCallbacks(bleCallbacksManager)
    }

    @SuppressLint("RestrictedApi")
    private fun formatNode(node: ProvisionedMeshNode): JSObject {
        return JSObject().apply {
            put("name", node.nodeName)
            put("deviceKey", MeshParserUtils.bytesToHex(node.deviceKey, false))
            put("unicastAddress", node.unicastAddress)
            put("security", if (node.security == 1) "secure" else "insecure")
            put("ttl", node.ttl)
            put("excluded", node.isExcluded)
            put("netKeys", JSArray().apply {
                node.addedNetKeys.forEach {
                    put(JSObject().apply {
                        put("index", it.index)
                        put("updated", it.isUpdated)
                    })
                }
            })
            put("appKeys", JSArray().apply {
                node.addedAppKeys.forEach {
                    put(JSObject().apply {
                        put("index", it.index)
                        put("updated", it.isUpdated)
                    })
                }
            })
            put("elements", JSArray().apply {
                node.elements.values.forEach {
                    put(JSObject().apply {
                        put("name", it.name)
                        put("elementAddress", it.elementAddress)
                        put("location", it.locationDescriptor)
                        put("models", JSArray().apply {
                            it.meshModels.values.forEach {
                                put(JSObject().apply {
                                    put("modelId", it.modelId)
                                    put("bind", JSArray().apply {
                                        it.boundAppKeyIndexes.forEach {
                                            put(it)
                                        }
                                    })
                                    put("subscribe", JSArray().apply {
                                        it.subscribedAddresses.forEach {
                                            put(it)
                                        }
                                    })
                                    if (it.publicationSettings != null) {
                                        put("publish", it.publicationSettings.publishAddress)
                                    }
                                })
                            }
                        })
                    })
                }
            })

            if (node.nodeFeatures != null) {
                put("features", JSObject().apply {
                    put("friend", node.nodeFeatures.friend)
                    put("lowPower", node.nodeFeatures.lowPower)
                    put("proxy", node.nodeFeatures.proxy)
                    put("relay", node.nodeFeatures.relay)
                })
            }
            if (node.networkTransmitSettings != null) {
                put("networkTransmit", JSObject().apply {
                    put("count", node.networkTransmitSettings.networkTransmitCount)
                    put("interval", node.networkTransmitSettings.networkTransmissionInterval)
                    put("steps", node.networkTransmitSettings.networkIntervalSteps)
                })
            }
            if (node.companyIdentifier != null) {
                put("cid", CompositionDataParser.formatCompanyIdentifier(node.companyIdentifier, false))
            }
            if (node.productIdentifier != null) {
                put("pid", CompositionDataParser.formatProductIdentifier(node.productIdentifier, false))
            }
            if (node.versionIdentifier != null) {
                put("vid", CompositionDataParser.formatVersionIdentifier(node.versionIdentifier, false))
            }
            if (node.crpl != null) {
                put("crpl", CompositionDataParser.formatReplayProtectionCount(node.crpl, false))
            }
        }
    }

    private fun formatGroup(group: Group): JSObject {
        val network = meshManagerApi.meshNetwork!!
        val models = network.getModels(group)

        return JSObject().apply {
            put("name", group.name)
            put("address", group.address)
            put("devices", models.size)
        }
    }

    fun connectBle(bluetoothDevice: BluetoothDevice): Boolean {
        scannerRepository.stopScanDevices()
        bleMeshManager.connect(bluetoothDevice).retry(3, 200).await()
        return bleMeshManager.isConnected
    }

    fun disconnectBle(): DisconnectRequest {
        scannerRepository.startScanDevices()
        return bleMeshManager.disconnect()
    }

    fun isBleConnected(): Boolean {
        return bleMeshManager.isConnected
    }

    fun connectedDevice(): BluetoothDevice? {
        return bleMeshManager.bluetoothDevice
    }

    fun startScan() {
        scannerRepository.startScanDevices()
    }

    fun stopScan() {
        scannerRepository.stopScanDevices()
    }

    fun getNodes(): List<ProvisionedMeshNode> {
        return meshManagerApi.meshNetwork?.nodes ?: listOf()
    }

    fun assertMeshNetwork(call: PluginCall): Boolean {
        if (meshManagerApi.meshNetwork == null) {
            call.reject("meshNetwork not initialized.")
            return false
        }
        return true
    }

    fun initMeshNetwork() {
        meshManagerApi.loadMeshNetwork()
    }

    fun exportMeshNetwork(): String? {
        return meshManagerApi.exportMeshNetwork()
    }

    fun importMeshNetwork(json: String) {
        meshManagerApi.importMeshNetworkJson(json)
    }

    @SuppressLint("RestrictedApi")
    fun getMeshNetwork(): JSObject {
        val network = meshManagerApi.meshNetwork!!

        return JSObject().apply {
            put("name", network.meshName)
            put("lastModified", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(network.timestamp))
            put("netKeys", JSArray().apply {
                network.netKeys.forEach {
                    put(JSObject().apply {
                        put("name", it.name)
                        put("index", it.keyIndex)
                        put("phase", it.phaseDescription)
                        put("key", MeshParserUtils.bytesToHex(it.key, false))
                        if (it.oldKey != null) {
                            put("oldKey", MeshParserUtils.bytesToHex(it.oldKey, false))
                        }
                        put("security", if (it.isMinSecurity) "secure" else "insecure")
                        put("lastModified", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(it.timestamp))
                    })
                }
            })
            put("appKeys", JSArray().apply {
                network.appKeys.forEach {
                    put(JSObject().apply {
                        put("name", it.name)
                        put("index", it.keyIndex)
                        put("key", MeshParserUtils.bytesToHex(it.key, false))
                        if (it.oldKey != null) {
                            put("oldKey", MeshParserUtils.bytesToHex(it.oldKey, false))
                        }
                        put("boundNetKeyIndex", it.boundNetKeyIndex)
                    })
                }
            })
            put("provisioners", JSArray().apply {
                network.provisioners.forEach {
                    put(JSObject().apply {
                        put("name", it.provisionerName)
                        it.allocatedUnicastRanges.forEach {
                            put("unicast", JSArray().apply {
                                put(JSObject().apply {
                                    put("lowerAddress", it.lowAddress)
                                    put("highAddress", it.highAddress)
                                })
                            })
                        }

                        it.allocatedGroupRanges.forEach {
                            put("group", JSArray().apply {
                                put(JSObject().apply {
                                    put("lowerAddress", it.lowAddress)
                                    put("highAddress", it.highAddress)
                                })
                            })
                        }

                        it.allocatedSceneRanges.forEach {
                            put("scene", JSArray().apply {
                                put(JSObject().apply {
                                    put("firstScene", it.firstScene)
                                    put("lastScene", it.lastScene)
                                })
                            })
                        }
                    })
                }
            })
            put("nodes", JSArray().apply {
                network.nodes.forEach {
                    put(formatNode(it))
                }
            })
            put("groups", JSArray().apply {
                network.groups.forEach {
                    put(formatGroup(it))
                }
            })
            put("networkExclusions", JSArray().apply {
                network.networkExclusions.forEach { (ivIndex, address) ->
                    put(JSObject().apply {
                        put("ivIndex", ivIndex)
                        put("addresses", JSArray().apply {
                            address.forEach {
                                put(it)
                            }
                        })
                    })
                }
            })
        }
    }

    fun getNode(unicastAddress: Int): JSObject? {
        val network = meshManagerApi.meshNetwork!!
        val node = network.getNode(unicastAddress) ?: return null
        return formatNode(node)
    }

    fun createAppKey(): JSObject {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.createAppKey()
        network.addAppKey(appkey)

        return JSObject().apply {
            put("name", appkey.name)
            put("index", appkey.keyIndex)
            put("key", MeshParserUtils.bytesToHex(appkey.key, false))
            if (appkey.oldKey != null) {
                put("oldKey", MeshParserUtils.bytesToHex(appkey.oldKey, false))
            }
            put("boundNetKeyIndex", appkey.boundNetKeyIndex)
        }
    }

    fun removeAppKey(appKeyIndex: Int) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex) ?: return

        network.removeAppKey(appkey)
    }

    fun createGroup(name: String): JSObject {
        val network = meshManagerApi.meshNetwork!!
        val provisioner = network.selectedProvisioner

        val group = network.createGroup(provisioner, name)
        return JSObject().apply {
            put("name", group.name)
            put("address", group.address)
            put("devices", 0)
        }
    }

    fun removeGroup(groupAddress: Int) {
        val network = meshManagerApi.meshNetwork!!
        val group = network.getGroup(groupAddress) ?: return
        val models = network.getModels(group)
        if (models.size != 0) return

        network.removeGroup(group)
    }

    fun getGroup(groupAddress: Int): JSObject? {
        val network = meshManagerApi.meshNetwork!!
        val group = network.getGroup(groupAddress) ?: return null

        return formatGroup(group)
    }

    @SuppressLint("MissingPermission")
    suspend fun searchProxyMesh(): BluetoothDevice? {
        if (bleMeshManager.isConnected) {
            Log.d(tag, "searchProxyMesh : Connected to a bluetooth device")

            synchronized(scannerRepository.devices) {
                val isMeshProxy = scannerRepository.devices.any() { device ->
                    device.provisioned && device.scanResult?.device?.address == bleMeshManager.bluetoothDevice?.address
                }

                Log.d(tag, "searchProxyMesh : Is mesh proxy: $isMeshProxy")

                if (isMeshProxy) {
                    Log.i(tag, "searchProxyMesh : Connected to a mesh proxy ${bleMeshManager.bluetoothDevice?.address}")
                    return bleMeshManager.bluetoothDevice
                }
            }

            withContext(Dispatchers.IO) {
                disconnectBle().await()
            }
        }

        synchronized(scannerRepository.devices) {
            Log.d(tag, "searchProxyMesh : Provisioned devices: ${scannerRepository.devices.size}")

            if (scannerRepository.devices.isNotEmpty()) {
                val devices = scannerRepository.devices.filter {
                    it.provisioned
                }.toMutableList()

                devices.sortBy { device -> device.scanResult?.rssi }

                val device = devices.first().device
                Log.i(tag, "searchProxyMesh : Found a mesh proxy ${device!!.address}")
                return device
            }
        }

        return null
    }

    suspend fun searchUnprovisionedBluetoothDevice(uuid: String): BluetoothDevice? {
        if (bleMeshManager.isConnected) {
            val macAddress = bleMeshManager.bluetoothDevice!!.address

            synchronized(scannerRepository.devices) {
                if (scannerRepository.devices.any { device -> !device.provisioned && device.scanResult?.device?.address == macAddress }) {
                    return bleMeshManager.bluetoothDevice
                }
            }

            withContext(Dispatchers.IO) {
                disconnectBle().await()
            }
        }

        synchronized(scannerRepository.devices) {
            return scannerRepository.devices.firstOrNull { device ->
                if (device.provisioned) {
                    return@firstOrNull false
                }

                device.scanResult?.let {
                    val serviceData = Utils.getServiceData(it, MeshManagerApi.MESH_PROVISIONING_UUID)
                    val deviceUuid = meshManagerApi.getDeviceUuid(serviceData!!)
                    deviceUuid.toString() == uuid
                } ?: false
            }?.device
        }
    }

    suspend fun scanMeshDevices(scanDurationMs: Int = 5000): List<ExtendedBluetoothDevice> {
        synchronized(scannerRepository.devices) {
            scannerRepository.devices.clear()
        }

        scannerRepository.stopScanDevices()
        scannerRepository.startScanDevices()

        delay(scanDurationMs.toLong())

        val devices: MutableList<ExtendedBluetoothDevice> = mutableListOf()
        synchronized(scannerRepository.devices) {
            scannerRepository.devices.forEach {
                devices.add(it)
            }
        }

        return devices
    }

    fun identify(uuid: UUID) {
        meshManagerApi.identifyNode(uuid)
    }

    fun unprovisionedMeshNode(uuid: UUID): UnprovisionedMeshNode? {
        return meshProvisioningCallbacksManager.unprovisionedMeshNodes.firstOrNull { node ->
            node.deviceUuid == uuid
        }
    }

    fun provisionDevice(node: UnprovisionedMeshNode) {
        val provisioner = meshManagerApi.meshNetwork?.selectedProvisioner
        val unicastAddress = meshManagerApi.meshNetwork?.nextAvailableUnicastAddress(
                node.numberOfElements, provisioner!!
        )

        if (bleMeshManager.isConnected) {
            node.nodeName = bleMeshManager.bluetoothDevice!!.name
        }

        meshManagerApi.meshNetwork?.assignUnicastAddress(unicastAddress!!)
        meshManagerApi.startProvisioning(node)
    }

    fun unprovisionDevice(unicastAddress: Int) {
        val configNodeReset = ConfigNodeReset()
        meshManagerApi.createMeshPdu(unicastAddress, configNodeReset)
    }

    fun getCompositionData(unicastAddress: Int) {
        val configCompositionDataGet = ConfigCompositionDataGet()
        meshManagerApi.createMeshPdu(unicastAddress, configCompositionDataGet)
    }

    fun getDefaultTTL(unicastAddress: Int) {
        val configDefaultTtlGet = ConfigDefaultTtlGet()
        meshManagerApi.createMeshPdu(unicastAddress, configDefaultTtlGet)
    }

    fun setDefaultTTL(unicastAddress: Int, ttl: Int) {
        val configDefaultTtlSet = ConfigDefaultTtlSet(ttl)
        meshManagerApi.createMeshPdu(unicastAddress, configDefaultTtlSet)
    }

    fun getNetworkTransmit(unicastAddress: Int) {
        val configNetworkTransmitGet = ConfigNetworkTransmitGet()
        meshManagerApi.createMeshPdu(unicastAddress, configNetworkTransmitGet)
    }

    fun setNetworkTransmit(unicastAddress: Int, networkTransmitCount: Int, networkTransmitIntervalSteps: Int) {
        val configNetworkTransmitSet = ConfigNetworkTransmitSet(networkTransmitCount, networkTransmitIntervalSteps)
        meshManagerApi.createMeshPdu(unicastAddress, configNetworkTransmitSet)
    }

    fun addAppKey(unicastAddress: Int, appKeyIndex: Int) {
        val network = meshManagerApi.meshNetwork!!
        val netkey = network.primaryNetworkKey
        val appkey = network.getAppKey(appKeyIndex)

        val configAppKeyAdd = ConfigAppKeyAdd(netkey, appkey)
        meshManagerApi.createMeshPdu(unicastAddress, configAppKeyAdd)
    }

    fun delAppKey(unicastAddress: Int, appKeyIndex: Int) {
        val network = meshManagerApi.meshNetwork!!
        val netkey = network.primaryNetworkKey
        val appkey = network.getAppKey(appKeyIndex)

        val configAppKeyDelete = ConfigAppKeyDelete(netkey, appkey)
        meshManagerApi.createMeshPdu(unicastAddress, configAppKeyDelete)
    }

    fun getAppKeys(unicastAddress: Int) {
        val network = meshManagerApi.meshNetwork!!
        val netkey = network.primaryNetworkKey

        val configAppKeyGet = ConfigAppKeyGet(netkey)
        meshManagerApi.createMeshPdu(unicastAddress, configAppKeyGet)
    }

    fun bindAppKey(unicastAddress: Int, elementAddress: Int, modelId: Int, appKeyIndex: Int) {
        val configModelAppBind = ConfigModelAppBind(elementAddress, modelId, appKeyIndex)
        meshManagerApi.createMeshPdu(unicastAddress, configModelAppBind)
    }

    fun unbindAppKey(unicastAddress: Int, elementAddress: Int, modelId: Int, appKeyIndex: Int) {
        val configModelAppUnbind = ConfigModelAppUnbind(elementAddress, modelId, appKeyIndex)
        meshManagerApi.createMeshPdu(unicastAddress, configModelAppUnbind)
    }

    fun subscribe(unicastAddress: Int, elementAddress: Int, subscriptionAddress: Int, modelId: Int) {
        val configModelSubscriptionAdd = ConfigModelSubscriptionAdd(elementAddress, subscriptionAddress, modelId)
        meshManagerApi.createMeshPdu(unicastAddress, configModelSubscriptionAdd)
    }

    fun unsubscribe(unicastAddress: Int, elementAddress: Int, subscriptionAddress: Int, modelId: Int) {
        val configModelSubscriptionDelete = ConfigModelSubscriptionDelete(elementAddress, subscriptionAddress, modelId)
        meshManagerApi.createMeshPdu(unicastAddress, configModelSubscriptionDelete)
    }

    fun unsubscribeAll(unicastAddress: Int, elementAddress: Int, subscriptionAddress: Int) {
        val configModelSubscriptionDeleteAll = ConfigModelSubscriptionDeleteAll(elementAddress, subscriptionAddress)
        meshManagerApi.createMeshPdu(unicastAddress, configModelSubscriptionDeleteAll)
    }

    fun setPublish(unicastAddress: Int,
                   elementAddress: Int,
                   publishAddress: Int,
                   appKeyIndex: Int,
                   credentialFlag: Boolean,
                   publishTtl: Int,
                   publicationSteps: Int,
                   publicationResolution: Int,
                   retransmitCount: Int,
                   retransmitIntervalSteps: Int,
                   modelId: Int) {
        val configModelPublicationSet = ConfigModelPublicationSet(elementAddress, publishAddress, appKeyIndex, credentialFlag, publishTtl, publicationSteps, publicationResolution, retransmitCount, retransmitIntervalSteps, modelId)
        meshManagerApi.createMeshPdu(unicastAddress, configModelPublicationSet)
    }

    fun delPublish(unicastAddress: Int,
                   elementAddress: Int,
                   modelId: Int) {
        val configModelPublicationSet = ConfigModelPublicationSet(elementAddress, modelId)
        meshManagerApi.createMeshPdu(unicastAddress, configModelPublicationSet)
    }

    fun getOnOff(elementAddress: Int, appKeyIndex: Int) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configGenericOnOffGet = GenericOnOffGet(appkey)
        meshManagerApi.createMeshPdu(elementAddress, configGenericOnOffGet)
    }

    fun setOnOffAck(elementAddress: Int, appKeyIndex: Int, onOff: Boolean, transitionSteps: Int?, transitionResolution: Int?, delay: Int?) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configGenericOnOffSet = GenericOnOffSet(appkey, onOff, Random.nextInt(), transitionSteps, transitionResolution, delay)
        meshManagerApi.createMeshPdu(elementAddress, configGenericOnOffSet)
    }

    fun setOnOff(elementAddress: Int, appKeyIndex: Int, onOff: Boolean, transitionSteps: Int?, transitionResolution: Int?, delay: Int?): JSObject {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configGenericOnOffSet = GenericOnOffSetUnacknowledged(appkey, onOff, Random.nextInt(), transitionSteps, transitionResolution, delay)
        meshManagerApi.createMeshPdu(elementAddress, configGenericOnOffSet)

        return JSObject().apply {
            put("src", configGenericOnOffSet.src)
            put("dst", configGenericOnOffSet.dst)
            put("opcode", configGenericOnOffSet.opCode)
            put("data", JSObject().apply {
                put("onOff", onOff)
            })
        }
    }

    fun getSensor(elementAddress: Int, appKeyIndex: Int, propertyId: Int?) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configSensorGet = SensorGet(appkey, if (propertyId == null) null else DeviceProperty.from(propertyId.toShort()))
        meshManagerApi.createMeshPdu(elementAddress, configSensorGet)
    }

    fun getSensorDescriptor(elementAddress: Int, appKeyIndex: Int, propertyId: Int?) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configSensorDescriptorGet = SensorDescriptorGet(appkey, if (propertyId == null) null else DeviceProperty.from(propertyId.toShort()))
        meshManagerApi.createMeshPdu(elementAddress, configSensorDescriptorGet)
    }

    fun getSensorColumn(elementAddress: Int, appKeyIndex: Int, propertyId: Int, rawValueX: ByteArray) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configSensorColumnGet = SensorColumnGet(appkey, DeviceProperty.from(propertyId.toShort()), rawValueX)
        meshManagerApi.createMeshPdu(elementAddress, configSensorColumnGet)
    }

    fun getSensorSeries(elementAddress: Int, appKeyIndex: Int, propertyId: Int, rawValueX1: ByteArray?, rawValueX2: ByteArray?) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        if (rawValueX1 == null) {
            val configSensorSeriesGet = SensorSeriesGet(appkey, DeviceProperty.from(propertyId.toShort()))
            meshManagerApi.createMeshPdu(elementAddress, configSensorSeriesGet)
        } else {
            val configSensorSeriesGet = SensorSeriesGet(appkey, DeviceProperty.from(propertyId.toShort()), rawValueX1, rawValueX2!!)
            meshManagerApi.createMeshPdu(elementAddress, configSensorSeriesGet)
        }
    }

    fun getSensorCadence(elementAddress: Int, appKeyIndex: Int, propertyId: Int) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configSensorCadenceGet = SensorCadenceGet(appkey, DeviceProperty.from(propertyId.toShort()))
        meshManagerApi.createMeshPdu(elementAddress, configSensorCadenceGet)
    }

    fun getSensorSettings(elementAddress: Int, appKeyIndex: Int, propertyId: Int) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configSensorSettingsGet = SensorSettingsGet(appkey, DeviceProperty.from(propertyId.toShort()))
        meshManagerApi.createMeshPdu(elementAddress, configSensorSettingsGet)
    }

    fun getSensorSetting(elementAddress: Int, appKeyIndex: Int, propertyId: Int, sensorSettingPropertyId: Int) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configSensorSettingGet = SensorSettingGet(appkey, DeviceProperty.from(propertyId.toShort()), DeviceProperty.from(sensorSettingPropertyId.toShort()))
        meshManagerApi.createMeshPdu(elementAddress, configSensorSettingGet)
    }

    fun setSensorSettingAck(elementAddress: Int, appKeyIndex: Int, propertyId: Int, sensorSettingPropertyId: Int, values: ByteArray) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val sensorId = DeviceProperty.from(sensorSettingPropertyId.toShort())
        val sensorVl = DeviceProperty.getCharacteristic(sensorId,values,0,values.count())

        val configSensorSettingSet = SensorSettingSet(appkey,DeviceProperty.from(propertyId.toShort()),sensorId,sensorVl)
        meshManagerApi.createMeshPdu(elementAddress,configSensorSettingSet)
    }

    fun setSensorSetting(elementAddress: Int, appKeyIndex: Int, propertyId: Int, sensorSettingPropertyId: Int, values: ByteArray):JSObject {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val sensorId = DeviceProperty.from(sensorSettingPropertyId.toShort())
        val sensorVl = DeviceProperty.getCharacteristic(sensorId,values,0,values.count())

        val configSensorSettingSet = SensorSettingSetUnacknowledged(appkey,DeviceProperty.from(propertyId.toShort()),sensorId,sensorVl)
        meshManagerApi.createMeshPdu(elementAddress,configSensorSettingSet)

        return JSObject().apply {
            put("propertyId", propertyId)
            put("sensorSettingPropertyId", sensorSettingPropertyId)
            put("sensorSetting", sensorVl.value)
        }
    }
}
