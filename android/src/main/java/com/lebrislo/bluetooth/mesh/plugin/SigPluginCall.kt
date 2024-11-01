package com.lebrislo.bluetooth.mesh.plugin

import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import no.nordicsemi.android.mesh.sensorutils.DeviceProperty
import no.nordicsemi.android.mesh.sensorutils.DevicePropertyCharacteristic
import no.nordicsemi.android.mesh.sensorutils.SensorSettingAccess
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
                val pms = meshMessage.parameters

                put("propertyId", MeshParserUtils.unsignedBytesToInt(pms[0], pms[1]))
                if (pms.size <= 2) return@apply

                val periodDivisor = pms[2].toInt().and(0x7F)
                val triggerType = pms[2].toInt().and(0x80).shr(7)
                val triggerLen = if (triggerType == 0x00) ((pms.size - 4) / 4) else 2

                var offset = 3
                val triggerDeltaDown = pms.slice(IntRange(offset, offset + triggerLen - 1))
                offset += triggerLen

                val triggerDeltaUp = pms.slice(IntRange(offset, offset + triggerLen - 1))
                offset += triggerLen

                val minInterval = pms[offset++].toInt()

                val fastCadenceLow = pms.slice(IntRange(offset, offset + triggerLen - 1))
                val fastCadenceHigh = pms.slice(IntRange(offset, offset + triggerLen - 1))

                put("periodDivisor",periodDivisor)
                put("triggerType",triggerType)
                put("minInterval",minInterval)
                put("triggerDeltaDown",JSArray().apply {
                    triggerDeltaDown.forEach {
                        put(it)
                    }
                })
                put("triggerDeltaUp",JSArray().apply {
                    triggerDeltaUp.forEach {
                        put(it)
                    }
                })
                put("fastCadenceLow",JSArray().apply {
                    fastCadenceLow.forEach {
                        put(it)
                    }
                })
                put("fastCadenceHigh",JSArray().apply {
                    fastCadenceHigh.forEach {
                        put(it)
                    }
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
                val pms = meshMessage.parameters
                put("propertyId", MeshParserUtils.unsignedBytesToInt(pms[0], pms[1]))
                put("sensorSettingPropertyId", MeshParserUtils.unsignedBytesToInt(pms[2], pms[3]))
                if (pms.size > 4) {
                    put("sensorSettingAccess", pms[4].toInt().and(0xFF))
                    put("sensorSetting", JSArray().apply {
                        pms.slice(IntRange(5, pms.size - 1)).forEach {
                            put(it)
                        }
                    })
                }
            }
        }
    }
}
