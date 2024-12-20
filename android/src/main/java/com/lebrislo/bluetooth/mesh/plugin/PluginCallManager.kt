package com.lebrislo.bluetooth.mesh.plugin

import android.util.Log
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.lebrislo.bluetooth.mesh.NrfMeshPlugin
import com.lebrislo.bluetooth.mesh.NrfMeshPlugin.Companion.ADAPTER_EVENT_STRING
import com.lebrislo.bluetooth.mesh.NrfMeshPlugin.Companion.CONNECTION_EVENT_STRING
import com.lebrislo.bluetooth.mesh.NrfMeshPlugin.Companion.MODEL_EVENT_STRING
import com.lebrislo.bluetooth.mesh.NrfMeshPlugin.Companion.NODE_EVENT_STRING
import com.lebrislo.bluetooth.mesh.models.BleMeshDevice
import com.lebrislo.bluetooth.mesh.plugin.ConfigOperationPair.Companion.getConfigOperationPair
import com.lebrislo.bluetooth.mesh.plugin.ConfigPluginCall.Companion.generateConfigPluginCallResponse
import com.lebrislo.bluetooth.mesh.plugin.SigOperationPair.Companion.getSigOperationPair
import com.lebrislo.bluetooth.mesh.plugin.SigPluginCall.Companion.generateSigPluginCallResponse
import com.lebrislo.bluetooth.mesh.plugin.VendorPluginCall.Companion.generateVendorPluginCallResponse
import no.nordicsemi.android.mesh.MeshNetwork
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode
import no.nordicsemi.android.mesh.transport.MeshMessage

/**
 * This class is used to manage plugin calls.
 */
class PluginCallManager private constructor() {
    private val tag: String = PluginCallManager::class.java.simpleName

    private lateinit var plugin: NrfMeshPlugin
    private lateinit var network: MeshNetwork
    private val pluginCalls: MutableList<BasePluginCall> = mutableListOf()

