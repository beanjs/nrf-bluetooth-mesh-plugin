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
import no.nordicsemi.android.mesh.transport.SensorSettingStatus
import no.nordicsemi.android.mesh.transport.SensorSettingsStatus
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
                    is SensorSettingsStatus -> sensorSettingsStatusResponse(meshMessage)
                    is SensorSettingStatus -> sensorSettingStatusResponse(meshMessage)
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
                if (meshMessage.result is SensorDescriptorStatus.SensorDescriptors) {
                    val result = meshMessage.result as SensorDescriptorStatus.SensorDescriptors
                    result.descriptors.forEach {
                        put(JSObject().apply {
                            put("propertyId", it.property.propertyId)
                            put("positiveTolerance", it.positiveTolerance)
                            put("negativeTolerance", it.negativeTolerance)
                            put("samplingFunction", it.sensorSamplingFunction.ordinal)
                            put("measurementPeriod", it.measurementPeriod)
                            put("updateInterval", it.updateInterval)
                        })
                    }
                }
            }

        }

        private fun sensorColumnStatusResponse(meshMessage: SensorColumnStatus): JSObject {
            return JSObject().apply {
                put("propertyId", meshMessage.propertyId.propertyId)
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
                put("propertyId", meshMessage.propertyId.propertyId)
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
                put("delta", JSObject().apply {
                    put("down", down.value)
                    put("up", up.value)
                })
            }
        }

        private fun sensorSettingsStatusResponse(meshMessage: SensorSettingsStatus): JSObject {
            return JSObject().apply {
                put("propertyId", meshMessage.propertyId.propertyId)
                put("settings", JSArray().apply {
                    meshMessage.sensorSettingPropertyIds.forEach {
                        put(it.propertyId)
                    }
                })
            }
        }

        private fun sensorSettingStatusResponse(meshMessage: SensorSettingStatus): JSObject {
            return JSObject().apply {
                put("propertyId", meshMessage.propertyId.propertyId)
                put("sensorSettingPropertyId", meshMessage.sensorSettingPropertyId.propertyId)

                if (meshMessage.sensorSettingAccess != null) {
                    put("sensorSettingAccess", meshMessage.sensorSettingAccess.ordinal)
                }

                if (meshMessage.sensorSetting != null) {
                    put("sensorSetting", meshMessage.sensorSetting.value)
                }
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
