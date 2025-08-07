package com.rifsxd.ksunext.ui.screen


import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Wysiwyg
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dergoogler.mmrl.platform.Platform
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ExecuteModuleActionScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rifsxd.ksunext.Natives
import com.rifsxd.ksunext.R
import com.rifsxd.ksunext.ksuApp
import com.rifsxd.ksunext.ui.component.ConfirmResult
import com.rifsxd.ksunext.ui.component.rememberConfirmDialog
import com.rifsxd.ksunext.ui.component.rememberLoadingDialog
import com.rifsxd.ksunext.ui.component.SearchAppBar
import com.rifsxd.ksunext.ui.util.*
import com.rifsxd.ksunext.ui.util.DownloadListener
import com.rifsxd.ksunext.ui.util.LocalSnackbarHost
import com.rifsxd.ksunext.ui.util.download
import com.rifsxd.ksunext.ui.util.hasMagisk
import com.rifsxd.ksunext.ui.util.reboot
import com.rifsxd.ksunext.ui.util.toggleModule
import com.rifsxd.ksunext.ui.util.uninstallModule
import com.rifsxd.ksunext.ui.util.restoreModule
import com.rifsxd.ksunext.ui.viewmodel.ModuleViewModel
import com.rifsxd.ksunext.ui.webui.WebUIActivity
import com.rifsxd.ksunext.ui.webui.WebUIXActivity
import com.dergoogler.mmrl.ui.component.LabelItem
import com.topjohnwu.superuser.io.SuFile

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun ModuleScreen(navigator: DestinationsNavigator) {
    val viewModel = viewModel<ModuleViewModel>()
    val context = LocalContext.current
    val snackBarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        viewModel.sortAToZ = prefs.getBoolean("module_sort_a_to_z", true)
        viewModel.sortZToA = prefs.getBoolean("module_sort_z_to_a", false)
        viewModel.sortSizeLowToHigh = prefs.getBoolean("module_sort_size_low_to_high", false)
        viewModel.sortSizeHighToLow = prefs.getBoolean("module_sort_size_high_to_low", false)
        if (viewModel.moduleList.isEmpty() || viewModel.isNeedRefresh) {
            viewModel.fetchModuleList()
        }
    }

    val isSafeMode = Natives.isSafeMode
    val hasMagisk = hasMagisk()

    val hideInstallButton = isSafeMode || hasMagisk

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    var zipUri by remember { mutableStateOf<Uri?>(null) }
    var zipUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    val webUILauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.fetchModuleList() }

    Scaffold(
        topBar = {
            SearchAppBar(
                title = { Text(stringResource(R.string.module)) },
                searchText = viewModel.search,
                onSearchTextChange = { viewModel.search = it },
                onClearClick = { viewModel.search = "" },
                dropdownContent = {
                    var showDropdown by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showDropdown = true },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(id = R.string.settings)
                        )
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = {
                                showDropdown = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.module_sort_a_to_z))
                                },
                                trailingIcon = {
                                    Checkbox(checked = viewModel.sortAToZ, onCheckedChange = null)
                                },
                                onClick = {
                                    viewModel.sortAToZ = !viewModel.sortAToZ
                                    viewModel.sortZToA = false
                                    viewModel.sortSizeLowToHigh = false
                                    viewModel.sortSizeHighToLow = false
                                    prefs.edit()
                                        .putBoolean("module_sort_a_to_z", viewModel.sortAToZ)
                                        .putBoolean("module_sort_z_to_a", false)
                                        .putBoolean("module_sort_size_low_to_high", false)
                                        .putBoolean("module_sort_size_high_to_low", false)
                                        .apply()
                                    scope.launch {
                                        viewModel.fetchModuleList()
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.module_sort_z_to_a))
                                },
                                trailingIcon = {
                                    Checkbox(checked = viewModel.sortZToA, onCheckedChange = null)
                                },
                                onClick = {
                                    viewModel.sortZToA = !viewModel.sortZToA
                                    viewModel.sortAToZ = false
                                    viewModel.sortSizeLowToHigh = false
                                    viewModel.sortSizeHighToLow = false
                                    prefs.edit()
                                        .putBoolean("module_sort_z_to_a", viewModel.sortZToA)
                                        .putBoolean("module_sort_a_to_z", false)
                                        .putBoolean("module_sort_size_low_to_high", false)
                                        .putBoolean("module_sort_size_high_to_low", false)
                                        .apply()
                                    scope.launch {
                                        viewModel.fetchModuleList()
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.module_size_low_to_high))
                                },
                                trailingIcon = {
                                    Checkbox(checked = viewModel.sortSizeLowToHigh, onCheckedChange = null)
                                },
                                onClick = {
                                    viewModel.sortSizeLowToHigh = !viewModel.sortSizeLowToHigh
                                    viewModel.sortAToZ = false
                                    viewModel.sortZToA = false
                                    viewModel.sortSizeHighToLow = false
                                    prefs.edit()
                                        .putBoolean("module_sort_size_low_to_high", viewModel.sortSizeLowToHigh)
                                        .putBoolean("module_sort_a_to_z", false)
                                        .putBoolean("module_sort_z_to_a", false)
                                        .putBoolean("module_sort_size_high_to_low", false)
                                        .apply()
                                    scope.launch {
                                        viewModel.fetchModuleList()
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.module_size_high_to_low))
                                },
                                trailingIcon = {
                                    Checkbox(checked = viewModel.sortSizeHighToLow, onCheckedChange = null)
                                },
                                onClick = {
                                    viewModel.sortSizeHighToLow = !viewModel.sortSizeHighToLow
                                    viewModel.sortAToZ = false
                                    viewModel.sortZToA = false
                                    viewModel.sortSizeLowToHigh = false
                                    prefs.edit()
                                        .putBoolean("module_sort_size_high_to_low", viewModel.sortSizeHighToLow)
                                        .putBoolean("module_sort_a_to_z", false)
                                        .putBoolean("module_sort_z_to_a", false)
                                        .putBoolean("module_sort_size_low_to_high", false)
                                        .apply()
                                    scope.launch {
                                        viewModel.fetchModuleList()
                                    }
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (!hideInstallButton) {
                val moduleInstall = stringResource(id = R.string.module_install)
                val selectZipLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode != RESULT_OK) {
                        return@rememberLauncherForActivityResult
                    }
                    val data = result.data ?: return@rememberLauncherForActivityResult
                    val clipData = data.clipData

                    val uris = mutableListOf<Uri>()
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            clipData.getItemAt(i)?.uri?.let { uris.add(it) }
                        }
                    } else {
                        data.data?.let { uris.add(it) }
                    }

                    if (uris.isEmpty()) return@rememberLauncherForActivityResult

                    viewModel.updateZipUris(uris)

                    navigator.navigate(FlashScreenDestination(FlashIt.FlashModules(uris)))
                    viewModel.clearZipUris()
                    viewModel.markNeedRefresh()
                }

                ExtendedFloatingActionButton(
                    onClick = {
                        // Select the zip files to install
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                        selectZipLauncher.launch(intent)
                    },
                    icon = { Icon(Icons.Filled.Add, moduleInstall) },
                    text = { Text(text = moduleInstall) },
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        snackbarHost = { SnackbarHost(hostState = snackBarHost) }
    ) { innerPadding ->

        when {
            hasMagisk -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.module_magisk_conflict),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                ModuleList(
                    navigator,
                    viewModel = viewModel,
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    boxModifier = Modifier.padding(innerPadding),
                    onInstallModule = {
                        navigator.navigate(FlashScreenDestination(FlashIt.FlashModules(listOf(it))))
                    },
                    onClickModule = { id, name, hasWebUi ->
                        if (hasWebUi) {
                            val wxEngine = Intent(context, WebUIXActivity::class.java)
                                .setData("kernelsu://webuix/$id".toUri())
                                .putExtra("id", id)
                                .putExtra("name", name)

                            val ksuEngine = Intent(context, WebUIActivity::class.java)
                                .setData("kernelsu://webui/$id".toUri())
                                .putExtra("id", id)
                                .putExtra("name", name)

                            webUILauncher.launch(
                                if (prefs.getBoolean("use_webuix", true) && Platform.isAlive) {
                                    wxEngine
                                } else {
                                    ksuEngine
                                }
                            )
                        }
                    },
                    context = context,
                    snackBarHost = snackBarHost
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleList(
    navigator: DestinationsNavigator,
    viewModel: ModuleViewModel,
    modifier: Modifier = Modifier,
    boxModifier: Modifier = Modifier,
    onInstallModule: (Uri) -> Unit,
    onClickModule: (id: String, name: String, hasWebUi: Boolean) -> Unit,
    context: Context,
    snackBarHost: SnackbarHostState,
) {
    val failedEnable = stringResource(R.string.module_failed_to_enable)
    val failedDisable = stringResource(R.string.module_failed_to_disable)
    val failedUninstall = stringResource(R.string.module_uninstall_failed)
    val failedRestore = stringResource(R.string.module_restore_failed)
    val successUninstall = stringResource(R.string.module_uninstall_success)
    val successRestore = stringResource(R.string.module_restore_success)
    val reboot = stringResource(R.string.reboot)
    val rebootToApply = stringResource(R.string.reboot_to_apply)
    val moduleStr = stringResource(R.string.module)
    val uninstall = stringResource(R.string.uninstall)
    val restore = stringResource(R.string.restore)
    val cancel = stringResource(android.R.string.cancel)
    val moduleUninstallConfirm = stringResource(R.string.module_uninstall_confirm)
    val moduleRestoreConfirm = stringResource(R.string.module_restore_confirm)
    val updateText = stringResource(R.string.module_update)
    val changelogText = stringResource(R.string.module_changelog)
    val downloadingText = stringResource(R.string.module_downloading)
    val startDownloadingText = stringResource(R.string.module_start_downloading)
    val fetchChangeLogFailed = stringResource(R.string.module_changelog_failed)

    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val hasShownWarning =
        rememberSaveable { mutableStateOf(prefs.getBoolean("has_shown_warning", false)) }

    val loadingDialog = rememberLoadingDialog()
    val confirmDialog = rememberConfirmDialog()

    var expandedModuleId by rememberSaveable { mutableStateOf<String?>(null) }

    suspend fun onModuleUpdate(
        module: ModuleViewModel.ModuleInfo,
        changelogUrl: String,
        downloadUrl: String,
        fileName: String,
    ) {
        val changelogResult = loadingDialog.withLoading {
            withContext(Dispatchers.IO) {
                runCatching {
                    ksuApp.okhttpClient.newCall(
                        okhttp3.Request.Builder().url(changelogUrl).build()
                    ).execute().body!!.string()
                }
            }
        }

        val showToast: suspend (String) -> Unit = { msg ->
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val changelog = changelogResult.getOrElse {
            showToast(fetchChangeLogFailed.format(it.message))
            return
        }.ifBlank {
            showToast(fetchChangeLogFailed.format(module.name))
            return
        }

        // changelog is not empty, show it and wait for confirm
        val confirmResult = confirmDialog.awaitConfirm(
            changelogText,
            content = changelog,
            markdown = true,
            confirm = updateText,
        )

        if (confirmResult != ConfirmResult.Confirmed) {
            return
        }

        showToast(startDownloadingText.format(module.name))

        val downloading = downloadingText.format(module.name)
        withContext(Dispatchers.IO) {
            download(
                context,
                downloadUrl,
                fileName,
                downloading,
                onDownloaded = onInstallModule,
                onDownloading = {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, downloading, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    suspend fun onModuleUninstall(module: ModuleViewModel.ModuleInfo) {
        val confirmResult = confirmDialog.awaitConfirm(
            moduleStr,
            content = moduleUninstallConfirm.format(module.name),
            confirm = uninstall,
            dismiss = cancel
        )
        if (confirmResult != ConfirmResult.Confirmed) {
            return
        }

        val success = loadingDialog.withLoading {
            withContext(Dispatchers.IO) {
                uninstallModule(module.dirId)
            }
        }

        if (success) {
            viewModel.fetchModuleList()
        }
        val message = if (success) {
            successUninstall.format(module.name)
        } else {
            failedUninstall.format(module.name)
        }
        val actionLabel = if (success) {
            reboot
        } else {
            null
        }
        val result = snackBarHost.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            reboot()
        }
    }

    suspend fun onModuleRestore(module: ModuleViewModel.ModuleInfo) {
        val confirmResult = confirmDialog.awaitConfirm(
            moduleStr,
            content = moduleRestoreConfirm.format(module.name),
            confirm = restore,
            dismiss = cancel
        )
        if (confirmResult != ConfirmResult.Confirmed) {
            return
        }

        val success = loadingDialog.withLoading {
            withContext(Dispatchers.IO) {
                restoreModule(module.dirId)
            }
        }

        if (success) {
            viewModel.fetchModuleList()
        }
        val message = if (success) {
            successRestore.format(module.name)
        } else {
            failedRestore.format(module.name)
        }
    }
    PullToRefreshBox(
        modifier = boxModifier,
        onRefresh = {
            viewModel.fetchModuleList()
        },
        isRefreshing = viewModel.isRefreshing
    ) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = remember {
                PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + 56.dp + 16.dp + 48.dp + 6.dp /* Scaffold Fab Spacing + Fab container height + SnackBar height */
                )
            },
        ) {
            when {
                viewModel.moduleList.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.module_empty),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    items(viewModel.moduleList) { module ->
                        val scope = rememberCoroutineScope()
                        val updatedModule by produceState(key1 = module.id, initialValue = Triple("", "", "")) {
                            value = withContext(Dispatchers.IO) {
                                viewModel.checkUpdate(module)
                            }
                        }

                        ModuleItem(
                            navigator = navigator,
                            module = module,
                            updateUrl = updatedModule.first,
                            onUninstall = {
                                scope.launch { onModuleUninstall(module) }
                            },
                            onRestore = {
                                scope.launch { onModuleRestore(module) }
                            },
                            onCheckChanged = {
                                scope.launch {
                                    val success = loadingDialog.withLoading {
                                        withContext(Dispatchers.IO) {
                                            toggleModule(module.dirId, !module.enabled)
                                        }
                                    }
                                    if (success) {
                                        viewModel.fetchModuleList()

                                        val result = snackBarHost.showSnackbar(
                                            message = rebootToApply,
                                            actionLabel = reboot,
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            reboot()
                                        }
                                    } else {
                                        val message =
                                            if (module.enabled) failedDisable else failedEnable
                                        snackBarHost.showSnackbar(message.format(module.name))
                                    }
                                }
                            },
                            onUpdate = {
                                scope.launch {
                                    onModuleUpdate(
                                        module,
                                        updatedModule.third,
                                        updatedModule.first,
                                        "${module.name}-${updatedModule.second}.zip"
                                    )
                                    viewModel.markNeedRefresh()
                                }
                            },
                            onClick = {
                                onClickModule(it.dirId, it.name, it.hasWebUi)
                            },
                            expanded = expandedModuleId == module.id,
                            onExpandToggle = {
                                expandedModuleId = if (expandedModuleId == module.id) null else module.id
                            }
                        )

                        // fix last item shadow incomplete in LazyColumn
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
        }

        DownloadListener(context, onInstallModule)

    }
}

@Composable
fun ModuleItem(
    navigator: DestinationsNavigator,
    module: ModuleViewModel.ModuleInfo,
    updateUrl: String,
    onUninstall: (ModuleViewModel.ModuleInfo) -> Unit,
    onRestore: (ModuleViewModel.ModuleInfo) -> Unit,
    onCheckChanged: (Boolean) -> Unit,
    onUpdate: (ModuleViewModel.ModuleInfo) -> Unit,
    onClick: (ModuleViewModel.ModuleInfo) -> Unit,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
) {
    val viewModel = viewModel<ModuleViewModel>()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                onClick = onExpandToggle
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val context = LocalContext.current
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

            val useBanner = prefs.getBoolean("use_banner", true)

            if (useBanner && module.banner.isNotEmpty()) {
                val isDark = isSystemInDarkTheme()
                val colorScheme = MaterialTheme.colorScheme
                val context = LocalContext.current
                val amoledMode = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                    .getBoolean("amoled_mode", false)
                val isDynamic = colorScheme.primary != colorScheme.secondary

                val fadeColor = when {
                    amoledMode && isDark -> Color.Black
                    isDynamic -> colorScheme.surface
                    isDark -> Color(0xFF222222)
                    else -> Color.White
                }

                Box(
                    modifier = Modifier
                        .matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (module.banner.startsWith("https", true) || module.banner.startsWith("http", true)) {
                        AsyncImage(
                            model = module.banner,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.18f
                        )
                    } else {
                        val bannerData = remember(module.banner) {
                            try {
                                val file = SuFile("/data/adb/modules/${module.id}/${module.banner}")
                                file.newInputStream().use { it.readBytes() }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bannerData != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(bannerData)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.18f
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        fadeColor.copy(alpha = 0.0f),
                                        fadeColor.copy(alpha = 0.8f)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )
                }
            }
            Column {
                val interactionSource = remember { MutableInteractionSource() }

                var developerOptionsEnabled by rememberSaveable {
                    mutableStateOf(
                        prefs.getBoolean("enable_developer_options", false)
                    )
                }

                LaunchedEffect(Unit) {
                    developerOptionsEnabled = prefs.getBoolean("enable_developer_options", false)
                }

                Column(
                    modifier = Modifier
                        .padding(22.dp, 18.dp, 22.dp, 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val moduleVersion = stringResource(id = R.string.module_version)
                        val moduleAuthor = stringResource(id = R.string.module_author)
                        val moduleId = stringResource(id = R.string.module_id)
                        val moduleVersionCode = stringResource(id = R.string.module_version_code)
                        val moduleUpdateJson = stringResource(id = R.string.module_update_json)
                        val moduleUpdateJsonEmpty = stringResource(id = R.string.module_update_json_empty)

                        Column(
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LabelItem(
                                    text = formatSize(module.size),
                                    style = com.dergoogler.mmrl.ui.component.LabelItemDefaults.style.copy(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                                if (module.remove) {
                                    LabelItem(
                                        text = stringResource(R.string.uninstall),
                                        style = com.dergoogler.mmrl.ui.component.LabelItemDefaults.style.copy(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    )
                                }
                                if (updateUrl.isNotEmpty() && !module.remove && !module.update) {
                                    LabelItem(
                                        text = stringResource(R.string.module_update),
                                        style = com.dergoogler.mmrl.ui.component.LabelItemDefaults.style.copy(
                                            containerColor = MaterialTheme.colorScheme.onTertiary,
                                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    )
                                }
                                if (!module.remove) {
                                    if (module.update) {
                                        LabelItem(
                                            text = stringResource(R.string.module_updated),
                                            style = com.dergoogler.mmrl.ui.component.LabelItemDefaults.style.copy(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        )
                                    }
                                }
                                if (module.enabled && !module.remove) {
                                    if (module.hasWebUi) {
                                        LabelItem(
                                            text = stringResource(R.string.webui),
                                            style = com.dergoogler.mmrl.ui.component.LabelItemDefaults.style.copy(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                    if (module.hasActionScript) {
                                        LabelItem(
                                            text = stringResource(R.string.action),
                                            style = com.dergoogler.mmrl.ui.component.LabelItemDefaults.style.copy(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = module.name,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                                fontFamily = MaterialTheme.typography.titleMedium.fontFamily
                            )

                            Text(
                                text = "$moduleVersion: ${module.version}",
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                                fontFamily = MaterialTheme.typography.bodySmall.fontFamily
                            )

                            Text(
                                text = "$moduleAuthor: ${module.author}",
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                                fontFamily = MaterialTheme.typography.bodySmall.fontFamily
                            )

                            if (developerOptionsEnabled) {

                                Text(
                                    text = "$moduleId: ${module.id}",
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily
                                )

                                Text(
                                    text = "$moduleVersionCode: ${module.versionCode}",
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily
                                )

                                Text(
                                    text = if (module.updateJson.isNotEmpty()) "$moduleUpdateJson: ${module.updateJson}" else "$moduleUpdateJson: $moduleUpdateJsonEmpty",
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Switch(
                                enabled = !module.update,
                                checked = module.enabled,
                                onCheckedChange = onCheckChanged,
                                interactionSource = if (!module.hasWebUi) interactionSource else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = module.description,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                        fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    if (expanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    AnimatedVisibility(
                        visible = expanded,
                        enter = fadeIn() + expandVertically(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (module.hasActionScript) {
                                FilledTonalButton(
                                    modifier = Modifier.defaultMinSize(52.dp, 32.dp),
                                    enabled = !module.remove && module.enabled,
                                    onClick = {
                                        navigator.navigate(ExecuteModuleActionScreenDestination(module.dirId))
                                        viewModel.markNeedRefresh()
                                    },
                                    contentPadding = ButtonDefaults.TextButtonContentPadding
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.Outlined.Terminal,
                                        contentDescription = null
                                    )
                                    if (!module.hasWebUi && updateUrl.isEmpty()) {
                                        Text(
                                            modifier = Modifier.padding(start = 7.dp),
                                            text = stringResource(R.string.action),
                                            fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                                            fontSize = MaterialTheme.typography.labelMedium.fontSize
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(0.1f, true))
                            }

                            if (module.hasWebUi) {
                                FilledTonalButton(
                                    modifier = Modifier.defaultMinSize(52.dp, 32.dp),
                                    enabled = !module.remove && module.enabled,
                                    onClick = { onClick(module) },
                                    interactionSource = interactionSource,
                                    contentPadding = ButtonDefaults.TextButtonContentPadding
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.AutoMirrored.Outlined.Wysiwyg,
                                        contentDescription = null
                                    )
                                    if (!module.hasActionScript && updateUrl.isEmpty()) {
                                        Text(
                                            modifier = Modifier.padding(start = 7.dp),
                                            fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                                            fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                            text = stringResource(R.string.open)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f, true))

                            if (updateUrl.isNotEmpty()) {
                                Button(
                                    modifier = Modifier.defaultMinSize(52.dp, 32.dp),
                                    enabled = !module.remove,
                                    onClick = { onUpdate(module) },
                                    shape = ButtonDefaults.textShape,
                                    contentPadding = ButtonDefaults.TextButtonContentPadding
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.Outlined.Download,
                                        contentDescription = null
                                    )
                                    if (!module.hasActionScript || !module.hasWebUi) {
                                        Text(
                                            modifier = Modifier.padding(start = 7.dp),
                                            fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                                            fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                            text = stringResource(R.string.module_update)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(0.1f, true))
                            }

                            if (module.remove) {
                                FilledTonalButton(
                                    modifier = Modifier.defaultMinSize(52.dp, 32.dp),
                                    onClick = { onRestore(module) },
                                    contentPadding = ButtonDefaults.TextButtonContentPadding
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.Outlined.Restore,
                                        contentDescription = null
                                    )
                                    if (!module.hasActionScript && !module.hasWebUi && updateUrl.isEmpty()) {
                                        Text(
                                            modifier = Modifier.padding(start = 7.dp),
                                            fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                                            fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                            text = stringResource(R.string.restore)
                                        )
                                    }
                                }
                            } else {
                                FilledTonalButton(
                                    modifier = Modifier.defaultMinSize(52.dp, 32.dp),
                                    enabled = true,
                                    onClick = { onUninstall(module) },
                                    contentPadding = ButtonDefaults.TextButtonContentPadding
                                ) {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null
                                    )
                                    if (!module.hasActionScript && !module.hasWebUi && updateUrl.isEmpty()) {
                                        Text(
                                            modifier = Modifier.padding(start = 7.dp),
                                            fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                                            fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                            text = stringResource(R.string.uninstall)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatSize(size: Long): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        size >= gb -> String.format("%.2f GB", size.toDouble() / gb)
        size >= mb -> String.format("%.2f MB", size.toDouble() / mb)
        size >= kb -> String.format("%.2f KB", size.toDouble() / kb)
        else -> "$size B"
    }
}

@Preview
@Composable
fun ModuleItemPreview() {
    val module = ModuleViewModel.ModuleInfo(
        id = "id",
        name = "name",
        version = "version",
        versionCode = 1,
        author = "author",
        description = "I am a test module and i do nothing but show a very long description",
        enabled = true,
        update = true,
        remove = false,
        updateJson = "",
        hasWebUi = false,
        hasActionScript = false,
        dirId = "dirId",
        size = 12345678L,
        banner = ""
    )
    ModuleItem(EmptyDestinationsNavigator, module, "", {}, {}, {}, {}, {}, false, {})
}
