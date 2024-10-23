package com.lebrislo.bluetooth.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
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
import no.nordicsemi.android.mesh.MeshManagerApi
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode
import no.nordicsemi.android.mesh.transport.ConfigAppKeyAdd
import no.nordicsemi.android.mesh.transport.ConfigAppKeyStatus
import no.nordicsemi.android.mesh.transport.ConfigCompositionDataGet
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlGet
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlSet
import no.nordicsemi.android.mesh.transport.ConfigModelAppBind
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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class NrfMeshManager(private val context: Context) {
    private val tag: String = NrfMeshManager::class.java.simpleName

    private val meshCallbacksManager: MeshCallbacksManager
    private val meshProvisioningCallbacksManager: MeshProvisioningCallbacksManager
    private val meshStatusCallbacksManager: MeshStatusCallbacksManager
    private val bleCallbacksManager: BleCallbacksManager
    private val scannerRepository: ScannerRepository

    private var bleMeshManager: BleMeshManager = BleMeshManager(context)
    var meshManagerApi: MeshManagerApi = MeshManagerApi(context)

    private val addAppKeyStatusMap = ConcurrentHashMap<Int, CompletableDeferred<Boolean>>()

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
                synchronized(scannerRepository.devices) {
                    val devices = scannerRepository.devices.filter {
                        it.provisioned
                    }.toMutableList()

                    devices.sortBy { device -> device.scanResult?.rssi }

                    val device =  devices.first().device
                    Log.i(tag, "searchProxyMesh : Found a mesh proxy ${device!!.address}")
                    return device
                }
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

//    fun onCompositionDataStatusReceived(meshMessage: ConfigNetworkTransmitStatus) {
//        Log.d(tag, "onCompositionDataStatusReceived")
//        val unicastAddress = meshMessage.src
//        val operationSucceeded = meshMessage.statusCode == 0
//
//        if (operationSucceeded) {
//            compositionDataStatusMap[unicastAddress]?.complete(true)
//            compositionDataStatusMap.remove(unicastAddress)
//        }
//    }

    /**
     * Create an application key
     *
     * @return Boolean whether the application key was created successfully
     */
    fun createApplicationKey(): Boolean {
        val applicationKey = meshManagerApi.meshNetwork?.createAppKey()
        return meshManagerApi.meshNetwork?.addAppKey(applicationKey!!) ?: false
    }

    /**
     * Remove an application key from the mesh network
     *
     * @param appKeyIndex index of the application key
     *
     * @return Boolean whether the application key was removed successfully
     */
    fun removeApplicationKey(appKeyIndex: Int): Boolean {
        return meshManagerApi.meshNetwork?.getAppKey(appKeyIndex)?.let {
            meshManagerApi.meshNetwork?.removeAppKey(it)
        } ?: false
    }

    /**
     * Add an application key to a node
     *
     * Note: The application must be connected to a mesh proxy before sending messages
     *
     * @param elementAddress unicast address of the node's element
     * @param appKeyIndex index of the application key
     *
     * @return Boolean whether the message was sent successfully
     */
    fun addApplicationKeyToNode(elementAddress: Int, appKeyIndex: Int): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        addAppKeyStatusMap[appKeyIndex] = deferred

        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            deferred.cancel()
            return deferred
        }

        val netKey = meshManagerApi.meshNetwork?.primaryNetworkKey
        val appKey = meshManagerApi.meshNetwork?.getAppKey(appKeyIndex)

        val configModelAppBind = ConfigAppKeyAdd(netKey!!, appKey!!)
        meshManagerApi.createMeshPdu(elementAddress, configModelAppBind)

        return deferred
    }

    fun onAppKeyAddStatusReceived(meshMessage: ConfigAppKeyStatus) {
        val appKeyIndex = meshMessage.appKeyIndex
        val operationSucceeded = meshMessage.statusCode == 0

        addAppKeyStatusMap[appKeyIndex]?.complete(operationSucceeded)
        addAppKeyStatusMap.remove(appKeyIndex)
    }

    /**
     * Bind an application key to a model
     *
     * Note: The application must be connected to a mesh proxy before sending messages
     *
     * @param elementAddress unicast address of the node's element
     * @param appKeyIndex index of the application key
     * @param modelId model id
     *
     * @return Boolean whether the message was sent successfully
     */
    fun bindApplicationKeyToModel(elementAddress: Int, appKeyIndex: Int, modelId: Int): Boolean {
        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            return false
        }

        val configModelAppBind = ConfigModelAppBind(elementAddress, modelId, appKeyIndex)
        meshManagerApi.createMeshPdu(elementAddress, configModelAppBind)

        return true
    }

    fun initMeshNetwork() {
        meshManagerApi.loadMeshNetwork()
    }

    /**
     * Export the mesh network to a json string
     *
     * @return String
     */
    fun exportMeshNetwork(): String? {
        return meshManagerApi.exportMeshNetwork()
    }

    /**
     * Import a mesh network from a json string
     *
     * @param json json string of the mesh network
     */
    fun importMeshNetwork(json: String) {
        meshManagerApi.importMeshNetworkJson(json)
    }

    /**
     * Create a new mesh network
     *
     * @param networkName name of the mesh network
     *
     * @return Unit
     */
    fun initMeshNetwork(networkName: String) {
        meshManagerApi.resetMeshNetwork()
        meshManagerApi.meshNetwork!!.meshName = networkName
    }

    /**
     * Send a Generic OnOff Set message to a node
     *
     * Note: The application must be connected to a mesh proxy before sending messages
     *
     * @param address unicast address of the node
     * @param appKeyIndex index of the application key
     * @param onOffvalue on/off value to set
     * @param tId transaction id
     * @param transitionStep transition step
     * @param transitionResolution transition resolution
     * @param delay delay before the message is sent
     * @param acknowledgement whether to send an acknowledgement
     *
     * @return Boolean whether the message was sent successfully
     */
    fun sendGenericOnOffSet(
        address: Int,
        appKeyIndex: Int,
        onOffvalue: Boolean,
        tId: Int,
        transitionStep: Int? = 0,
        transitionResolution: Int? = 0,
        delay: Int = 0,
        acknowledgement: Boolean = false
    ): Boolean {
        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            return false
        }

        var meshMessage: MeshMessage? = null

        if (acknowledgement) {
            meshMessage = GenericOnOffSet(
                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
                onOffvalue,
                tId,
                transitionStep,
                transitionResolution,
                delay
            )
        } else {
            meshMessage = GenericOnOffSetUnacknowledged(
                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
                onOffvalue,
                tId,
                transitionStep,
                transitionResolution,
                delay
            )
        }

        meshManagerApi.createMeshPdu(address, meshMessage)
        return true
    }

    /**
     * Send Generic OnOff Get message to a node
     *
     * Note: The application must be connected to a mesh proxy before sending messages
     *
     * @param address unicast address of the node
     * @param appKeyIndex index of the application key
     *
     * @return Boolean whether the message was sent successfully
     */
    fun sendGenericOnOffGet(
        address: Int,
        appKeyIndex: Int
    ): Boolean {
        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            return false
        }

        val meshMessage = GenericOnOffGet(
            meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
        )

        meshManagerApi.createMeshPdu(address, meshMessage)
        return true
    }

    /**
     * Send a Generic Level Set message to a node
     *
     * Note: The application must be connected to a mesh proxy before sending messages
     *
     * @param address unicast address of the node
     * @param appKeyIndex index of the application key
     * @param level level to set
     * @param tId transaction id
     * @param transitionStep transition step
     * @param transitionResolution transition resolution
     * @param delay delay before the message is sent
     * @param acknowledgement whether to send an acknowledgement
     *
     * @return Boolean whether the message was sent successfully
     */
    fun sendGenericLevelSet(
        address: Int,
        appKeyIndex: Int,
        level: Int,
        tId: Int,
        transitionStep: Int? = 0,
        transitionResolution: Int? = 0,
        delay: Int = 0,
        acknowledgement: Boolean = false
    ): Boolean {
        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            return false
        }

        var meshMessage: MeshMessage? = null

        if (acknowledgement) {
            meshMessage = GenericLevelSet(
                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
                transitionStep,
                transitionResolution,
                delay,
                level,
                tId,
            )
        } else {
            meshMessage = GenericLevelSetUnacknowledged(
                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
                transitionStep,
                transitionResolution,
                delay,
                level,
                tId,
            )
        }

        meshManagerApi.createMeshPdu(address, meshMessage)
        return true
    }

    /**
     * Send a Generic Power Level Set message to a node
     *
     * Note: The application must be connected to a mesh proxy before sending messages
     *
     * @param address unicast address of the node
     * @param appKeyIndex index of the application key
     * @param powerLevel power level to set
     * @param tId transaction id
     * @param transitionStep transition step
     * @param transitionResolution transition resolution
     * @param delay delay before the message is sent
     * @param acknowledgement whether to send an acknowledgement
     *
     * @return Boolean whether the message was sent successfully
     */
    fun sendGenericPowerLevelSet(
        address: Int,
        appKeyIndex: Int,
        powerLevel: Int,
        tId: Int,
        transitionStep: Int? = 0,
        transitionResolution: Int? = 0,
        delay: Int = 0,
        acknowledgement: Boolean = false
    ): Boolean {
        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            return false
        }

        var meshMessage: MeshMessage? = null

