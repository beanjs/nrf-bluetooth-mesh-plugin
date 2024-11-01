package com.lebrislo.bluetooth.mesh

import android.util.Log
import com.lebrislo.bluetooth.mesh.models.BleMeshDevice
import com.lebrislo.bluetooth.mesh.plugin.PluginCallManager
import no.nordicsemi.android.mesh.MeshProvisioningStatusCallbacks
import no.nordicsemi.android.mesh.provisionerstates.ProvisioningState
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode
import no.nordicsemi.android.mesh.utils.MeshParserUtils

class MeshProvisioningCallbacksManager(
        var nrfMeshManager: NrfMeshManager
) :
        MeshProvisioningStatusCallbacks {
    private val tag: String = MeshProvisioningCallbacksManager::class.java.simpleName
    val unprovisionedMeshNodes: ArrayList<UnprovisionedMeshNode> = ArrayList()

    private fun findUnode(unode: UnprovisionedMeshNode,pnode: ProvisionedMeshNode):Boolean{
        if (unode.deviceKey == null) return false

        val ukey = MeshParserUtils.bytesToHex(unode.deviceKey, false)
        val pkey = MeshParserUtils.bytesToHex(pnode.deviceKey, false)

        return ukey == pkey
    }

    override fun onProvisioningStateChanged(
            meshNode: UnprovisionedMeshNode?,
            state: ProvisioningState.States?,
            data: ByteArray?
    ) {
        Log.d(tag, "onProvisioningStateChanged : ${meshNode?.deviceUuid}  ${state?.name}")
        if (state == ProvisioningState.States.PROVISIONING_CAPABILITIES) {
            unprovisionedMeshNodes.add(meshNode!!)
            PluginCallManager.getInstance().resolveMeshIndetifyPluginCall(meshNode)
        }
    }

    override fun onProvisioningFailed(
            meshNode: UnprovisionedMeshNode?,
            state: ProvisioningState.States?,
            data: ByteArray?
    ) {
        Log.d(tag, "onProvisioningFailed : " + meshNode?.deviceUuid)
        if (state == ProvisioningState.States.PROVISIONING_FAILED) {
            unprovisionedMeshNodes.remove(meshNode!!)
            PluginCallManager.getInstance().resolveMeshProvisionPluginCall(BleMeshDevice.Unprovisioned(meshNode!!))
        }
    }

    override fun onProvisioningCompleted(
            meshNode: ProvisionedMeshNode?,
            state: ProvisioningState.States?,
            data: ByteArray?
    ) {
        Log.d(tag, "onProvisioningCompleted : " + meshNode?.uuid)
        if (state == ProvisioningState.States.PROVISIONING_COMPLETE) {
            nrfMeshManager.disconnectBle().enqueue()
            val unode = unprovisionedMeshNodes.find {
                findUnode(it,meshNode!!)
            }
            if (unode != null) unprovisionedMeshNodes.remove(unode)
            PluginCallManager.getInstance().resolveMeshProvisionPluginCall(BleMeshDevice.Provisioned(meshNode!!))
        }
    }
}