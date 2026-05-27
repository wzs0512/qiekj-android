package com.example.devicecontrol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.devicecontrol.data.AppRepository
import com.example.devicecontrol.data.BalanceData
import com.example.devicecontrol.data.DeviceItem
import com.example.devicecontrol.data.UnlockResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DeviceTab { Control, Me }

data class AppUiState(
    val currentTab: DeviceTab = DeviceTab.Control,
    val hasToken: Boolean = false,
    val phone: String = "",
    val code: String = "",
    val sendingCode: Boolean = false,
    val loggingIn: Boolean = false,
    val loadingDevices: Boolean = false,
    val loadingBalance: Boolean = false,
    val unlocking: Boolean = false,
    val devices: List<DeviceItem> = emptyList(),
    val balance: BalanceData? = null,
    val unlockStatus: String? = null,
    val orderDetail: UnlockResult? = null,
    val toastMessage: String? = null,
    val errorMessage: String? = null,
)

class AppViewModel(
    private val repository: AppRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState(hasToken = repository.localToken() != null))
    val state: StateFlow<AppUiState> = _state

    init {
        if (repository.localToken() != null) {
            refreshDevices()
            refreshBalance()
        }
    }

    fun selectTab(tab: DeviceTab) {
        _state.update { it.copy(currentTab = tab) }
        if (tab == DeviceTab.Control && state.value.hasToken && state.value.devices.isEmpty()) {
            refreshDevices()
        }
    }

    fun updatePhone(value: String) {
        _state.update { it.copy(phone = value) }
    }

    fun updateCode(value: String) {
        _state.update { it.copy(code = value) }
    }

    fun sendCode() = viewModelScope.launch {
        val phone = state.value.phone.trim()
        if (phone.isBlank()) {
            showError("请输入手机号")
            return@launch
        }
        runCatching {
            _state.update { it.copy(sendingCode = true) }
            repository.sendCode(phone)
        }.onSuccess {
            showToast("验证码已发送")
        }.onFailure {
            showError(it.message ?: "验证码发送失败")
        }
        _state.update { it.copy(sendingCode = false) }
    }

    fun login() = viewModelScope.launch {
        val phone = state.value.phone.trim()
        val code = state.value.code.trim()
        if (phone.isBlank() || code.isBlank()) {
            showError("请输入手机号和验证码")
            return@launch
        }
        runCatching {
            _state.update { it.copy(loggingIn = true) }
            repository.login(phone, code)
        }.onSuccess {
            _state.update { it.copy(hasToken = true, loggingIn = false) }
            showToast("登录成功")
            refreshBalance()
            refreshDevices()
        }.onFailure {
            _state.update { it.copy(loggingIn = false) }
            showError(it.message ?: "登录失败")
        }
    }

    fun refreshDevices() = viewModelScope.launch {
        if (!state.value.hasToken) return@launch
        runCatching {
            _state.update { it.copy(loadingDevices = true) }
            repository.latestDevices()
        }.onSuccess { devices ->
            _state.update { it.copy(devices = devices, loadingDevices = false) }
        }.onFailure {
            _state.update { it.copy(loadingDevices = false) }
            showError(it.message ?: "查询历史设备失败")
        }
    }

    fun refreshBalance() = viewModelScope.launch {
        if (!state.value.hasToken) return@launch
        runCatching {
            _state.update { it.copy(loadingBalance = true) }
            repository.queryBalance()
        }.onSuccess { balance ->
            _state.update { it.copy(balance = balance, loadingBalance = false) }
        }.onFailure {
            _state.update { it.copy(loadingBalance = false) }
            showError(it.message ?: "查询资产失败")
        }
    }

    fun unlock(device: DeviceItem) = viewModelScope.launch {
        if (state.value.unlocking) return@launch
        runCatching {
            _state.update { it.copy(unlocking = true, unlockStatus = "准备解锁") }
            repository.unlockDevice(device) { step ->
                _state.update { it.copy(unlockStatus = step) }
            }
        }.onSuccess { result ->
            _state.update { it.copy(unlocking = false, unlockStatus = null, orderDetail = result) }
            showToast("解锁成功！订单原价：${result.originPrice}，花费小票：${result.ticketCost}")
            refreshBalance()
        }.onFailure {
            _state.update { it.copy(unlocking = false, unlockStatus = null) }
            showError(it.message ?: "解锁失败")
        }
    }

    fun consumeToast() {
        _state.update { it.copy(toastMessage = null) }
    }

    fun consumeError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun dismissOrderDetail() {
        _state.update { it.copy(orderDetail = null) }
    }

    private fun showToast(message: String) {
        _state.update { it.copy(toastMessage = message) }
    }

    private fun showError(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }
}

class AppViewModelFactory(
    private val repository: AppRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppViewModel(repository) as T
    }
}
