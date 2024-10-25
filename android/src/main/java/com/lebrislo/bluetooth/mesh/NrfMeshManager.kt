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
import no.nordicsemi.android.mesh.Group
import no.nordicsemi.android.mesh.MeshManagerApi
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode
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
    private fun formatNode(node: ProvisionedMeshNode):JSObject{
        return JSObject().apply {
            put("name",node.nodeName)
            put("deviceKey",MeshParserUtils.bytesToHex(node.deviceKey,false))
            put("unicastAddress",node.unicastAddress)
            put("security",if(node.security == 1) "secure" else "insecure")
            put("ttl",node.ttl)
            put("excluded",node.isExcluded)
            put("features",JSObject().apply {
                put("friend",node.nodeFeatures.friend)
                put("lowPower",node.nodeFeatures.lowPower)
                put("proxy",node.nodeFeatures.proxy)
                put("relay",node.nodeFeatures.relay)
            })
            put("netKeys",JSArray().apply {
                node.addedNetKeys.forEach {
                    put(JSObject().apply {
                        put("index",it.index)
                        put("updated",it.isUpdated)
                    })
                }
            })
            put("appKeys",JSArray().apply {
                node.addedAppKeys.forEach {
                    put(JSObject().apply {
                        put("index",it.index)
                        put("updated",it.isUpdated)
                    })
                }
            })
            put("elements",JSArray().apply {
                node.elements.values.forEach {
                    put(JSObject().apply {
                        put("name",it.name)
                        put("elementAddress",it.elementAddress)
                        put("location",it.locationDescriptor)
                        put("models",JSArray().apply {
                            it.meshModels.values.forEach {
                                put(JSObject().apply {
                                    put("modelId",it.modelId)
                                    put("bind",JSArray().apply {
                                        it.boundAppKeyIndexes.forEach {
                                            put(it)
                                        }
                                    })
                                    put("subscribe",JSArray().apply {
                                        it.subscribedAddresses.forEach {
                                            put(it)
                                        }
                                    })
                                })
                            }
                        })
                    })
                }
            })

            if (node.networkTransmitSettings != null) {
                put("networkTransmit", JSObject().apply {
                    put("count", node.networkTransmitSettings.networkTransmitCount)
                    put("interval", node.networkTransmitSettings.networkTransmissionInterval)
                    put("steps", node.networkTransmitSettings.networkIntervalSteps)
                })
            }
            if (node.companyIdentifier != null){
                put("cid", CompositionDataParser.formatCompanyIdentifier(node.companyIdentifier,false))
            }
            if (node.productIdentifier != null){
                put("pid", CompositionDataParser.formatProductIdentifier(node.productIdentifier,false))
            }
            if (node.versionIdentifier != null){
                put("vid", CompositionDataParser.formatVersionIdentifier(node.versionIdentifier,false))
            }
            if(node.crpl != null){
                put("crpl",CompositionDataParser.formatReplayProtectionCount(node.crpl,false))
            }
        }
    }

    private fun formatGroup(group:Group):JSObject{
        val network = meshManagerApi.meshNetwork!!
        val models = network.getModels(group)

        return JSObject().apply {
            put("name",group.name)
            put("address",group.address)
            put("devices",models.size)
        }
    }

    fun connectBle(bluetoothDevice: BluetoothDevice): Boolean {
        bleMeshManager.connect(bluetoothDevice).retry(3, 200).await()
        return bleMeshManager.isConnected
    }

    fun disconnectBle(): DisconnectRequest {
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

    fun getNodes(): List<ProvisionedMeshNode>{
        return meshManagerApi.meshNetwork?.nodes ?: listOf()
    }

    fun assertMeshNetwork(call: PluginCall):Boolean{
        if (meshManagerApi.meshNetwork == null){
            call.reject("meshNetwork not initialized.")
            return  false
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
    fun getMeshNetwork():JSObject {
        val network = meshManagerApi.meshNetwork!!

        return JSObject().apply {
            put("name",network.meshName)
            put("lastModified", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(network.timestamp))
            put("netKeys",JSArray().apply {
                network.netKeys.forEach {
                    put(JSObject().apply {
                        put("name",it.name)
                        put("index",it.keyIndex)
                        put("phase",it.phaseDescription)
                        put("key", MeshParserUtils.bytesToHex(it.key,false))
                        if (it.oldKey != null) {
                            put("oldKey", MeshParserUtils.bytesToHex(it.oldKey, false))
                        }
                        put("security",if(it.isMinSecurity) "secure" else "insecure")
                        put("lastModified", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(it.timestamp))
                    })
                }
            })
            put("appKeys",JSArray().apply {
                network.appKeys.forEach {
                    put(JSObject().apply {
                        put("name",it.name)
                        put("index",it.keyIndex)
                        put("key", MeshParserUtils.bytesToHex(it.key,false))
                        if (it.oldKey != null) {
                            put("oldKey", MeshParserUtils.bytesToHex(it.oldKey,false))
                        }
                        put("boundNetKeyIndex",it.boundNetKeyIndex)
                    })
                }
            })
            put("provisioners",JSArray().apply {
                network.provisioners.forEach {
                    put(JSObject().apply {
                        put("name",it.provisionerName)
                        it.allocatedUnicastRanges.forEach {
                            put("unicast",JSArray().apply {
                                put(JSObject().apply {
                                    put("lowerAddress",it.lowAddress)
                                    put("highAddress",it.highAddress)
                                })
                            })
                        }

                        it.allocatedGroupRanges.forEach {
                            put("group",JSArray().apply {
                                put(JSObject().apply {
                                    put("lowerAddress",it.lowAddress)
                                    put("highAddress",it.highAddress)
                                })
                            })
                        }

                        it.allocatedSceneRanges.forEach {
                            put("scene",JSArray().apply {
                                put(JSObject().apply {
                                    put("firstScene",it.firstScene)
                                    put("lastScene",it.lastScene)
                                })
                            })
                        }
                    })
                }
            })
            put("nodes",JSArray().apply {
                network.nodes.forEach {
                    put(formatNode(it))
                }
            })
            put("groups",JSArray().apply {
                network.groups.forEach {
                    put(formatGroup(it))
                }
            })
            put("networkExclusions",JSArray().apply {
                network.networkExclusions.forEach { (ivIndex, address) ->
                    put(JSObject().apply {
                        put("ivIndex",ivIndex)
                        put("addresses",JSArray().apply {
                            address.forEach {
                                put(it)
                            }
                        })
                    })
                }
            })
        }
    }

    fun getNode(unicastAddress: Int): JSObject?{
        val network = meshManagerApi.meshNetwork!!
        val node = network.getNode(unicastAddress) ?: return null
        return formatNode(node)
    }

    fun createAppKey(): JSObject {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.createAppKey()
        network.addAppKey(appkey)

        return  JSObject().apply {
            put("name",appkey.name)
            put("index",appkey.keyIndex)
            put("key", MeshParserUtils.bytesToHex(appkey.key,false))
            if (appkey.oldKey != null) {
                put("oldKey", MeshParserUtils.bytesToHex(appkey.oldKey,false))
            }
            put("boundNetKeyIndex",appkey.boundNetKeyIndex)
        }
    }

    fun removeAppKey(appKeyIndex: Int) {
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex) ?: return

        network.removeAppKey(appkey)
    }

    fun createGroup(name: String):JSObject{
        val network = meshManagerApi.meshNetwork!!
        val provisioner = network.selectedProvisioner

        val  group = network.createGroup(provisioner,name)
        return  JSObject().apply {
            put("name",group.name)
            put("address",group.address)
            put("devices",0)
        }
    }

    fun removeGroup(groupAddress: Int){
        val network = meshManagerApi.meshNetwork!!
        val group = network.getGroup(groupAddress)?:return
        val models = network.getModels(group)
        if (models.size!=0) return

        network.removeGroup(group)
    }

    fun getGroup(groupAddress: Int):JSObject?{
        val network = meshManagerApi.meshNetwork!!
        val group = network.getGroup(groupAddress)?:return null

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

                val device =  devices.first().device
                Log.i(tag, "searchProxyMesh : Found a mesh proxy ${device!!.address}")
                return device
            }
        }

        return null
    }

    suspend fun searchUnprovisionedBluetoothDevice(uuid: String): BluetoothDevice? {
        if (bleMeshManager.isConnected) {
            val macAddress = bleMeshManager.bluetoothDevice!!.address

            synchronized(scannerRepository.devices){
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
                if (device.provisioned){
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
        synchronized(scannerRepository.devices){
            scannerRepository.devices.clear()
        }

        scannerRepository.stopScanDevices()
        scannerRepository.startScanDevices()

        delay(scanDurationMs.toLong())

        val devices : MutableList<ExtendedBluetoothDevice> = mutableListOf()
        synchronized(scannerRepository.devices){
            scannerRepository.devices.forEach {
                devices.add(it)
            }
        }

        return  devices
    }

    fun identify(uuid: UUID) {
        meshManagerApi.identifyNode(uuid)
    }

    fun unprovisionedMeshNode(uuid: UUID): UnprovisionedMeshNode?{
        return meshProvisioningCallbacksManager.unprovisionedMeshNodes.firstOrNull { node ->
            node.deviceUuid == uuid
        }
    }

    fun provisionDevice(uuid: UUID) {
        val node = unprovisionedMeshNode(uuid)!!
        val provisioner = meshManagerApi.meshNetwork?.selectedProvisioner
        val unicastAddress = meshManagerApi.meshNetwork?.nextAvailableUnicastAddress(
            node.numberOfElements, provisioner!!
        )

        if (bleMeshManager.isConnected){
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

    fun getDefaultTTL(unicastAddress: Int){
        val configDefaultTtlGet = ConfigDefaultTtlGet()
        meshManagerApi.createMeshPdu(unicastAddress,configDefaultTtlGet)
    }

    fun setDefaultTTL(unicastAddress: Int,ttl: Int){
        val configDefaultTtlSet = ConfigDefaultTtlSet(ttl)
        meshManagerApi.createMeshPdu(unicastAddress,configDefaultTtlSet)
    }

    fun getNetworkTransmit(unicastAddress: Int){
        val configNetworkTransmitGet = ConfigNetworkTransmitGet()
        meshManagerApi.createMeshPdu(unicastAddress,configNetworkTransmitGet)
    }

    fun setNetworkTransmit(unicastAddress: Int,networkTransmitCount: Int, networkTransmitIntervalSteps: Int){
        val configNetworkTransmitSet = ConfigNetworkTransmitSet(networkTransmitCount,networkTransmitIntervalSteps)
        meshManagerApi.createMeshPdu(unicastAddress, configNetworkTransmitSet)
    }

    fun addAppKey(unicastAddress: Int,appKeyIndex: Int){
        val network = meshManagerApi.meshNetwork!!
        val netkey = network.primaryNetworkKey
        val appkey = network.getAppKey(appKeyIndex)

        val configAppKeyAdd = ConfigAppKeyAdd(netkey,appkey)
        meshManagerApi.createMeshPdu(unicastAddress,configAppKeyAdd)
    }

    fun deleteAppKey(unicastAddress: Int,appKeyIndex: Int){
        val network = meshManagerApi.meshNetwork!!
        val netkey = network.primaryNetworkKey
        val appkey = network.getAppKey(appKeyIndex)

        val configAppKeyDelete = ConfigAppKeyDelete(netkey,appkey)
        meshManagerApi.createMeshPdu(unicastAddress,configAppKeyDelete)
    }

    fun getAppKeys(unicastAddress: Int){
        val network = meshManagerApi.meshNetwork!!
        val netkey = network.primaryNetworkKey

        val configAppKeyGet = ConfigAppKeyGet(netkey)
        meshManagerApi.createMeshPdu(unicastAddress,configAppKeyGet)
    }

    fun bindAppKey(unicastAddress:Int,elementAddress: Int,modelId: Int,appKeyIndex:Int ){
        val configModelAppBind = ConfigModelAppBind(elementAddress,modelId,appKeyIndex)
        meshManagerApi.createMeshPdu(unicastAddress,configModelAppBind)
    }

    fun unbindAppKey(unicastAddress:Int,elementAddress: Int,modelId: Int,appKeyIndex:Int){
        val configModelAppUnbind = ConfigModelAppUnbind(elementAddress,modelId,appKeyIndex)
        meshManagerApi.createMeshPdu(unicastAddress,configModelAppUnbind)
    }

    fun subscribe(unicastAddress:Int,elementAddress: Int,subscriptionAddress: Int,modelId: Int){
        val configModelSubscriptionAdd = ConfigModelSubscriptionAdd(elementAddress,subscriptionAddress,modelId)
        meshManagerApi.createMeshPdu(unicastAddress,configModelSubscriptionAdd)
    }

    fun unsubscribe(unicastAddress:Int,elementAddress: Int,subscriptionAddress: Int,modelId: Int){
        val configModelSubscriptionDelete = ConfigModelSubscriptionDelete(elementAddress,subscriptionAddress,modelId)
        meshManagerApi.createMeshPdu(unicastAddress,configModelSubscriptionDelete)
    }

    fun unsubscribeAll(unicastAddress:Int,elementAddress: Int,subscriptionAddress: Int){
        val configModelSubscriptionDeleteAll = ConfigModelSubscriptionDeleteAll(elementAddress,subscriptionAddress)
        meshManagerApi.createMeshPdu(unicastAddress,configModelSubscriptionDeleteAll)
    }

    fun getOnOff(elementAddress: Int, appKeyIndex: Int){
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configGenericOnOffGet = GenericOnOffGet(appkey)
        meshManagerApi.createMeshPdu(elementAddress,configGenericOnOffGet)
    }

    fun setOnOffAck(elementAddress: Int, appKeyIndex: Int,onOff: Boolean){
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configGenericOnOffSet = GenericOnOffSet(appkey,onOff, Random.nextInt())
        meshManagerApi.createMeshPdu(elementAddress,configGenericOnOffSet)
    }

    fun setOnOff(elementAddress: Int, appKeyIndex: Int,onOff: Boolean):JSObject{
        val network = meshManagerApi.meshNetwork!!
        val appkey = network.getAppKey(appKeyIndex)

        val configGenericOnOffSet = GenericOnOffSetUnacknowledged(appkey,onOff, Random.nextInt())
        meshManagerApi.createMeshPdu(elementAddress,configGenericOnOffSet)

        return JSObject().apply {
            put("src",configGenericOnOffSet.src)
            put("dst",configGenericOnOffSet.dst)
            put("opcode",configGenericOnOffSet.opCode)
            put("data",JSObject().apply {
                put("onOff",onOff)
            })
        }
    }

//    /**
//     * Create a new mesh network
//     *
//     * @param networkName name of the mesh network
//     *
//     * @return Unit
//     */
//    fun initMeshNetwork(networkName: String) {
//        meshManagerApi.resetMeshNetwork()
//        meshManagerApi.meshNetwork!!.meshName = networkName
//    }

//    /**
//     * Send a Generic OnOff Set message to a node
//     *
//     * Note: The application must be connected to a mesh proxy before sending messages
//     *
//     * @param address unicast address of the node
//     * @param appKeyIndex index of the application key
//     * @param onOffvalue on/off value to set
//     * @param tId transaction id
//     * @param transitionStep transition step
//     * @param transitionResolution transition resolution
//     * @param delay delay before the message is sent
//     * @param acknowledgement whether to send an acknowledgement
//     *
//     * @return Boolean whether the message was sent successfully
//     */
//    fun sendGenericOnOffSet(
//        address: Int,
//        appKeyIndex: Int,
//        onOffvalue: Boolean,
//        tId: Int,
//        transitionStep: Int? = 0,
//        transitionResolution: Int? = 0,
//        delay: Int = 0,
//        acknowledgement: Boolean = false
//    ): Boolean {
//        if (!bleMeshManager.isConnected) {
//            Log.e(tag, "Not connected to a mesh proxy")
//            return false
//        }
//
//        var meshMessage: MeshMessage? = null
//
//        if (acknowledgement) {
//            meshMessage = GenericOnOffSet(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                onOffvalue,
//                tId,
//                transitionStep,
//                transitionResolution,
//                delay
//            )
//        } else {
//            meshMessage = GenericOnOffSetUnacknowledged(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                onOffvalue,
//                tId,
//                transitionStep,
//                transitionResolution,
//                delay
//            )
//        }
//
//        meshManagerApi.createMeshPdu(address, meshMessage)
//        return true
//    }
//
//    /**
//     * Send Generic OnOff Get message to a node
//     *
//     * Note: The application must be connected to a mesh proxy before sending messages
//     *
//     * @param address unicast address of the node
//     * @param appKeyIndex index of the application key
//     *
//     * @return Boolean whether the message was sent successfully
//     */
//    fun sendGenericOnOffGet(
//        address: Int,
//        appKeyIndex: Int
//    ): Boolean {
//        if (!bleMeshManager.isConnected) {
//            Log.e(tag, "Not connected to a mesh proxy")
//            return false
//        }
//
//        val meshMessage = GenericOnOffGet(
//            meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//        )
//
//        meshManagerApi.createMeshPdu(address, meshMessage)
//        return true
//    }
//
//    /**
//     * Send a Generic Level Set message to a node
//     *
//     * Note: The application must be connected to a mesh proxy before sending messages
//     *
//     * @param address unicast address of the node
//     * @param appKeyIndex index of the application key
//     * @param level level to set
//     * @param tId transaction id
//     * @param transitionStep transition step
//     * @param transitionResolution transition resolution
//     * @param delay delay before the message is sent
//     * @param acknowledgement whether to send an acknowledgement
//     *
//     * @return Boolean whether the message was sent successfully
//     */
//    fun sendGenericLevelSet(
//        address: Int,
//        appKeyIndex: Int,
//        level: Int,
//        tId: Int,
//        transitionStep: Int? = 0,
//        transitionResolution: Int? = 0,
//        delay: Int = 0,
//        acknowledgement: Boolean = false
//    ): Boolean {
//        if (!bleMeshManager.isConnected) {
//            Log.e(tag, "Not connected to a mesh proxy")
//            return false
//        }
//
//        var meshMessage: MeshMessage? = null
//
//        if (acknowledgement) {
//            meshMessage = GenericLevelSet(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                transitionStep,
//                transitionResolution,
//                delay,
//                level,
//                tId,
//            )
//        } else {
//            meshMessage = GenericLevelSetUnacknowledged(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                transitionStep,
//                transitionResolution,
//                delay,
//                level,
//                tId,
//            )
//        }
//
//        meshManagerApi.createMeshPdu(address, meshMessage)
//        return true
//    }
//
//    /**
//     * Send a Generic Power Level Set message to a node
//     *
//     * Note: The application must be connected to a mesh proxy before sending messages
//     *
//     * @param address unicast address of the node
//     * @param appKeyIndex index of the application key
//     * @param powerLevel power level to set
//     * @param tId transaction id
//     * @param transitionStep transition step
//     * @param transitionResolution transition resolution
//     * @param delay delay before the message is sent
//     * @param acknowledgement whether to send an acknowledgement
//     *
//     * @return Boolean whether the message was sent successfully
//     */
//    fun sendGenericPowerLevelSet(
//        address: Int,
//        appKeyIndex: Int,
//        powerLevel: Int,
//        tId: Int,
//        transitionStep: Int? = 0,
//        transitionResolution: Int? = 0,
//        delay: Int = 0,
//        acknowledgement: Boolean = false
//    ): Boolean {
//        if (!bleMeshManager.isConnected) {
//            Log.e(tag, "Not connected to a mesh proxy")
//            return false
//        }
//
//        var meshMessage: MeshMessage? = null
//
////        if (acknowledgement) {
////            meshMessage = GenericPowerLevelSet(
////                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
////                tId,
////                transitionStep,
////                transitionResolution,
////                powerLevel,
////                delay
////            )
////        } else {
////            meshMessage = GenericPowerLevelSetUnacknowledged(
////                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
////                tId,
////                transitionStep,
////                transitionResolution,
////                powerLevel,
////                delay
////            )
////        }
////        meshManagerApi.createMeshPdu(address, meshMessage)
//        return true
//    }
//
//    /**
//     * Send a Generic Power Level Get message to a node
//     *
//     * Note: The application must be connected to a mesh proxy before sending messages
//     *
//     * @param address unicast address of the node
//     * @param appKeyIndex index of the application key
//     *
//     * @return Boolean whether the message was sent successfully
//     */
//    fun sendGenericPowerLevelGet(
//        address: Int,
//        appKeyIndex: Int
//    ): Boolean {
//        if (!bleMeshManager.isConnected) {
//            Log.e(tag, "Not connected to a mesh proxy")
//            return false
//        }
//
////        val meshMessage = GenericPowerLevelGet(
////            meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
////        )
////
////        meshManagerApi.createMeshPdu(address, meshMessage)
//        return true
//    }
//
//    /**
//     * Send a Light HSL Set message to a node
//     *
//     * Note: The application must be connected to a mesh proxy before sending messages
//     *
//     * @param address unicast address of the node
//     * @param appKeyIndex index of the application key
//     * @param hue hue value to set
//     * @param saturation saturation value to set
//     * @param lightness lightness value to set
//     * @param tId transaction id
//     * @param transitionStep transition step
//     * @param transitionResolution transition resolution
//     * @param delay delay before the message is sent
//     * @param acknowledgement whether to send an acknowledgement
//     *
//     * @return Boolean whether the message was sent successfully
//     */
//    fun sendLightHslSet(
//        address: Int,
//        appKeyIndex: Int,
//        hue: Int,
//        saturation: Int,
//        lightness: Int,
//        tId: Int,
//        transitionStep: Int? = 0,
//        transitionResolution: Int? = 0,
//        delay: Int = 0,
//        acknowledgement: Boolean = false
//    ): Boolean {
//        if (!bleMeshManager.isConnected) {
//            Log.e(tag, "Not connected to a mesh proxy")
//            return false
//        }
//
//        var meshMessage: MeshMessage? = null
//
//        if (acknowledgement) {
//            meshMessage = LightHslSet(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                transitionStep,
//                transitionResolution,
//                delay,
//                lightness,
//                hue,
//                saturation,
//                tId
//            )
//        } else {
//            meshMessage = LightHslSetUnacknowledged(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                transitionStep,
//                transitionResolution,
//                delay,
//                lightness,
//                hue,
//                saturation,
//                tId
//            )
//        }
//        meshManagerApi.createMeshPdu(address, meshMessage)
//        return true
//    }
//
//    fun sendLightHslGet(
//        address: Int,
//        appKeyIndex: Int
//    ): Boolean {
//        if (!bleMeshManager.isConnected) {
//            Log.e(tag, "Not connected to a mesh proxy")
//            return false
//        }
//
//        val meshMessage = LightHslGet(
//            meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//        )
//
//        meshManagerApi.createMeshPdu(address, meshMessage)
//        return true
//    }
//
//    /**
//     * Send a Light CTL Set message to a node
//     *
//     * Note: The application must be connected to a mesh proxy before sending messages
//     *
//     * @param address unicast address of the node
//     * @param appKeyIndex index of the application key
//     * @param lightness lightness value to set
//     * @param temperature temperature value to set
//     * @param deltaUv delta uv value to set
//     * @param tId transaction id
//     * @param transitionStep transition step
//     * @param transitionResolution transition resolution
//     * @param delay delay before the message is sent
//     * @param acknowledgement whether to send an acknowledgement
//     *
//     * @return Boolean whether the message was sent successfully
//     */
//    fun sendLightCtlSet(
//        address: Int,
//        appKeyIndex: Int,
//        lightness: Int,
//        temperature: Int,
//        deltaUv: Int,
//        tId: Int,
//        transitionStep: Int? = 0,
//        transitionResolution: Int? = 0,
//        delay: Int = 0,
//        acknowledgement: Boolean = false
//    ): Boolean {
//        if (!bleMeshManager.isConnected) {
//            Log.e(tag, "Not connected to a mesh proxy")
//            return false
//        }
//
//        var meshMessage: MeshMessage? = null
//
//        if (acknowledgement) {
//            meshMessage = LightCtlSet(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                transitionStep,
//                transitionResolution,
//                delay,
//                lightness,
//                temperature,
//                deltaUv,
//                tId
//            )
//        } else {
//            meshMessage = LightCtlSetUnacknowledged(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                transitionStep,
//                transitionResolution,
//                delay,
//                lightness,
//                temperature,
//                deltaUv,
//                tId
//            )
//        }
//        meshManagerApi.createMeshPdu(address, meshMessage)
//        return true
//    }
//
//    /**
//     * Send a Vendor Model message to a node
//     *
//     * Note: The application must be connected to a mesh proxy before sending messages
//     *
//     * @param address unicast address of the node
//     * @param appKeyIndex index of the application key
//     * @param modelId model id
//     * @param companyIdentifier company identifier
//     * @param opCode operation code
//     * @param payload parameters of the message
//     * @param acknowledgement whether to send an acknowledgement
//     *
//     * @return Boolean whether the message was sent successfully
//     */
//    fun sendVendorModelMessage(
//        address: Int,
//        appKeyIndex: Int,
//        modelId: Int,
//        companyIdentifier: Int,
//        opCode: Int,
//        payload: ByteArray = byteArrayOf(),
//        acknowledgement: Boolean = false
//    ): Boolean {
//        if (!bleMeshManager.isConnected) {
//            Log.e(tag, "Not connected to a mesh proxy")
//            return false
//        }
//
//        var meshMessage: MeshMessage? = null
//
//        if (acknowledgement) {
//            meshMessage = VendorModelMessageAcked(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                modelId,
//                companyIdentifier,
//                opCode,
//                payload
//            )
//        } else {
//            meshMessage = VendorModelMessageUnacked(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                modelId,
//                companyIdentifier,
//                opCode,
//                payload
//            )
//        }
//        meshManagerApi.createMeshPdu(address, meshMessage)
//        return true
//    }
}