    companion object {
        const val MESH_NETWORK_INIT = 0x08000000;
        const val MESH_NODE_IDENTIFY = 0x08000001;
        const val MESH_NODE_PROVISION = 0x08000002;

        @Volatile
        private var instance: PluginCallManager? = null

        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: PluginCallManager().also { instance = it }
                }
    }

    private fun clearTimeout() {
        val timeouts = pluginCalls.filter {
            it.isTimeout
        }

        timeouts.forEach {
            pluginCalls.remove(it)
        }
    }

    /**
     * Set the plugin, must be called before any other method.
     *
     * @param plugin Plugin.
     */
    fun setPlugin(plugin: NrfMeshPlugin) {
        this.plugin = plugin
    }

    fun setNetwork(network: MeshNetwork) {
        this.network = network
    }

    /**
     * Add a SIG plugin call to the list of plugin calls to watch for a response.
     *
     * @param meshOperation Mesh operation.
     * @param meshAddress Mesh address.
     * @param call Plugin call.
     */
    fun addSigPluginCall(meshOperation: Int, meshAddress: Int, call: PluginCall) {
        this.clearTimeout()
        val operationPair = getSigOperationPair(meshOperation)
        pluginCalls.add(SigPluginCall(operationPair, meshAddress, call))
    }

    /**
     * Resolve a SIG plugin call.
     * If the call is not found in the list of plugin calls, a notification is sent to the listeners.
     *
     * @param meshMessage Mesh message.
     */
    fun resolveSigPluginCall(meshMessage: MeshMessage) {
        this.clearTimeout()
        val callResponse = generateSigPluginCallResponse(meshMessage)

        val pluginCall =
                pluginCalls.find { it is SigPluginCall && it.meshOperationCallback == meshMessage.opCode && it.meshAddress == meshMessage.src }

        if (pluginCall != null) {
            pluginCall as SigPluginCall
            pluginCall.resolve(callResponse)
            pluginCalls.remove(pluginCall)
        }
        plugin.sendNotification(MODEL_EVENT_STRING, callResponse)
    }

    /**
     * Add a Config plugin call to the list of plugin calls to watch for a response.
     *
     * @param meshOperation Mesh operation.
     * @param meshAddress Mesh address.
     * @param call Plugin call.
     */
    fun addConfigPluginCall(meshOperation: Int, meshAddress: Int, call: PluginCall, timeout: Int = 10000) {
        this.clearTimeout()
        val operationPair = getConfigOperationPair(meshOperation)
        pluginCalls.add(ConfigPluginCall(operationPair, meshAddress, call, timeout))
    }

    /**
     * Resolve a Config plugin call.
     * If the call is not found in the list of plugin calls, a notification is sent to the listeners.
     *
     * @param meshMessage Mesh message.
     */
    fun resolveConfigPluginCall(meshMessage: MeshMessage) {
        this.clearTimeout()
        val callResponse = generateConfigPluginCallResponse(meshMessage)

        val pluginCall =
                pluginCalls.find { it is ConfigPluginCall && it.meshOperationCallback == meshMessage.opCode && it.meshAddress == meshMessage.src }

        if (pluginCall != null) {
            pluginCall as ConfigPluginCall
            pluginCall.resolve(callResponse)
            pluginCalls.remove(pluginCall)
        }

        plugin.sendNotification(MODEL_EVENT_STRING, callResponse)
    }

    fun addMeshPluginCall(meshOperation: Int, call: PluginCall, timeout: Int) {
        this.clearTimeout()
        pluginCalls.add(MeshPluginCall(meshOperation, call, timeout))
    }

    fun resolveMeshIndetifyPluginCall(meshNode: UnprovisionedMeshNode) {
        this.clearTimeout()
        val pluginCall = pluginCalls.find { it is MeshPluginCall && it.meshOperationCallback == MESH_NODE_IDENTIFY }

        val result = JSObject().apply {
            put("numberOfElements", meshNode.provisioningCapabilities.numberOfElements)
            val oobTypeArray = JSArray().apply {
                meshNode.provisioningCapabilities.availableOOBTypes.forEach {
                    put(it)
                }
            }
            put("availableOOBTypes", oobTypeArray)
            put("algorithms", meshNode.provisioningCapabilities.rawAlgorithm)
            put("publicKeyType", meshNode.provisioningCapabilities.rawPublicKeyType)
            put("staticOobTypes", meshNode.provisioningCapabilities.rawStaticOOBType)
            put("outputOobSize", meshNode.provisioningCapabilities.outputOOBSize)
            put("outputOobActions", meshNode.provisioningCapabilities.rawOutputOOBAction)
            put("inputOobSize", meshNode.provisioningCapabilities.inputOOBSize)
            put("inputOobActions", meshNode.provisioningCapabilities.rawInputOOBAction)
        }

        if (pluginCall == null) return

        pluginCall as MeshPluginCall
        pluginCall.resolve(result)
        pluginCalls.remove(pluginCall)
    }

    fun resolveMeshProvisionPluginCall(meshDevice: BleMeshDevice) {
        this.clearTimeout()
        val pluginCall = pluginCalls.find { it is MeshPluginCall && it.meshOperationCallback == MESH_NODE_PROVISION }

        val result = JSObject().apply {
            when (meshDevice) {
                is BleMeshDevice.Provisioned -> {
                    put("provisioningComplete", true)
                    put("uuid", meshDevice.node.uuid)
                    put("unicastAddress", meshDevice.node.unicastAddress)
                }

                is BleMeshDevice.Unprovisioned -> {
                    put("provisioningComplete", false)
                    put("uuid", meshDevice.node.deviceUuid)
                }
            }
        }

        if (pluginCall == null) return

        pluginCall as MeshPluginCall
        pluginCall.resolve(result)
        pluginCalls.remove(pluginCall)
    }

    fun resolveMeshNetworkInitPluginCall() {
        this.clearTimeout()
        val pluginCall = pluginCalls.find { it is MeshPluginCall && it.meshOperationCallback == MESH_NETWORK_INIT }

        if (pluginCall == null) return

        pluginCall as MeshPluginCall
        pluginCall.resolve(JSObject())
        pluginCalls.remove(pluginCall)
    }

    fun notifyNodeDelete(unicastAddress: Int) {
        if (plugin == null) return

        plugin.sendNotification(NODE_EVENT_STRING, JSObject().apply {
            put("action", "delete")
            put("unicastAddress", unicastAddress)
        })
    }

    fun notifyAdapter(enabled: Boolean) {
        if (enabled) plugin.startScan()
        if (!enabled) plugin.stopScan()

        if (plugin == null) return

        plugin.sendNotification(ADAPTER_EVENT_STRING, JSObject().apply {
            put("enabled", enabled)
        })
    }

    fun notifyConnection(connected: Boolean) {
        if (plugin == null) return

        plugin.sendNotification(CONNECTION_EVENT_STRING, JSObject().apply {
            put("connected", connected)
        })
    }

    /**
     * Add a Vendor plugin call to the list of plugin calls to watch for a response.
     *
     * @param modelId Model ID.
     * @param opCode Operation code sent.
     * @param opPairCode Operation code to watch for.
     * @param meshAddress Mesh address.
     * @param call Plugin call.
     */
    fun addVendorPluginCall(modelId: Int, opCode: Int, opPairCode: Int, meshAddress: Int, call: PluginCall) {
        this.clearTimeout()
        pluginCalls.add(VendorPluginCall(modelId, opCode, opPairCode, meshAddress, call))
    }

    /**
     * Resolve a Vendor plugin call.
     * If the call is not found in the list of plugin calls, a notification is sent to the listeners.
     *
     * @param meshMessage Mesh message.
     */
    fun resolveVendorPluginCall(meshMessage: MeshMessage) {
        this.clearTimeout()
        Log.d(tag, "resolveVendorPluginCall ${meshMessage.opCode} from ${meshMessage.src}")
        val callResponse = generateVendorPluginCallResponse(meshMessage)

        val pluginCall =
                pluginCalls.find { it is VendorPluginCall && it.meshOperationCallback == meshMessage.opCode && it.meshAddress == meshMessage.src }

        if (pluginCall != null) {
            pluginCall as VendorPluginCall
            pluginCall.resolve(callResponse)
            pluginCalls.remove(pluginCall)
        }

        plugin.sendNotification(MODEL_EVENT_STRING, callResponse)
    }
}