package com.lebrislo.bluetooth.mesh.plugin

import com.getcapacitor.PluginCall

class MeshPluginCall(val meshOperationCallback: Int, call: PluginCall, timeout: Int):BasePluginCall(call,timeout) {
}