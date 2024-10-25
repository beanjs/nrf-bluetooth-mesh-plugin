package com.lebrislo.bluetooth.mesh

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResult
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import com.lebrislo.bluetooth.mesh.plugin.PluginCallManager
import com.lebrislo.bluetooth.mesh.utils.BluetoothStateReceiver
import com.lebrislo.bluetooth.mesh.utils.Permissions
import com.lebrislo.bluetooth.mesh.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.mesh.Features
import no.nordicsemi.android.mesh.MeshManagerApi
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes
import no.nordicsemi.android.mesh.utils.CompanyIdentifiers
import no.nordicsemi.android.mesh.utils.CompositionDataParser
import no.nordicsemi.android.mesh.utils.MeshParserUtils
import java.text.DateFormat
import java.util.UUID

@SuppressLint("MissingPermission")
@CapacitorPlugin(
    name = "NrfMesh",
    permissions = [
        Permission(
                strings = [
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                ], alias = "ACCESS_COARSE_LOCATION"
        ),
        Permission(
                strings = [
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                ], alias = "ACCESS_FINE_LOCATION"
        ),
        Permission(
                strings = [
                    android.Manifest.permission.BLUETOOTH,
                ], alias = "BLUETOOTH"
        ),
        Permission(
                strings = [
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                ], alias = "BLUETOOTH_ADMIN"
        ),
        Permission(
                strings = [
                    // Manifest.permission.BLUETOOTH_SCAN
                    "android.permission.BLUETOOTH_SCAN",
                ], alias = "BLUETOOTH_SCAN"
        ),
        Permission(
                strings = [
                    // Manifest.permission.BLUETOOTH_ADMIN
                    "android.permission.BLUETOOTH_CONNECT",
                ], alias = "BLUETOOTH_CONNECT"
        ),
    ]
)
class NrfMeshPlugin : Plugin() {
    private val tag: String = NrfMeshPlugin::class.java.simpleName

    companion object {
        val MESH_EVENT_STRING: String = "meshEvent"
        val BLUETOOTH_ADAPTER_EVENT_STRING: String = "bluetoothAdapterEvent"
    }

    private var aliases: Array<String> = arrayOf()
    private lateinit var implementation: NrfMeshManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothStateReceiver: BroadcastReceiver

