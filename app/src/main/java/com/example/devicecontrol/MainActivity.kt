package com.example.devicecontrol

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.devicecontrol.data.AppRepository
import com.example.devicecontrol.data.TokenStore
import com.example.devicecontrol.ui.AppViewModel
import com.example.devicecontrol.ui.AppViewModelFactory
import com.example.devicecontrol.ui.DeviceTab
import com.example.devicecontrol.ui.theme.DeviceControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeviceControlTheme {
                val repository = remember {
                    AppRepository(TokenStore(applicationContext))
                }
                val vm: AppViewModel = viewModel(
                    factory = AppViewModelFactory(repository),
                )
                DeviceControlApp(vm)
            }
        }
    }
}

@Composable
private fun DeviceControlApp(vm: AppViewModel) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.toastMessage) {
        val message = state.toastMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        vm.consumeToast()
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        vm.consumeError()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = state.currentTab == DeviceTab.Control,
                    onClick = { vm.selectTab(DeviceTab.Control) },
                    icon = { androidx.compose.material3.Icon(Icons.Outlined.Home, contentDescription = null) },
                    label = { Text("设备控制") },
                )
                NavigationBarItem(
                    selected = state.currentTab == DeviceTab.Me,
                    onClick = { vm.selectTab(DeviceTab.Me) },
                    icon = { androidx.compose.material3.Icon(Icons.Outlined.Person, contentDescription = null) },
                    label = { Text("我的") },
                )
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (state.currentTab) {
                DeviceTab.Control -> ControlScreen(state, vm)
                DeviceTab.Me -> MeScreen(state, vm)
            }
        }
    }
}

@Composable
private fun ControlScreen(
    state: com.example.devicecontrol.ui.AppUiState,
    vm: AppViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        PageTitle("设备控制", state.unlockStatus ?: "历史设备")
        Spacer(Modifier.height(18.dp))

        if (!state.hasToken) {
            EmptyText("请先到“我的”页面登录获取权限")
            return@Column
        }

        if (state.loadingDevices) {
            LoadingText("正在查询历史设备")
        } else if (state.devices.isEmpty()) {
            EmptyText("暂无历史设备")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 18.dp),
            ) {
                items(state.devices) { device ->
                    DeviceRow(
                        name = device.goodsName.ifBlank { "未命名设备" },
                        enabled = !state.unlocking,
                        onClick = { vm.unlock(device) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeScreen(
    state: com.example.devicecontrol.ui.AppUiState,
    vm: AppViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        PageTitle("我的", if (state.hasToken) "已登录" else "未登录")
        Spacer(Modifier.height(22.dp))

        OutlinedTextField(
            value = state.phone,
            onValueChange = vm::updatePhone,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("手机号") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(8.dp),
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = vm::sendCode,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.sendingCode,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(if (state.sendingCode) "发送中" else "发送验证码")
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.code,
            onValueChange = vm::updateCode,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("验证码") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp),
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = vm::login,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loggingIn,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)),
        ) {
            Text(if (state.loggingIn) "登录中" else "确认登录")
        }

        Spacer(Modifier.height(28.dp))
        Divider()
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("资产", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            TextButton(onClick = vm::refreshBalance, enabled = state.hasToken && !state.loadingBalance) {
                Text("刷新")
            }
        }
        Spacer(Modifier.height(8.dp))
        when {
            !state.hasToken -> EmptyText("登录后展示资产信息")
            state.loadingBalance -> LoadingText("正在查询资产")
            state.balance != null -> {
                Text("小票：${state.balance.ticketText}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("积分：${state.balance.pointsText}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("积分抵扣金额：${state.balance.integralAmount ?: "-"}", style = MaterialTheme.typography.bodyLarge)
            }
            else -> EmptyText("暂无资产信息")
        }
    }
}

@Composable
private fun PageTitle(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DeviceRow(name: String, enabled: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 18.dp),
    ) {
        Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(16.dp))
        Divider()
    }
}

@Composable
private fun LoadingText(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 36.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(strokeWidth = 2.dp)
            Spacer(Modifier.height(12.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 36.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
