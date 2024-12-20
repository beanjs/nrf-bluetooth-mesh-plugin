package com.lebrislo.bluetooth.mesh

import android.util.Log
import com.lebrislo.bluetooth.mesh.plugin.PluginCallManager
import no.nordicsemi.android.mesh.MeshStatusCallbacks
import no.nordicsemi.android.mesh.transport.ConfigAppKeyList
import no.nordicsemi.android.mesh.transport.ConfigAppKeyStatus
import no.nordicsemi.android.mesh.transport.ConfigCompositionDataStatus
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlGet
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlStatus
import no.nordicsemi.android.mesh.transport.ConfigModelAppStatus
import no.nordicsemi.android.mesh.transport.ConfigModelPublicationStatus
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionStatus
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitSet
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitStatus
import no.nordicsemi.android.mesh.transport.ConfigNodeResetStatus
import no.nordicsemi.android.mesh.transport.ControlMessage
import no.nordicsemi.android.mesh.transport.GenericOnOffStatus
import no.nordicsemi.android.mesh.transport.LightCtlStatus
import no.nordicsemi.android.mesh.transport.LightHslStatus
import no.nordicsemi.android.mesh.transport.MeshMessage
import no.nordicsemi.android.mesh.transport.SensorCadenceStatus
import no.nordicsemi.android.mesh.transport.SensorColumnStatus
import no.nordicsemi.android.mesh.transport.SensorDescriptorStatus
import no.nordicsemi.android.mesh.transport.SensorSeriesStatus
import no.nordicsemi.android.mesh.transport.SensorSettingStatus
import no.nordicsemi.android.mesh.transport.SensorSettingsStatus
import no.nordicsemi.android.mesh.transport.SensorStatus
import no.nordicsemi.android.mesh.transport.VendorModelMessageStatus

class MeshStatusCallbacksManager(var nrfMeshManager: NrfMeshManager) : MeshStatusCallbacks {
    private val tag: String = MeshStatusCallbacksManager::class.java.simpleName

    override fun onTransactionFailed(dst: Int, hasIncompleteTimerExpired: Boolean) {

    }

    override fun onUnknownPduReceived(src: Int, accessPayload: ByteArray?) {

    }

    override fun onBlockAcknowledgementProcessed(dst: Int, message: ControlMessage) {
        Log.d(tag, "onBlockAcknowledgementProcessed")
    }

    override fun onBlockAcknowledgementReceived(src: Int, message: ControlMessage) {
        Log.d(tag, "onBlockAcknowledgementReceived")
    }

    override fun onMeshMessageProcessed(dst: Int, meshMessage: MeshMessage) {
        Log.d(tag, "onMeshMessageProcessed")
    }

    override fun onMeshMessageReceived(src: Int, meshMessage: MeshMessage) {
        Log.d(tag, "onMeshMessageReceived ${meshMessage.javaClass.simpleName}")
        when (meshMessage) {
            is ConfigNodeResetStatus,
            is ConfigModelAppStatus,
            is ConfigAppKeyStatus,
            is ConfigCompositionDataStatus,
            is ConfigDefaultTtlStatus,
            is ConfigNetworkTransmitStatus,
            is ConfigModelSubscriptionStatus,
            is ConfigModelPublicationStatus,
            is ConfigAppKeyList -> PluginCallManager.getInstance().resolveConfigPluginCall(meshMessage)

            is GenericOnOffStatus,
            is SensorStatus,
            is SensorDescriptorStatus,
            is SensorColumnStatus,
            is SensorSeriesStatus,
            is SensorCadenceStatus,
            is SensorSettingsStatus,
            is SensorSettingStatus -> PluginCallManager.getInstance().resolveSigPluginCall(meshMessage)
        }
    }

    override fun onMessageDecryptionFailed(meshLayer: String?, errorMessage: String?) {

    }
}