    @SuppressLint("ServiceCast")
    override fun load() {
        this.implementation = NrfMeshManager(this.context)
        PluginCallManager.getInstance().setPlugin(this)

        aliases = if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                    "BLUETOOTH_SCAN",
                    "BLUETOOTH_CONNECT",
                    "ACCESS_FINE_LOCATION",
            )
        } else {
            arrayOf(
                    "BLUETOOTH",
                    "BLUETOOTH_ADMIN",
                    "ACCESS_FINE_LOCATION",
                    "ACCESS_COARSE_LOCATION",
            )
        }
    }

    override fun handleOnStart() {
        Log.d(tag, "handleOnStart")
        super.handleOnStart()
    }

    override fun handleOnStop() {
        Log.d(tag, "handleOnStop")
        super.handleOnStop()
        try {
            context.unregisterReceiver(bluetoothStateReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e(tag, "handleOnStop : Receiver not registered")
        }

        if (implementation.isBleConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
                implementation.disconnectBle().await()
            }
        }
        implementation.stopScan()
    }

    override fun handleOnDestroy() {
        Log.d(tag, "handleOnDestroy")
        super.handleOnDestroy()
        try {
            context.unregisterReceiver(bluetoothStateReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e(tag, "handleOnDestroy : Receiver not registered")
        }

        if (implementation.isBleConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
                implementation.disconnectBle().await()
            }
        }
        implementation.stopScan()
    }

    @PermissionCallback
    @PluginMethod
    override fun checkPermissions(call: PluginCall) {
        if (!activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return call.reject("BLE is not supported.")
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter == null) {
            return call.reject("BLE is not available.")
        }

        call.resolve(JSObject().apply {
            aliases.forEach {
                put(it,getPermissionState(it))
            }
        })
    }

    @PluginMethod
    override fun requestPermissions(call: PluginCall) {
        requestPermissionForAliases(aliases, call, "checkPermissions")
    }

    private fun connectedToUnprovisionedDestinations(destinationMacAddress: String): Boolean {
        return implementation.isBleConnected() && implementation.connectedDevice()?.address == destinationMacAddress
    }

    private suspend fun connectionToUnprovisionedDevice(
            destinationMacAddress: String,
            destinationUuid: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            if (!connectedToUnprovisionedDestinations(destinationMacAddress)) {
                if (implementation.isBleConnected()) {
                    withContext(Dispatchers.IO) {
                        implementation.disconnectBle().await()
                    }
                }

                val bluetoothDevice = withContext(Dispatchers.IO) {
                    implementation.searchUnprovisionedBluetoothDevice(destinationUuid)
                }

                if (bluetoothDevice == null) {
                    Log.d(tag, "connectionToUnprovisionedDevice : Failed to find unprovisioned device")
                    return@withContext false
                }

                withContext(Dispatchers.IO) {
                    implementation.connectBle(bluetoothDevice)
                }
            }
            return@withContext true
        }
    }

    private suspend fun connectionToProvisionedDevice(
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val proxy = withContext(Dispatchers.IO) {
                implementation.searchProxyMesh()
            }

            if (proxy == null) {
                Log.d(tag, "connectionToProvisionedDevice : Failed to find proxy node")
                return@withContext false
            }

            withContext(Dispatchers.IO) {
                implementation.connectBle(proxy)
            }
            return@withContext true
        }
    }

    private fun assertBluetoothAdapter(call: PluginCall): Boolean {
        if (bluetoothAdapter == null) {
            call.reject("Bluetooth LE not initialized.")
            return false
        }
        return true
    }

    @PluginMethod
    fun isBluetoothEnabled(call: PluginCall) {
        val result = JSObject()
        result.put("enabled", Permissions.isBleEnabled(context))
        call.resolve(result)
    }

    @PluginMethod
    fun requestBluetoothEnable(call: PluginCall) {
        val intent = Intent(ACTION_REQUEST_ENABLE)
        startActivityForResult(call, intent, "handleRequestEnableResult")
    }

    @ActivityCallback
    private fun handleRequestEnableResult(call: PluginCall, result: ActivityResult) {
        call.resolve(JSObject().put("enabled", result.resultCode == Activity.RESULT_OK))
    }

    @PluginMethod
    fun initMeshNetwork(call: PluginCall) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Register for Bluetooth state changes
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        bluetoothStateReceiver = BluetoothStateReceiver(this)
        context.registerReceiver(bluetoothStateReceiver, filter)

        implementation.initMeshNetwork()
        CoroutineScope(Dispatchers.IO).launch {
//        delay(500)
            implementation.startScan()
            call.resolve()
        }
    }

    @PluginMethod
    fun getMeshNetwork(call: PluginCall){
        if (!implementation.assertMeshNetwork(call)) return

        CoroutineScope(Dispatchers.IO).launch {
            call.resolve(implementation.getMeshNetwork())
        }
    }

    @PluginMethod
    fun getNode(call: PluginCall){
        if (!implementation.assertMeshNetwork(call)) return

        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")

        CoroutineScope(Dispatchers.IO).launch {
            call.resolve(implementation.getNode(unicastAddress))
        }
    }

    @PluginMethod
    fun exportMeshNetwork(call: PluginCall) {
        if (!implementation.assertMeshNetwork(call)) return

        CoroutineScope(Dispatchers.IO).launch {
            val result = implementation.exportMeshNetwork()
            if (result != null) {
                call.resolve(JSObject().put("meshNetwork", result))
            } else {
                call.reject("Failed to export mesh network")
            }
        }
    }


    @PluginMethod
    fun importMeshNetwork(call: PluginCall) {
        if (!implementation.assertMeshNetwork(call)) return

        val meshNetwork = call.getString("meshNetwork")
                ?: return call.reject("meshNetwork is required")


        CoroutineScope(Dispatchers.IO).launch {
            implementation.importMeshNetwork(meshNetwork)

            call.resolve()
        }
    }

    @PluginMethod
    fun createAppKey(call: PluginCall) {
        if (!implementation.assertMeshNetwork(call)) return

        CoroutineScope(Dispatchers.IO).launch {
            call.resolve(implementation.createAppKey())
        }
    }

    @PluginMethod
    fun removeAppKey(call: PluginCall){
        if (!implementation.assertMeshNetwork(call)) return

        val appKeyIndex = call.getInt("index")
                ?: return call.reject("index is required")

        CoroutineScope(Dispatchers.IO).launch {
            implementation.removeAppKey(appKeyIndex)
            call.resolve()
        }
    }

    @PluginMethod
    fun createGroup(call: PluginCall){
        if (!implementation.assertMeshNetwork(call)) return

        val name = call.getString("name")
                ?: return call.reject("name is required")

        CoroutineScope(Dispatchers.IO).launch {
            call.resolve(implementation.createGroup(name))
        }
    }

    @PluginMethod
    fun removeGroup(call: PluginCall){
        if (!implementation.assertMeshNetwork(call)) return

        val groupAddress = call.getInt("groupAddress")
                ?: return call.reject("groupAddress is required")

        CoroutineScope(Dispatchers.IO).launch {
            implementation.removeGroup(groupAddress)
            call.resolve()
        }
    }

    @PluginMethod
    fun getGroup(call: PluginCall){
        if (!implementation.assertMeshNetwork(call)) return

        val groupAddress = call.getInt("groupAddress")
                ?: return call.reject("groupAddress is required")

        CoroutineScope(Dispatchers.IO).launch {
            call.resolve(implementation.getGroup(groupAddress))
        }
    }

    @PluginMethod
    fun scanMeshDevices(call: PluginCall) {
        val scanDuration = call.getInt("timeout", 5000)

        if (!Permissions.isBleEnabled(context)) {
            return call.reject("Bluetooth is disabled")
        }

        if (!Permissions.isLocationGranted(context)) {
            return call.reject("Location permission is required")
        }

        CoroutineScope(Dispatchers.IO).launch {
            val devices = implementation.scanMeshDevices(scanDuration!!)

            // return a dict of devices, unprovisioned and provisioned
            val result = JSObject().apply {
                put("unprovisioned", JSArray().apply {
                    devices.forEach {
                        if (it.provisioned) return@forEach

                        val serviceData = Utils.getServiceData(
                            it.scanResult!!,
                            MeshManagerApi.MESH_PROVISIONING_UUID
                        )?: return@forEach

                        if (serviceData.size < 18) return@forEach

                        val uuid: UUID = implementation.meshManagerApi.getDeviceUuid(serviceData)

                        put(JSObject().apply {
                            put("uuid", uuid.toString())
                            put("macAddress", it.scanResult.device.address)
                            put("rssi", it.rssi)
                            put("name", it.name)
                        })
                    }
                })
                put("proxy", JSArray().apply {
                    devices.forEach {
                        if (!it.provisioned) return@forEach

                        put(JSObject().apply {
                            put("macAddress", it.scanResult?.device?.address)
                            put("rssi", it.rssi)
                            put("name", it.name)
                        })
                    }
                })
                put("provisioned",JSArray().apply {
                    val nodes = implementation.getNodes()
                    nodes.forEach{
                        put(JSObject().apply {
                            put("name",it.nodeName)
                            put("provisionedTime",DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG).format(it.timeStamp))
                            put("unicastAddress",it.unicastAddress)
                            put("security",it.isSecurelyProvisioned)
                            put("deviceKey",MeshParserUtils.bytesToHex(it.deviceKey,false))
                            if (it.companyIdentifier != null) {
                                put("companyIdentifier", CompanyIdentifiers.getCompanyName(it.companyIdentifier.toShort()))
                            }
                            if(it.productIdentifier !=null){
                                put("productIdentifier",CompositionDataParser.formatProductIdentifier(it.productIdentifier,false))
                            }
                            if (it.versionIdentifier != null){
                                put("productVersion",CompositionDataParser.formatVersionIdentifier(it.versionIdentifier,false))
                            }
                            if (it.crpl != null){
                                put("replayProtectionCount",CompositionDataParser.formatReplayProtectionCount(it.crpl,false))
                            }

                            if(it.nodeFeatures != null) {
                                put("nodeFeaturesSupported", JSObject().apply {
                                    put("relay", it.nodeFeatures.isRelayFeatureSupported)
                                    put("proxy", it.nodeFeatures.isProxyFeatureSupported)
                                    put("friend", it.nodeFeatures.isFriendFeatureSupported)
                                    put("lowPower", it.nodeFeatures.isLowPowerFeatureSupported)
                                })
                                put("nodeFeatures", JSObject().apply {
                                    put("relay", it.nodeFeatures.relay == Features.ENABLED)
                                    put("proxy", it.nodeFeatures.proxy == Features.ENABLED)
                                    put("friend", it.nodeFeatures.friend == Features.ENABLED)
                                    put("lowPower", it.nodeFeatures.lowPower == Features.ENABLED)
                                })
                            }
                        })
                    }

                })
            }
            call.resolve(result)
        }
    }

    @PluginMethod
    fun getProvisioningCapabilities(call: PluginCall) {
        val macAddress = call.getString("macAddress")
                ?: return call.reject("macAddress is required")
        val uuid = call.getString("uuid")
                ?: return call.reject("uuid is required")

        CoroutineScope(Dispatchers.Main).launch {
            val connected = connectionToUnprovisionedDevice(macAddress, uuid)
            if (!connected) {
                return@launch call.reject("Failed to connect to device : $macAddress $uuid")
            }

            PluginCallManager.getInstance()
                .addMeshPluginCall(PluginCallManager.MESH_NODE_IDENTIFY, call)

            implementation.identify(UUID.fromString(uuid))
        }
    }

    @PluginMethod
    fun provisionDevice(call: PluginCall) {
        val macAddress = call.getString("macAddress")
                ?: return call.reject("macAddress is required")
        val uuid = call.getString("uuid")
                ?: return call.reject("uuid is required")

        CoroutineScope(Dispatchers.Main).launch {
            val connected = connectionToUnprovisionedDevice(macAddress, uuid)
            if (!connected) {
                return@launch call.reject("Failed to connect to device : $macAddress $uuid")
            }

            implementation.unprovisionedMeshNode(UUID.fromString(uuid))
                    ?: return@launch call.reject("Unprovisioned Mesh Node not found, try identifying the node first")

            PluginCallManager.getInstance()
                    .addMeshPluginCall(PluginCallManager.MESH_NODE_PROVISION, call)

            implementation.provisionDevice(UUID.fromString(uuid))
        }
    }

    @PluginMethod
    fun unprovisionDevice(call: PluginCall) {
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_NODE_RESET, unicastAddress, call)

            implementation.unprovisionDevice(unicastAddress)
        }
    }

    @PluginMethod
    fun getCompositionData(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_GET, unicastAddress, call)

            implementation.getCompositionData(unicastAddress)
        }
    }

    @PluginMethod
    fun getDefaultTTL(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_DEFAULT_TTL_GET, unicastAddress, call)

            implementation.getDefaultTTL(unicastAddress)
        }
    }

    @PluginMethod
    fun setDefaultTTL(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")

        val ttl = call.getInt("ttl")
                ?: return call.reject("ttl is required")


        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_DEFAULT_TTL_SET, unicastAddress, call)

            implementation.setDefaultTTL(unicastAddress,ttl)
        }
    }

    @PluginMethod
    fun getNetworkTransmit(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_NETWORK_TRANSMIT_GET, unicastAddress, call)

            implementation.getNetworkTransmit(unicastAddress)
        }
    }

    @PluginMethod
    fun setNetworkTransmit(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")
        val networkTransmitCount = call.getInt("networkTransmitCount")
                ?: return call.reject("networkTransmitCount is required")
        val networkTransmitIntervalSteps = call.getInt("networkTransmitIntervalSteps")
                ?: return call.reject("networkTransmitIntervalSteps is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_NETWORK_TRANSMIT_SET, unicastAddress, call)

            implementation.setNetworkTransmit(unicastAddress,networkTransmitCount,networkTransmitIntervalSteps)
        }
    }

    @PluginMethod
    fun addAppKey(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")
        val appKeyIndex = call.getInt("appKeyIndex")
                ?: return call.reject("appKeyIndex is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_APPKEY_ADD.toInt(), unicastAddress, call)

            implementation.addAppKey(unicastAddress,appKeyIndex)
        }
    }

    @PluginMethod
    fun delAppKey(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")
        val appKeyIndex = call.getInt("appKeyIndex")
                ?: return call.reject("appKeyIndex is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_APPKEY_DELETE, unicastAddress, call)

            implementation.delAppKey(unicastAddress,appKeyIndex)
        }
    }

    @PluginMethod
    fun getAppKeys(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_APPKEY_GET, unicastAddress, call)

            implementation.getAppKeys(unicastAddress)
        }
    }

    @PluginMethod
    fun bindAppKey(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")
        val elementAddress = call.getInt("elementAddress")
                ?: return call.reject("elementAddress is required")
        val modelId = call.getInt("modelId")
                ?: return call.reject("modelId is required")
        val appKeyIndex = call.getInt("appKeyIndex")
                ?: return call.reject("appKeyIndex is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_MODEL_APP_BIND, unicastAddress, call)

            implementation.bindAppKey(unicastAddress,elementAddress,modelId,appKeyIndex)
        }
    }

    @PluginMethod
    fun unbindAppKey(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")
        val elementAddress = call.getInt("elementAddress")
                ?: return call.reject("elementAddress is required")
        val modelId = call.getInt("modelId")
                ?: return call.reject("modelId is required")
        val appKeyIndex = call.getInt("appKeyIndex")
                ?: return call.reject("appKeyIndex is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_MODEL_APP_UNBIND, unicastAddress, call)

            implementation.unbindAppKey(unicastAddress,elementAddress,modelId,appKeyIndex)
        }
    }

    @PluginMethod
    fun subscribe(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")
        val elementAddress = call.getInt("elementAddress")
                ?: return call.reject("elementAddress is required")
        val subscriptionAddress = call.getInt("subscriptionAddress")
                ?: return call.reject("subscriptionAddress is required")
        val modelId = call.getInt("modelId")
                ?: return call.reject("modelId is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_STATUS, unicastAddress, call)

            implementation.subscribe(unicastAddress,elementAddress,subscriptionAddress,modelId)
        }
    }

    @PluginMethod
    fun unsubscribe(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")
        val elementAddress = call.getInt("elementAddress")
                ?: return call.reject("elementAddress is required")
        val subscriptionAddress = call.getInt("subscriptionAddress")
                ?: return call.reject("subscriptionAddress is required")
        val modelId = call.getInt("modelId")
                ?: return call.reject("modelId is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_STATUS, unicastAddress, call)

            implementation.unsubscribe(unicastAddress,elementAddress,subscriptionAddress,modelId)
        }
    }

    @PluginMethod
    fun unsubscribeAll(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")
        val elementAddress = call.getInt("elementAddress")
                ?: return call.reject("elementAddress is required")
        val subscriptionAddress = call.getInt("subscriptionAddress")
                ?: return call.reject("subscriptionAddress is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_STATUS, unicastAddress, call)

            implementation.unsubscribeAll(unicastAddress,elementAddress,subscriptionAddress)
        }
    }

    @PluginMethod
    fun publish(call: PluginCall){
        val unicastAddress = call.getInt("unicastAddress")
                ?: return call.reject("unicastAddress is required")
        val elementAddress = call.getInt("elementAddress")
                ?: return call.reject("elementAddress is required")
        val modelId = call.getInt("modelId")
                ?: return call.reject("modelId is required")
        val appKeyIndex = call.getInt("appKeyIndex")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addConfigPluginCall(ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_STATUS, unicastAddress, call)

            if (appKeyIndex == null){
                implementation.delPublish(unicastAddress,elementAddress,modelId)
            }else{
                val publishAddress = call.getInt("publishAddress")
                        ?: return@launch call.reject("publishAddress is required")
                val credentialFlag = call.getBoolean("credentialFlag",false)!!
                val publishTtl = call.getInt("publishTtl",0xFF)!!
                val publicationSteps = call.getInt("publicationSteps",0)!!
                val publicationResolution = call.getInt("publicationResolution",0)!!
                val retransmitCount = call.getInt("retransmitCount",1)!!
                val retransmitIntervalSteps = call.getInt("retransmitIntervalSteps",1)!!
                implementation.setPublish(unicastAddress,elementAddress,publishAddress,appKeyIndex,credentialFlag,publishTtl,publicationSteps,publicationResolution,retransmitCount,retransmitIntervalSteps,modelId)
            }
        }
    }

    @PluginMethod
    fun getOnOff(call: PluginCall){
        val elementAddress = call.getInt("elementAddress")
                ?: return call.reject("elementAddress is required")
        val appKeyIndex = call.getInt("appKeyIndex")
                ?: return call.reject("appKeyIndex is required")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            PluginCallManager.getInstance()
                    .addSigPluginCall(ApplicationMessageOpCodes.GENERIC_ON_OFF_GET, elementAddress, call)

            implementation.getOnOff(elementAddress,appKeyIndex)
        }
    }

    @PluginMethod
    fun setOnOff(call: PluginCall){
        val elementAddress = call.getInt("elementAddress")
                ?: return call.reject("elementAddress is required")
        val appKeyIndex = call.getInt("appKeyIndex")
                ?: return call.reject("appKeyIndex is required")
        val onOff = call.getBoolean("onOff",false)
        val acknowledgement = call.getBoolean("acknowledgement", true)
        val transitionSteps = call.getInt("transitionSteps")
        val transitionResolution = call.getInt("transitionResolution")
        val delay = call.getInt("delay")

        CoroutineScope(Dispatchers.Main).launch {
            if (!assertBluetoothAdapter(call)) return@launch

            val connected = connectionToProvisionedDevice()
            if (!connected) {
                return@launch call.reject("Failed to connect to Mesh proxy")
            }

            if(acknowledgement == true){
                PluginCallManager.getInstance()
                    .addSigPluginCall(ApplicationMessageOpCodes.GENERIC_ON_OFF_SET, elementAddress, call)
                implementation.setOnOffAck(elementAddress,appKeyIndex,onOff==true,transitionSteps,transitionResolution,delay)
            }else{
                val res =  implementation.setOnOff(elementAddress,appKeyIndex,onOff==true,transitionSteps,transitionResolution,delay)
                call.resolve(res)
            }
        }
    }

