package com.lebrislo.bluetooth.mesh.plugin

import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import no.nordicsemi.android.mesh.transport.ConfigAppKeyStatus
import no.nordicsemi.android.mesh.transport.ConfigCompositionDataStatus
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlStatus
import no.nordicsemi.android.mesh.transport.ConfigModelAppStatus
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitStatus
import no.nordicsemi.android.mesh.transport.ConfigNodeResetStatus
import no.nordicsemi.android.mesh.transport.MeshMessage
import no.nordicsemi.android.mesh.utils.CompanyIdentifiers
import no.nordicsemi.android.mesh.utils.CompositionDataParser

/**
 * This class is used to generate a response for a Config plugin call.
 */
class ConfigPluginCall(val meshOperationCallback: Int, val meshAddress: Int, call: PluginCall) : BasePluginCall(call) {
    companion object {
        /**
         * Generates a response for a Config plugin call.
         *
         * @param meshMessage Mesh message.
         */
        @JvmStatic
        fun generateConfigPluginCallResponse(meshMessage: MeshMessage): JSObject {
            return JSObject().apply {
                put("src", meshMessage.src)
                put("dst", meshMessage.dst)
                put("opcode", meshMessage.opCode)
                put("data", when (meshMessage) {
                    is ConfigNodeResetStatus -> configNodeResetStatusResponse(meshMessage)
                    is ConfigCompositionDataStatus -> configCompositionDataStatusResponse(meshMessage)
                    is ConfigDefaultTtlStatus -> configDefaultTtlStatusResponse(meshMessage)
                    is ConfigNetworkTransmitStatus -> configNetworkTransmitStatusResponse(meshMessage)
                    is ConfigAppKeyStatus -> configAppKeyStatusResponse(meshMessage)
                    is ConfigModelAppStatus -> configModelAppStatusResponse(meshMessage)
                    else -> JSObject()
                    }
                )
            }
        }

        private fun configNodeResetStatusResponse(meshMessage: ConfigNodeResetStatus): JSObject {
            return JSObject().apply {
                put("status", meshMessage.statusCode)
                put("statusName",meshMessage.statusCodeName)
            }
        }

        private fun configCompositionDataStatusResponse(meshMessage: ConfigCompositionDataStatus):JSObject{
            return JSObject().apply {
                put("status", meshMessage.statusCode)
                put("statusName", meshMessage.statusCodeName)
                put("companyIdentifier", CompanyIdentifiers.getCompanyName(meshMessage.companyIdentifier.toShort()))
                put("productIdentifier", CompositionDataParser.formatProductIdentifier(meshMessage.productIdentifier,false))
                put("productVersion",CompositionDataParser.formatVersionIdentifier(meshMessage.versionIdentifier,false))
                put("nodeFeaturesSupported", JSObject().apply {
                    put("relay", meshMessage.isRelayFeatureSupported)
                    put("proxy", meshMessage.isProxyFeatureSupported)
                    put("friend", meshMessage.isFriendFeatureSupported)
                    put("lowPower", meshMessage.isLowPowerFeatureSupported)
                })
                put("elements",JSArray().apply {
                    meshMessage.elements.values.forEach{
                        put(JSObject().apply {
                            put("name",it.name)
                            put("elementAddress",it.elementAddress)
                            put("sigModelCount",it.sigModelCount)
                            put("vendorModelCount",it.vendorModelCount)
                            put("locationDescriptor",it.locationDescriptor)
                            put("models",JSArray().apply {
                                it.meshModels.values.forEach {
                                    put(JSObject().apply {
                                        put("modelId",it.modelId)
                                        put("modelName",it.modelName)
                                        put("boundAppKeyIndexes",it.boundAppKeyIndexes)
//                                        put("subscribedAddresses",it.subscribedAddresses)
//                                        put("",it.publicationSettings.)
                                    })
                                }
                            })
                        })
                    }
                })
            }
        }

        private fun configDefaultTtlStatusResponse(meshMessage: ConfigDefaultTtlStatus):JSObject{
            return  JSObject().apply {
                put("status", meshMessage.statusCode)
                put("statusName", meshMessage.statusCodeName)
                put("ttl",meshMessage.ttl)
            }
        }

        private fun configNetworkTransmitStatusResponse(meshMessage: ConfigNetworkTransmitStatus):JSObject{
            return  JSObject().apply {
                put("status", meshMessage.statusCode)
                put("statusName", meshMessage.statusCodeName)
                put("networkTransmitCount",meshMessage.networkTransmitCount)
                put("networkTransmitIntervalSteps",meshMessage.networkTransmitIntervalSteps)
            }
        }

        private fun configAppKeyStatusResponse(meshMessage: ConfigAppKeyStatus): JSObject {
            val data = JSObject()
            data.put("status", meshMessage.statusCode)
            data.put("netKeyIndex", meshMessage.netKeyIndex)
            data.put("appKeyIndex", meshMessage.appKeyIndex)
            return data
        }

        private fun configModelAppStatusResponse(meshMessage: ConfigModelAppStatus): JSObject {
            val data = JSObject()
            data.put("status", meshMessage.statusCode)
            data.put("elementAddress", meshMessage.elementAddress)
            data.put("modelId", meshMessage.modelIdentifier)
            data.put("appKeyIndex", meshMessage.appKeyIndex)
            return data
        }
    }
}
