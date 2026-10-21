package com.example.devicecontrol

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.devicecontrol.data.AppRepository
import com.example.devicecontrol.data.DeviceItem
import com.example.devicecontrol.data.OrderHistoryItem
import com.example.devicecontrol.data.OrderHistoryStore
import com.example.devicecontrol.data.TokenStore
import com.example.devicecontrol.data.UnlockResult
import com.example.devicecontrol.ui.AppViewModel
import com.example.devicecontrol.ui.AppViewModelFactory
import com.example.devicecontrol.ui.DeviceShortcutRequest
import com.example.devicecontrol.ui.DeviceTab
import com.example.devicecontrol.ui.theme.DeviceControlTheme

private const val ACTION_OPEN_DEVICE_SHORTCUT = "com.example.devicecontrol.OPEN_DEVICE_SHORTCUT"
private const val EXTRA_GOODS_ID = "goods_id"
private const val EXTRA_DEVICE_ID = "device_id"
private const val EXTRA_GOODS_NAME = "goods_name"
private const val PROJECT_URL = "https://github.com/wzs0512/qiekj-android"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeviceControlTheme {
                val repository = remember {
                    AppRepository(
                        tokenStore = TokenStore(applicationContext),
                        orderHistoryStore = OrderHistoryStore(applicationContext),
                    )
                }
                val vm: AppViewModel = viewModel(
                    factory = AppViewModelFactory(repository),
                )
                LaunchedEffect(vm) {
                    shortcutRequestFromIntent(intent)?.let(vm::openDeviceShortcut)
                }
                DeviceControlApp(vm)
            }
        }
    }
}

private fun shortcutRequestFromIntent(intent: Intent?): DeviceShortcutRequest? {
    if (intent?.action != ACTION_OPEN_DEVICE_SHORTCUT) return null
    return DeviceShortcutRequest(
        goodsId = intent.getStringExtra(EXTRA_GOODS_ID),
        id = intent.getStringExtra(EXTRA_DEVICE_ID),
        goodsName = intent.getStringExtra(EXTRA_GOODS_NAME),
    )
}

private fun pinDeviceShortcut(context: Context, device: DeviceItem) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        Toast.makeText(context, "当前系统不支持添加桌面快捷方式", Toast.LENGTH_LONG).show()
        return
    }
    val shortcutManager = context.getSystemService(ShortcutManager::class.java)
    if (shortcutManager?.isRequestPinShortcutSupported != true) {
        Toast.makeText(context, "当前桌面不支持添加快捷方式", Toast.LENGTH_LONG).show()
        return
    }

    val label = device.goodsName.ifBlank { "历史设备" }
    val shortcutIntent = Intent(context, MainActivity::class.java).apply {
        action = ACTION_OPEN_DEVICE_SHORTCUT
        putExtra(EXTRA_GOODS_ID, device.goodsId)
        putExtra(EXTRA_DEVICE_ID, device.id)
        putExtra(EXTRA_GOODS_NAME, device.goodsName)
    }
    val shortcut = ShortcutInfo.Builder(
        context,
        "device-${device.goodsId ?: device.id ?: device.goodsName.hashCode()}",
    )
        .setShortLabel(label.take(10))
        .setLongLabel(label)
        .setIcon(Icon.createWithResource(context, R.drawable.ic_launcher))
        .setIntent(shortcutIntent)
        .build()

    shortcutManager.requestPinShortcut(shortcut, null)
}

