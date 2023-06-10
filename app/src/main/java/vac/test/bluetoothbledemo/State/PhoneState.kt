package vac.test.bluetoothbledemo.State

import vac.test.bluetoothbledemo.bean.CPhone

sealed class PhoneState {

    object Idle : PhoneState()
    object Loading : PhoneState()
    data class Phone(val device:CPhone) : PhoneState()
    data class Error(val error: String?) : PhoneState()
}