//    @PluginMethod
//    fun sendGenericOnOffSet(call: PluginCall) {
//        val unicastAddress = call.getInt("unicastAddress")
//        val appKeyIndex = call.getInt("appKeyIndex")
//        val onOff = call.getBoolean("onOff")
//        val acknowledgement = call.getBoolean("acknowledgement", false)
//
//        if (unicastAddress == null || appKeyIndex == null || onOff == null) {
//            call.reject("unicastAddress, appKeyIndex, and onOff are required")
//            return
//        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            val connected = connectionToProvisionedDevice()
//            if (!connected) {
//                call.reject("Failed to connect to Mesh proxy")
//                return@launch
//            }
//
//            if (acknowledgement == true) {
//                PluginCallManager.getInstance()
//                    .addSigPluginCall(ApplicationMessageOpCodes.GENERIC_ON_OFF_SET, unicastAddress, call)
//            }
//
//            val result = implementation.sendGenericOnOffSet(
//                unicastAddress,
//                appKeyIndex,
//                onOff, Random().nextInt(), 0, 0, 0,
//                acknowledgement!!
//            )
//
//            if (!result) {
//                call.reject("Failed to send Generic OnOff Set")
//            } else {
//                if (acknowledgement == false) {
//                    call.resolve()
//                }
//            }
//        }
//    }

