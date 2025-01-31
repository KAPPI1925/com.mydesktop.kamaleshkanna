package com.mydesktop.kamaleshkanna.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GitHubUpdater {

    private const val GITHUB_API_URL = "https://api.github.com/repos/KAPPI1925/com.mydesktop.kamaleshkanna/releases/latest"

    suspend fun checkForUpdates(context: Context, currentVersion: String) {
        val latestRelease = fetchLatestGitHubRelease()

        if (latestRelease != null && latestRelease.version > currentVersion) {
            Toast.makeText(context, "New update available!", Toast.LENGTH_LONG).show()
            openDownloadPage(context, latestRelease.downloadUrl)
        } else {
            Toast.makeText(context, "No update available", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun fetchLatestGitHubRelease(): GitHubRelease? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(GITHUB_API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)

                    val tagName = json.getString("tag_name") // Latest version
                    val assets = json.getJSONArray("assets")
                    if (assets.length() > 0) {
                        val apkUrl = assets.getJSONObject(0).getString("browser_download_url")
                        return@withContext GitHubRelease(tagName, apkUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }
    }

    private fun openDownloadPage(context: Context, downloadUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
        context.startActivity(intent)
    }

    data class GitHubRelease(val version: String, val downloadUrl: String)
}
