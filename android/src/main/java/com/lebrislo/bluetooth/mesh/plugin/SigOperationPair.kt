package com.lebrislo.bluetooth.mesh.plugin

import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_LEVEL_GET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_LEVEL_SET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_LEVEL_SET_UNACKNOWLEDGED
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_ON_OFF_GET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_ON_OFF_SET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_ON_OFF_SET_UNACKNOWLEDGED
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_ON_OFF_STATUS
//import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_POWER_LEVEL_GET
//import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_POWER_LEVEL_SET
//import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_POWER_LEVEL_SET_UNACKNOWLEDGED
//import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_POWER_LEVEL_STATUS
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.LIGHT_CTL_GET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.LIGHT_CTL_SET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.LIGHT_CTL_SET_UNACKNOWLEDGED
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.LIGHT_CTL_STATUS
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.LIGHT_HSL_GET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.LIGHT_HSL_SET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.LIGHT_HSL_SET_UNACKNOWLEDGED
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.LIGHT_HSL_STATUS
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_CADENCE_GET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_CADENCE_STATUS
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_COLUMN_GET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_COLUMN_STATUS
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_DESCRIPTOR_GET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_DESCRIPTOR_STATUS
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_GET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_SERIES_GET
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_SERIES_STATUS
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SENSOR_STATUS

/**
 * This class is used to get the SIG operation pair.
 */
class SigOperationPair {
    companion object {
        /**
         * Returns the status operation code for a given operation code.
         *
         * @param operationCode Operation code.
         */
        fun getSigOperationPair(operationCode: Int): Int {
            return when (operationCode) {
                GENERIC_ON_OFF_GET, GENERIC_ON_OFF_SET -> GENERIC_ON_OFF_STATUS
                SENSOR_GET -> SENSOR_STATUS
                SENSOR_DESCRIPTOR_GET -> SENSOR_DESCRIPTOR_STATUS
                SENSOR_COLUMN_GET -> SENSOR_COLUMN_STATUS
                SENSOR_SERIES_GET -> SENSOR_SERIES_STATUS
                SENSOR_CADENCE_GET -> SENSOR_CADENCE_STATUS
//                GENERIC_LEVEL_GET, GENERIC_LEVEL_SET, GENERIC_LEVEL_SET_UNACKNOWLEDGED -> GENERIC_POWER_LEVEL_STATUS
//                GENERIC_POWER_LEVEL_GET, GENERIC_POWER_LEVEL_SET, GENERIC_POWER_LEVEL_SET_UNACKNOWLEDGED -> GENERIC_POWER_LEVEL_STATUS
//                LIGHT_HSL_GET, LIGHT_HSL_SET, LIGHT_HSL_SET_UNACKNOWLEDGED -> LIGHT_HSL_STATUS
//                LIGHT_CTL_GET, LIGHT_CTL_SET, LIGHT_CTL_SET_UNACKNOWLEDGED -> LIGHT_CTL_STATUS
                else -> 0
            }
        }
    }
}