//    @PluginMethod
//    fun sendGenericOnOffGet(call: PluginCall) {
//        val unicastAddress = call.getInt("unicastAddress")
//        val appKeyIndex = call.getInt("appKeyIndex")
//
//        if (unicastAddress == null || appKeyIndex == null) {
//            call.reject("unicastAddress and appKeyIndex are required")
//            return
//        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            val connected = connectionToProvisionedDevice()
//            if (!connected) {
//                call.reject("Failed to connect to Mesh proxy")
//                return@launch
//            }
//
//            PluginCallManager.getInstance()
//                .addSigPluginCall(ApplicationMessageOpCodes.GENERIC_ON_OFF_GET, unicastAddress, call)
//
//            val result = implementation.sendGenericOnOffGet(
//                unicastAddress,
//                appKeyIndex,
//            )
//
//            if (!result) {
//                call.reject("Failed to send Generic OnOff Get")
//            } else {
//                call.resolve()
//            }
//        }
//    }

//    @PluginMethod
//    fun sendGenericPowerLevelSet(call: PluginCall) {
//        val unicastAddress = call.getInt("unicastAddress")
//        val appKeyIndex = call.getInt("appKeyIndex")
//        val powerLevel = call.getInt("powerLevel")
//        val acknowledgement = call.getBoolean("acknowledgement", false)
//
//        if (unicastAddress == null || appKeyIndex == null || powerLevel == null) {
//            call.reject("unicastAddress, appKeyIndex, and powerLevel are required")
//            return
//        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            val connected = connectionToProvisionedDevice()
//            if (!connected) {
//                call.reject("Failed to connect to Mesh proxy")
//                return@launch
//            }
//
////            if (acknowledgement == true) {
////                PluginCallManager.getInstance()
////                    .addSigPluginCall(ApplicationMessageOpCodes.GENERIC_POWER_LEVEL_SET, unicastAddress, call)
////            }
//
//            val result = implementation.sendGenericPowerLevelSet(
//                unicastAddress,
//                appKeyIndex,
//                powerLevel,
//                0
//            )
//
//            if (!result) {
//                call.reject("Failed to send Generic Power Level Set")
//            } else {
//                if (acknowledgement == false) {
//                    call.resolve()
//                }
//            }
//        }
//    }

