package com.lebrislo.bluetooth.mesh.plugin

import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_APPKEY_ADD
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_APPKEY_DELETE
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_APPKEY_GET
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_APPKEY_LIST
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_APPKEY_STATUS
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_APPKEY_UPDATE
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_GET
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_STATUS
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_DEFAULT_TTL_GET
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_DEFAULT_TTL_SET
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_DEFAULT_TTL_STATUS
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_NODE_RESET
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_NODE_RESET_STATUS
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_APP_BIND
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_APP_STATUS
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_APP_UNBIND
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_GET
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_SET
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_STATUS
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_ADD
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_DELETE
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_DELETE_ALL
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_STATUS
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_NETWORK_TRANSMIT_GET
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_NETWORK_TRANSMIT_SET
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_NETWORK_TRANSMIT_STATUS

/**
 * This class is used to retrieve the status operation code for a given operation code.
 */
class ConfigOperationPair {
    companion object {
        /**
         * Returns the status operation code for a given operation code.
         *
         * @param operationCode Operation code.
         */
        fun getConfigOperationPair(operationCode: Int): Int {
            return when (operationCode) {
                CONFIG_APPKEY_ADD.toInt(), CONFIG_APPKEY_UPDATE, CONFIG_APPKEY_DELETE -> CONFIG_APPKEY_STATUS
                CONFIG_DEFAULT_TTL_GET, CONFIG_DEFAULT_TTL_SET -> CONFIG_DEFAULT_TTL_STATUS
                CONFIG_NETWORK_TRANSMIT_GET, CONFIG_NETWORK_TRANSMIT_SET -> CONFIG_NETWORK_TRANSMIT_STATUS
                CONFIG_COMPOSITION_DATA_GET -> CONFIG_COMPOSITION_DATA_STATUS.toInt()
                CONFIG_NODE_RESET -> CONFIG_NODE_RESET_STATUS
                CONFIG_APPKEY_GET -> CONFIG_APPKEY_LIST
                CONFIG_MODEL_APP_BIND, CONFIG_MODEL_APP_UNBIND -> CONFIG_MODEL_APP_STATUS
                CONFIG_MODEL_SUBSCRIPTION_ADD, CONFIG_MODEL_SUBSCRIPTION_DELETE, CONFIG_MODEL_SUBSCRIPTION_DELETE_ALL-> CONFIG_MODEL_SUBSCRIPTION_STATUS
                CONFIG_MODEL_PUBLICATION_SET.toInt(), CONFIG_MODEL_PUBLICATION_GET->CONFIG_MODEL_PUBLICATION_STATUS
                else -> 0
            }
        }
    }
}