package com.lebrislo.bluetooth.mesh.plugin

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import no.nordicsemi.android.mesh.transport.GenericLevelStatus
import no.nordicsemi.android.mesh.transport.GenericOnOffStatus
//import no.nordicsemi.android.mesh.transport.GenericPowerLevelStatus
import no.nordicsemi.android.mesh.transport.LightCtlStatus
import no.nordicsemi.android.mesh.transport.LightHslStatus
import no.nordicsemi.android.mesh.transport.MeshMessage

/**
 * This class is used to generate a response for a SIG plugin call.
 */
class SigPluginCall(val meshOperationCallback: Int, val meshAddress: Int, call: PluginCall) : BasePluginCall(call) {
    companion object {
        /**
         * Generates a response for a SIG plugin call.
         *
         * @param meshMessage Mesh message.
         */
        @JvmStatic
        fun generateSigPluginCallResponse(meshMessage: MeshMessage): JSObject {
           return  JSObject().apply {
               put("src", meshMessage.src)
               put("dst", meshMessage.dst)
               put("opcode", meshMessage.opCode)
               put("data", when (meshMessage) {
                   is GenericOnOffStatus -> genericOnOffStatusResponse(meshMessage)
//                    is GenericLevelStatus -> genericLevelStatusResponse(meshMessage)
//                    is GenericPowerLevelStatus -> genericPowerLevelStatusResponse(meshMessage)
//                    is LightHslStatus -> lightHslStatusResponse(meshMessage)
//                    is LightCtlStatus -> lightCtlStatusResponse(meshMessage)
                   else -> JSObject()
               })
           }
        }

        private fun genericOnOffStatusResponse(meshMessage: GenericOnOffStatus): JSObject {
            return JSObject().apply {
                put("onOff",meshMessage.presentState)
            }
        }
//
//        private fun genericLevelStatusResponse(meshMessage: GenericLevelStatus): JSObject {
//            val data = JSObject()
//            data.put("level", meshMessage.presentLevel)
//            return data
//        }

//        private fun genericPowerLevelStatusResponse(meshMessage: GenericPowerLevelStatus): JSObject {
//            val data = JSObject()
//            data.put("powerLevel", meshMessage.presentLevel.toUShort().toInt())
//            return data
//        }

//        private fun lightHslStatusResponse(meshMessage: LightHslStatus): JSObject {
//            val data = JSObject()
//            data.put("hue", meshMessage.presentHue.toUShort().toInt())
//            data.put("saturation", meshMessage.presentSaturation.toUShort().toInt())
//            data.put("lightness", meshMessage.presentLightness.toUShort().toInt())
//            return data
//        }
//
//        private fun lightCtlStatusResponse(meshMessage: LightCtlStatus): JSObject {
//            val data = JSObject()
//            data.put("lightness", meshMessage.presentLightness.toUShort().toInt())
//            data.put("temperature", meshMessage.presentTemperature.toUShort().toInt())
//            return data
//        }
    }
}