//    @PluginMethod
//    fun sendGenericPowerLevelGet(call: PluginCall) {
//        val unicastAddress = call.getInt("unicastAddress")
//        val appKeyIndex = call.getInt("appKeyIndex")
//
//        if (unicastAddress == null || appKeyIndex == null) {
//            call.reject("unicastAddress and appKeyIndex are required")
//            return
//        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            val connected = connectionToProvisionedDevice()
//            if (!connected) {
//                call.reject("Failed to connect to Mesh proxy")
//                return@launch
//            }
//
////            PluginCallManager.getInstance()
////                .addSigPluginCall(ApplicationMessageOpCodes.GENERIC_POWER_LEVEL_GET, unicastAddress, call)
//
//            val result = implementation.sendGenericPowerLevelGet(
//                unicastAddress,
//                appKeyIndex,
//            )
//
//            if (!result) {
//                call.reject("Failed to send Generic Power Level Get")
//            } else {
//                call.resolve()
//            }
//        }
//    }

//    @PluginMethod
//    fun sendLightHslSet(call: PluginCall) {
//        val unicastAddress = call.getInt("unicastAddress")
//        val appKeyIndex = call.getInt("appKeyIndex")
//        val hue = call.getInt("hue")
//        val saturation = call.getInt("saturation")
//        val lightness = call.getInt("lightness")
//        val acknowledgement = call.getBoolean("acknowledgement", false)
//
//        if (unicastAddress == null || appKeyIndex == null || hue == null || saturation == null || lightness == null) {
//            call.reject("unicastAddress, appKeyIndex, hue, saturation, and lightness are required")
//            return
//        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            val connected = connectionToProvisionedDevice()
//            if (!connected) {
//                call.reject("Failed to connect to Mesh proxy")
//                return@launch
//            }
//
//            if (acknowledgement == true) {
//                PluginCallManager.getInstance()
//                    .addSigPluginCall(ApplicationMessageOpCodes.LIGHT_HSL_SET, unicastAddress, call)
//            }
//
//            val result = implementation.sendLightHslSet(
//                unicastAddress,
//                appKeyIndex,
//                hue,
//                saturation,
//                lightness,
//                0
//            )
//
//            if (!result) {
//                call.reject("Failed to send Light HSL Set")
//            } else {
//                if (acknowledgement == false) {
//                    call.resolve()
//                }
//            }
//        }
//    }