//        if (acknowledgement) {
//            meshMessage = GenericPowerLevelSet(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                tId,
//                transitionStep,
//                transitionResolution,
//                powerLevel,
//                delay
//            )
//        } else {
//            meshMessage = GenericPowerLevelSetUnacknowledged(
//                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//                tId,
//                transitionStep,
//                transitionResolution,
//                powerLevel,
//                delay
//            )
//        }
//        meshManagerApi.createMeshPdu(address, meshMessage)
        return true
    }

    /**
     * Send a Generic Power Level Get message to a node
     *
     * Note: The application must be connected to a mesh proxy before sending messages
     *
     * @param address unicast address of the node
     * @param appKeyIndex index of the application key
     *
     * @return Boolean whether the message was sent successfully
     */
    fun sendGenericPowerLevelGet(
        address: Int,
        appKeyIndex: Int
    ): Boolean {
        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            return false
        }

//        val meshMessage = GenericPowerLevelGet(
//            meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
//        )
//
//        meshManagerApi.createMeshPdu(address, meshMessage)
        return true
    }

    /**
     * Send a Light HSL Set message to a node
     *
     * Note: The application must be connected to a mesh proxy before sending messages
     *
     * @param address unicast address of the node
     * @param appKeyIndex index of the application key
     * @param hue hue value to set
     * @param saturation saturation value to set
     * @param lightness lightness value to set
     * @param tId transaction id
     * @param transitionStep transition step
     * @param transitionResolution transition resolution
     * @param delay delay before the message is sent
     * @param acknowledgement whether to send an acknowledgement
     *
     * @return Boolean whether the message was sent successfully
     */
    fun sendLightHslSet(
        address: Int,
        appKeyIndex: Int,
        hue: Int,
        saturation: Int,
        lightness: Int,
        tId: Int,
        transitionStep: Int? = 0,
        transitionResolution: Int? = 0,
        delay: Int = 0,
        acknowledgement: Boolean = false
    ): Boolean {
        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            return false
        }

        var meshMessage: MeshMessage? = null

        if (acknowledgement) {
            meshMessage = LightHslSet(
                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
                transitionStep,
                transitionResolution,
                delay,
                lightness,
                hue,
                saturation,
                tId
            )
        } else {
            meshMessage = LightHslSetUnacknowledged(
                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
                transitionStep,
                transitionResolution,
                delay,
                lightness,
                hue,
                saturation,
                tId
            )
        }
        meshManagerApi.createMeshPdu(address, meshMessage)
        return true
    }

    fun sendLightHslGet(
        address: Int,
        appKeyIndex: Int
    ): Boolean {
        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            return false
        }

        val meshMessage = LightHslGet(
            meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
        )

        meshManagerApi.createMeshPdu(address, meshMessage)
        return true
    }

    /**
     * Send a Light CTL Set message to a node
     *
     * Note: The application must be connected to a mesh proxy before sending messages
     *
     * @param address unicast address of the node
     * @param appKeyIndex index of the application key
     * @param lightness lightness value to set
     * @param temperature temperature value to set
     * @param deltaUv delta uv value to set
     * @param tId transaction id
     * @param transitionStep transition step
     * @param transitionResolution transition resolution
     * @param delay delay before the message is sent
     * @param acknowledgement whether to send an acknowledgement
     *
     * @return Boolean whether the message was sent successfully
     */
    fun sendLightCtlSet(
        address: Int,
        appKeyIndex: Int,
        lightness: Int,
        temperature: Int,
        deltaUv: Int,
        tId: Int,
        transitionStep: Int? = 0,
        transitionResolution: Int? = 0,
        delay: Int = 0,
        acknowledgement: Boolean = false
    ): Boolean {
        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            return false
        }

        var meshMessage: MeshMessage? = null

        if (acknowledgement) {
            meshMessage = LightCtlSet(
                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
                transitionStep,
                transitionResolution,
                delay,
                lightness,
                temperature,
                deltaUv,
                tId
            )
        } else {
            meshMessage = LightCtlSetUnacknowledged(
                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
                transitionStep,
                transitionResolution,
                delay,
                lightness,
                temperature,
                deltaUv,
                tId
            )
        }
        meshManagerApi.createMeshPdu(address, meshMessage)
        return true
    }

    /**
     * Send a Vendor Model message to a node
     *
     * Note: The application must be connected to a mesh proxy before sending messages
     *
     * @param address unicast address of the node
     * @param appKeyIndex index of the application key
     * @param modelId model id
     * @param companyIdentifier company identifier
     * @param opCode operation code
     * @param payload parameters of the message
     * @param acknowledgement whether to send an acknowledgement
     *
     * @return Boolean whether the message was sent successfully
     */
    fun sendVendorModelMessage(
        address: Int,
        appKeyIndex: Int,
        modelId: Int,
        companyIdentifier: Int,
        opCode: Int,
        payload: ByteArray = byteArrayOf(),
        acknowledgement: Boolean = false
    ): Boolean {
        if (!bleMeshManager.isConnected) {
            Log.e(tag, "Not connected to a mesh proxy")
            return false
        }

        var meshMessage: MeshMessage? = null

        if (acknowledgement) {
            meshMessage = VendorModelMessageAcked(
                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
                modelId,
                companyIdentifier,
                opCode,
                payload
            )
        } else {
            meshMessage = VendorModelMessageUnacked(
                meshManagerApi.meshNetwork!!.getAppKey(appKeyIndex),
                modelId,
                companyIdentifier,
                opCode,
                payload
            )
        }
        meshManagerApi.createMeshPdu(address, meshMessage)
        return true
    }
}
