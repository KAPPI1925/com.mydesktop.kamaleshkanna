package com.mydesktop.kamaleshkanna

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mydesktop.kamaleshkanna.ui.theme.KamaleshKannaTheme
import kotlinx.coroutines.launch
import com.mydesktop.kamaleshkanna.utils.GitHubUpdater.checkForUpdates
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {
    @androidx.test.filters.SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KamaleshKannaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WebViewWithDrawer()
                }
            }
        }
    }

    // ✅ Function to get the installed app version dynamically
    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "0.0"  // If versionName is null, default to "0.0"
        } catch (e: Exception) {
            e.printStackTrace()
            "0.0" // Default version if retrieval fails
        }
    }
    // ✅ Place it below getAppVersion() to maintain structure
    private fun OpenLinkInApp(context: Context, url: String) {
        try {
            val intent = when {
                url.contains("linkedin.com") -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        setPackage("com.linkedin.android")
                    }
                }
                url.contains("youtube.com") -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        setPackage("com.google.android.youtube")
                    }
                }
                url.contains("instagram.com") -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        setPackage("com.instagram.android")
                    }
                }
                else -> Intent(Intent.ACTION_VIEW, Uri.parse(url)) // Default behavior for other URLs
            }
            val packageManager: PackageManager = context.packageManager
            val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

            if (activities.isNotEmpty()) {
                context.startActivity(intent)
            } else {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening link: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewWithDrawer() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val websiteUrl = "https://sites.google.com/view/kamalesh-kanna-s/home"
    var webView: WebView? by remember { mutableStateOf(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (drawerState.isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f))
                    .clickable {
                        if (drawerState.isOpen) {
                            scope.launch { drawerState.close() }
                        }
                    }
            )
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Box(modifier = Modifier.width(300.dp)) {
                    NavigationDrawerContent { url ->
                        errorMessage = null
                        isLoading = true
                        webView?.loadUrl(url)
                        scope.launch { drawerState.close() }
                    }
                }
            },
            gesturesEnabled = drawerState.isOpen
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Kamalesh Kanna") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    if (errorMessage == null) {
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.safeBrowsingEnabled = true
                                    settings.allowFileAccess = false
                                    settings.allowFileAccessFromFileURLs = false
                                    settings.allowUniversalAccessFromFileURLs = false

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                            super.onPageStarted(view, url, favicon)
                                            Log.d("WebView", "Page started: $url")
                                        }

                                        override fun onPageFinished(view: WebView, url: String) {
                                            super.onPageFinished(view, url)
                                            Log.d("WebView", "Page finished: $url")
                                            isLoading = false
                                        }

                                        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                                            super.onReceivedError(view, errorCode, description, failingUrl)
                                            errorMessage = "Error: Restart the App"
                                            isLoading = false
                                            Log.e("WebView", "Error: $description")
                                        }
                                    }

                                    loadUrl(websiteUrl)
                                    webView = this
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = errorMessage ?: "Unknown error", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    NavigationButtonBar(webView, websiteUrl, { errorMessage = null })
                }
            }
        }
    }
}


    @Composable