//    @PluginMethod
//    fun sendLightHslGet(call: PluginCall) {
//        val unicastAddress = call.getInt("unicastAddress")
//        val appKeyIndex = call.getInt("appKeyIndex")
//
//        if (unicastAddress == null || appKeyIndex == null) {
//            call.reject("unicastAddress and appKeyIndex are required")
//            return
//        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            val connected = connectionToProvisionedDevice()
//            if (!connected) {
//                call.reject("Failed to connect to Mesh proxy")
//                return@launch
//            }
//
//            PluginCallManager.getInstance()
//                .addSigPluginCall(ApplicationMessageOpCodes.LIGHT_HSL_GET, unicastAddress, call)
//
//            val result = implementation.sendLightHslGet(
//                unicastAddress,
//                appKeyIndex,
//            )
//
//            if (!result) {
//                call.reject("Failed to send Light HSL Get")
//            } else {
//                call.resolve()
//            }
//        }
//    }

//    @PluginMethod
//    fun sendLightCtlSet(call: PluginCall) {
//        val unicastAddress = call.getInt("unicastAddress")
//        val appKeyIndex = call.getInt("appKeyIndex")
//        val lightness = call.getInt("lightness")
//        val temperature = call.getInt("temperature")
//        val deltaUv = call.getInt("deltaUv")
//        val acknowledgement = call.getBoolean("acknowledgement", false)
//
//        if (unicastAddress == null || appKeyIndex == null || lightness == null || temperature == null || deltaUv == null) {
//            call.reject("unicastAddress, appKeyIndex, lightness, temperature, and deltaUv are required")
//            return
//        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            val connected = connectionToProvisionedDevice()
//            if (!connected) {
//                call.reject("Failed to connect to Mesh proxy")
//                return@launch
//            }
//
//            if (acknowledgement == true) {
//                PluginCallManager.getInstance()
//                    .addSigPluginCall(ApplicationMessageOpCodes.LIGHT_CTL_SET, unicastAddress, call)
//            }
//
//            val result = implementation.sendLightCtlSet(
//                unicastAddress,
//                appKeyIndex,
//                lightness,
//                temperature,
//                deltaUv,
//                0
//            )
//
//            if (!result) {
//                call.reject("Failed to send Light CTL Set")
//            } else {
//                if (acknowledgement == false) {
//                    call.resolve()
//                }
//            }
//        }
//    }

