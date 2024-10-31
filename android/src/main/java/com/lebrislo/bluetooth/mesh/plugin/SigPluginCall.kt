package com.lebrislo.bluetooth.mesh.plugin

import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import no.nordicsemi.android.mesh.sensorutils.DevicePropertyCharacteristic
import no.nordicsemi.android.mesh.transport.GenericOnOffStatus
import no.nordicsemi.android.mesh.transport.MeshMessage
import no.nordicsemi.android.mesh.transport.SensorCadenceStatus
import no.nordicsemi.android.mesh.transport.SensorColumnStatus
import no.nordicsemi.android.mesh.transport.SensorDescriptorStatus
import no.nordicsemi.android.mesh.transport.SensorSeriesStatus
import no.nordicsemi.android.mesh.transport.SensorStatus
import no.nordicsemi.android.mesh.utils.MeshParserUtils
import kotlin.experimental.and


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
            return JSObject().apply {
                put("src", meshMessage.src)
                put("dst", meshMessage.dst)
                put("opcode", meshMessage.opCode)
                put("data", when (meshMessage) {
                    is GenericOnOffStatus -> genericOnOffStatusResponse(meshMessage)
                    is SensorStatus -> sensorStatusResponse(meshMessage)
                    is SensorDescriptorStatus -> sensorDescriptorStatusResponse(meshMessage)
                    is SensorColumnStatus -> sensorColumnStatusResponse(meshMessage)
                    is SensorSeriesStatus -> sensorSeriesStatusResponse(meshMessage)
                    is SensorCadenceStatus -> sensorCadenceStatusResponse(meshMessage)
                    else -> JSObject()
                })
            }
        }

        private fun genericOnOffStatusResponse(meshMessage: GenericOnOffStatus): JSObject {
            return JSObject().apply {
                put("onOff", meshMessage.presentState)
            }
        }

        private fun sensorStatusResponse(meshMessage: SensorStatus): JSArray {
            return JSArray().apply {
                meshMessage.parameters.forEach {
                    put(it)
                }
            }
        }

        private fun sensorDescriptorStatusResponse(meshMessage: SensorDescriptorStatus): JSArray {
            return JSArray().apply {
                val pms = meshMessage.parameters
                var offset = 0

                while (offset < pms.count()) {
                    val property = MeshParserUtils.unsignedBytesToInt(pms[offset], pms[offset + 1])
                    val positiveTolerance = MeshParserUtils.bytesToInt(byteArrayOf(pms[offset + 3].toInt().and(0x0F).toByte(), pms[offset + 2]))
                    val negativeTolerance = MeshParserUtils.bytesToInt(byteArrayOf(
                            pms[offset + 4].toInt().and(0xF0).shr(4).toByte(),
                            pms[offset + 4].toInt().and(0x0F).shl(4).or(pms[offset + 3].toInt().and(0xF0).shr(4)).toByte()))
                    val samplingFunction = pms[offset + 5]
                    val measurementPeriod = pms[offset + 6]
                    val updateInterval = pms[offset + 7]

                    offset += 8

                    put(JSObject().apply {
                        put("propertyId", property)
                        put("positiveTolerance", positiveTolerance)
                        put("negativeTolerance", negativeTolerance)
                        put("samplingFunction", samplingFunction)
                        put("measurementPeriod", measurementPeriod)
                        put("updateInterval", updateInterval)
                    })
                }
            }
        }

        private fun sensorColumnStatusResponse(meshMessage: SensorColumnStatus): JSObject {
            return JSObject().apply {
                put("propertyId", meshMessage.propertyId)
                put("columns", JSArray().apply {
                    if (meshMessage.result != null) {
                        meshMessage.result.forEach {
                            put(it)
                        }
                    }
                })
            }
        }

        private fun sensorSeriesStatusResponse(meshMessage: SensorSeriesStatus): JSObject {
            return JSObject().apply {
                put("propertyId", meshMessage.propertyId)
                put("series", JSArray().apply {
                    if (meshMessage.seriesRawX1X2 != null) {
                        meshMessage.seriesRawX1X2.forEach {
                            put(it)
                        }
                    }
                })
            }
        }

        private fun sensorCadenceStatusResponse(meshMessage: SensorCadenceStatus): JSObject {
            return JSObject().apply {
                val cadence = meshMessage.cadence
                put("propertyId", cadence.deviceProperty.propertyId)
                put("periodDivisor", cadence.periodDivisor)
                put("statusMinInterval", cadence.statusMinInterval)
                cadence.triggerType?.let { put("triggerType", it.ordinal) }
                cadence.fastCadenceLow?.let { put("fastCadenceLow", it.value) }
                cadence.fastCadenceHigh?.let { put("fastCadenceHigh", it.value) }

                val delta = cadence.delta ?: return@apply
                val down = delta.down as DevicePropertyCharacteristic<*>
                val up = delta.up as DevicePropertyCharacteristic<*>
                put("delta",JSObject().apply {
                    put("down",down.value)
                    put("up",up.value)
                })
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