private fun openProjectHome(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_URL))
    context.startActivity(intent)
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

    state.tokenDialogText?.let { token ->
        TokenDialog(token = token, onDismiss = vm::dismissCurrentToken)
    }

    if (state.showOrderHistory) {
        OrderHistoryDialog(
            orders = state.orderHistory,
            onDismiss = vm::dismissOrderHistory,
            onOpenOrder = vm::showHistoricalOrder,
        )
    }

    state.orderDetail?.let { detail ->
        OrderDetailDialog(
            detail = detail,
            onDismiss = vm::dismissOrderDetail,
        )
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
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
                NavigationBarItem(
                    selected = state.currentTab == DeviceTab.Me,
                    onClick = { vm.selectTab(DeviceTab.Me) },
                    icon = { androidx.compose.material3.Icon(Icons.Outlined.Person, contentDescription = null) },
                    label = { Text("我的") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
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
private fun TokenDialog(token: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("当前 Token") },
        text = {
            Text(
                text = token,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        shape = RoundedCornerShape(8.dp),
    )
}
@Composable
private fun OrderDetailDialog(
    detail: UnlockResult,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("订单详情") },
        text = {
            Column {
                DetailLine("订单号", detail.orderNo)
                DetailLine("订单 ID", detail.orderId)
                DetailLine("订单原价", detail.originPrice)
                DetailLine("花费小票", detail.ticketCost)
                DetailLine("积分抵扣", detail.integralCost)
                if (detail.otherPromotions.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text("其他优惠", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    detail.otherPromotions.forEach { promotion ->
                        DetailLine(
                            label = "类型 ${promotion.promotionType ?: "-"}",
                            value = promotion.discountAmount ?: "-",
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了")
            }
        },
        shape = RoundedCornerShape(8.dp),
    )
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}


@Composable
private fun ControlScreen(
    state: com.example.devicecontrol.ui.AppUiState,
    vm: AppViewModel,
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    PageTitle("设备控制", "管理并快速使用历史设备")
                }
                IconButton(
                    onClick = vm::refreshDevices,
                    enabled = state.hasToken && !state.loadingDevices,
                ) {
                    androidx.compose.material3.Icon(Icons.Outlined.Refresh, contentDescription = "刷新设备")
                }
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("欢迎回来", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (state.hasToken) "选择一台设备即可开始使用。" else "登录后即可查看你的设备并开始使用。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                    )
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("历史设备", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    if (!state.hasToken) "未登录" else if (state.loadingDevices) "同步中" else "${state.devices.size} 台",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        when {
            !state.hasToken -> item { EmptyPanel("请先登录", "前往“我的”页面登录后，即可同步历史设备。") }
            state.loadingDevices && state.devices.isEmpty() -> item { LoadingPanel("正在查询历史设备") }
            state.devices.isEmpty() -> item { EmptyPanel("还没有历史设备", "使用过的设备会显示在这里。") }
            else -> items(state.devices) { device ->
                DeviceCard(
                    name = device.goodsName.ifBlank { "未命名设备" },
                    status = state.unlockStatus,
                    enabled = !state.unlocking,
                    onClick = { vm.unlock(device) },
                    onAddShortcut = { pinDeviceShortcut(context, device) },
                )
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
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                PageTitle("我的", if (state.hasToken) "账号与资产" else "登录后管理账号和订单")
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = if (state.hasToken) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    if (state.hasToken) "已登录" else "未登录",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("账号登录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("登录后同步设备与账户资产", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = vm::updatePhone,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("手机号") },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.PhoneAndroid, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(16.dp),
                )
                Button(
                    onClick = vm::sendCode,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.sendingCode,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Text(if (state.sendingCode) "正在发送验证码…" else "发送验证码")
                }
                OutlinedTextField(
                    value = state.code,
                    onValueChange = vm::updateCode,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("短信验证码") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                )
                Button(
                    onClick = vm::login,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loggingIn,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Text(if (state.loggingIn) "正在登录…" else "登录")
                }
            }
        }

        Text("快捷操作", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile(
                modifier = Modifier.weight(1f),
                icon = { androidx.compose.material3.Icon(Icons.Outlined.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = "当前 Token",
                subtitle = "查看登录凭证",
                onClick = vm::showCurrentToken,
            )
            ActionTile(
                modifier = Modifier.weight(1f),
                icon = { androidx.compose.material3.Icon(Icons.Outlined.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = "历史订单",
                subtitle = "查看使用记录",
                onClick = vm::showOrderHistory,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("账户资产", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    IconButton(onClick = vm::refreshBalance, enabled = state.hasToken && !state.loadingBalance) {
                        androidx.compose.material3.Icon(Icons.Outlined.Refresh, contentDescription = "刷新资产")
                    }
                }
                when {
                    !state.hasToken -> Text("登录后查看小票、积分和抵扣金额", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    state.loadingBalance -> LoadingPanel("正在查询账户资产")
                    state.balance != null -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssetValue(Modifier.weight(1f), "小票", state.balance.ticketText, Icons.Outlined.ConfirmationNumber)
                            AssetValue(Modifier.weight(1f), "积分", state.balance.pointsText, Icons.Outlined.Stars)
                            AssetValue(Modifier.weight(1f), "积分抵扣", state.balance.integralAmount ?: "-", Icons.Outlined.Code)
                        }
                    }
                    else -> Text("暂无资产信息", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        IconButton(onClick = { openProjectHome(context) }, modifier = Modifier.align(Alignment.End)) {
            androidx.compose.material3.Icon(Icons.Outlined.Code, contentDescription = "打开 GitHub 项目")
        }
    }
}

@Composable
private fun OrderHistoryDialog(
    orders: List<OrderHistoryItem>,
    onDismiss: () -> Unit,
    onOpenOrder: (OrderHistoryItem) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("历史订单") },
        text = {
            if (orders.isEmpty()) {
                Text("暂无历史订单", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn {
                    items(orders) { item ->
                        OrderHistoryRow(item = item, onClick = { onOpenOrder(item) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        shape = RoundedCornerShape(8.dp),
    )
}
@Composable
private fun OrderHistoryRow(item: OrderHistoryItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(item.goodsName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(
            "订单：${item.orderNo}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "原价：${item.originPrice}  小票：${item.ticketCost}  积分：${item.integralCost}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
    }
}

@Composable
private fun PageTitle(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DeviceCard(
    name: String,
    status: String?,
    enabled: Boolean,
    onClick: () -> Unit,
    onAddShortcut: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                androidx.compose.material3.Icon(
                    Icons.Outlined.Home,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(status ?: "点击卡片开始使用", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onAddShortcut, enabled = enabled) {
                androidx.compose.material3.Icon(Icons.Outlined.Add, contentDescription = "添加到桌面")
            }
        }
    }
}

@Composable
private fun EmptyPanel(title: String, message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LoadingPanel(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(strokeWidth = 2.dp)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActionTile(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            icon()
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AssetValue(
    modifier: Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        androidx.compose.material3.Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyText(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 36.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