fun NavigationDrawerContent(onItemClick: (String) -> Unit) {
    val context = LocalContext.current  // Get the context here
        LocalLifecycleOwner.current // Get Lifecycle Owner

        val menuItems = listOf(
        DrawerItem("Home", Icons.Default.Home, "https://sites.google.com/view/kamalesh-kanna-s/home"),
        DrawerItem("About Me", Icons.Default.Person, "https://sites.google.com/view/kamalesh-kanna-s/about-me"),
        DrawerItem("Publications", Icons.Default.MenuBook, "https://www.researchgate.net/profile/Kamalesh-Kanna/research"),
        DrawerItem("Blog", Icons.Default.RssFeed, "https://example.com/blog"),
        DrawerItem("Profile", Icons.Default.AccountCircle, "https://example.com/profile")
    )
    val projectItems = listOf(
        DrawerItem("Project 1", Icons.Filled.Person, "https://example.com/project1"),
        DrawerItem("Project 2", Icons.Outlined.Person, "https://example.com/project2")
    )
    val socialLinks = listOf(
        "LinkedIn" to "https://www.linkedin.com/in/kamalesh-kanna-s/",
        "ResearchGate" to "https://www.researchgate.net/profile/Kamalesh-Kanna",
        "GitHub" to "https://github.com/KAPPI1925",
        "YouTube" to "https://www.youtube.com/@MyDesktopTech",
        "My Desktop Tech" to "https://sites.google.com/view/mydesktoptamil/home",
        "Gis-hub" to "https://gis-hub.blogspot.com/",
        "Instagram" to "https://www.instagram.com/kamaleshkanna_s/",
        "X" to "https://x.com/kamalesh0109",
    )

    val textSize = 13.dp // Adjust this value to change text size

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .padding(0.dp)
    ) {
        // Use LazyColumn instead of Column for scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Drawer Header (for name, divider)
            item {
                Text("Kamalesh Kanna", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, start = 15.dp))
                HorizontalDivider(thickness = 2.dp) // Added Divider below name
            }

            // Menu Items
            items(menuItems) { item ->
                NavigationDrawerItem(
                    label = { Text(item.title) },
                    icon = {
                        when (item.icon) {
                            is Int -> Image(painter = painterResource(id = item.icon), contentDescription = item.title)
                            is ImageVector -> Icon(imageVector = item.icon, contentDescription = item.title)
                            else -> {}
                        }
                    },
                    selected = false,
                    onClick = { onItemClick(item.url) }
                )
            }

            // Projects Section
            item {
                Text("Projects", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp, bottom = 5.dp, start = 15.dp))
            }
            items(projectItems) { item ->
                NavigationDrawerItem(
                    label = { Text(item.title) },
                    icon = {
                        when (item.icon) {
                            is Int -> Image(painter = painterResource(id = item.icon), contentDescription = item.title)
                            is ImageVector -> Icon(imageVector = item.icon, contentDescription = item.title)
                            else -> {}
                        }
                    },
                    selected = false,
                    onClick = { onItemClick(item.url) }
                )
            }

            // Social Links Section
            item {
                HorizontalDivider()
                Text("Social Links", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 5.dp, bottom = 1.dp, start = 15.dp, end = 15.dp))
            }

            // Social Links in LazyRow for side-by-side display
            // Social Links in LazyRow for side-by-side display
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Spacing between items
                ) {
                    items(socialLinks) { (name, url) ->
                        OutlinedButton(
                            onClick = { OpenLinkInApp(context, url) },
                            modifier = Modifier.padding(vertical = 5.dp) // Adjust padding for better spacing
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = textSize.value.sp),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                HorizontalDivider()
            }

            // Mail and Update Button Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween // Align buttons side by side
                ) {
                    FilledTonalButton(
                        onClick = {
                            Toast.makeText(context, "Opened Mail", Toast.LENGTH_SHORT).show()

                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:kamaleshkanna.skk@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Inquiry from App")
                            }
                            context.startActivity(emailIntent)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = "Mail")
                        Spacer(modifier = Modifier.width(9.dp))
                        Text("Mail")
                    }
                    FilledTonalButton(
                        onClick = {
                            // Use the lifecycleScope directly for ComponentActivity
                            lifecycleScope.launch {
                                // Get the current app version dynamically
                                getAppVersion(context)

                                // Call checkForUpdates with the current version of the app
                                checkForUpdates(
                                    context,
                                    "3.0-alpha"
                                )
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Update, contentDescription = "Update")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update")
                    }
                }
            }
        }
    }
}

data class DrawerItem(val title: String, val icon: Any, val url: String)

@Composable
fun NavigationButtonBar(webView: WebView?, websiteUrl: String, onErrorMessageReset: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = { webView?.goBack() }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        IconButton(onClick = { onErrorMessageReset(); webView?.loadUrl(websiteUrl) }) {
            Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
        }

        IconButton(onClick = { webView?.goForward() }) {
            Icon(imageVector = Icons.AutoMirrored.Default.ArrowForward, contentDescription = "Forward")
        }

        IconButton(onClick = { webView?.reload() }) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
        }
    }
}}