//    @PluginMethod
//    fun sendVendorModelMessage(call: PluginCall) {
//        val unicastAddress = call.getInt("unicastAddress")
//        val appKeyIndex = call.getInt("appKeyIndex")
//        val modelId = call.getInt("modelId")
//        val opcode = call.getInt("opcode")
//        val payload = call.getObject("payload")
//        val opPairCode = call.getInt("opPairCode", null)
//        val companyIdentifier = modelId?.shr(16)
//
//        if (unicastAddress == null || appKeyIndex == null || modelId == null || companyIdentifier == null || opcode == null) {
//            call.reject("unicastAddress, appKeyIndex, modelId, companyIdentifier and opcode are required")
//            return
//        }
//
//        var payloadData = byteArrayOf()
//        if (payload != null) { // Convert the payload object into a ByteArray
//            payloadData = payload.keys()
//                .asSequence()
//                .mapNotNull { key -> payload.getInt(key) } // Convert each value to an Int, ignoring nulls
//                .map { it.toByte() } // Convert each Int to a Byte
//                .toList()
//                .toByteArray()
//        }
//
//        if (opPairCode != null) {
//            PluginCallManager.getInstance()
//                .addVendorPluginCall(modelId, opcode, opPairCode, unicastAddress, call)
//        }
//
//        CoroutineScope(Dispatchers.Main).launch {
//            val connected = connectionToProvisionedDevice()
//            if (!connected) {
//                call.reject("Failed to connect to Mesh proxy")
//                return@launch
//            }
//
//            val result = implementation.sendVendorModelMessage(
//                unicastAddress,
//                appKeyIndex,
//                modelId,
//                companyIdentifier,
//                opcode,
//                payloadData,
//            )
//
//            if (!result) {
//                call.reject("Failed to send Vendor Model Message")
//            }
//        }
//    }



//
//    @PluginMethod
//    fun initMeshNetwork(call: PluginCall) {
//        val networkName = call.getString("networkName")
//
//        if (networkName == null) {
//            call.reject("networkName is required")
//            return
//        }
//
//        implementation.initMeshNetwork(networkName)
//
//        val network = implementation.exportMeshNetwork()
//
//        if (network != null) {
//            call.resolve(JSObject().put("meshNetwork", network))
//        } else {
//            call.reject("Failed to initialize mesh network")
//        }
//    }

    fun sendNotification(eventName: String, data: JSObject) {
        if (!hasListeners(eventName)) {
            return
        }
        notifyListeners(eventName, data)
    }
}
