package com.axiii.parkingtally.model


import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.util.Date

class JavaScriptInterface(private val context: Context) {
    @JavascriptInterface
    @Throws(IOException::class)
    fun getBase64FromBlobData(base64Data: String) {
        convertBase64StringToFileAndStoreIt(base64Data)
    }

    @Throws(IOException::class)
    private fun convertBase64StringToFileAndStoreIt(base64PDf: String) {
        val notificationId = 1
        val currentDateTime = DateFormat.getDateTimeInstance().format(Date())
        val newTime = currentDateTime.replaceFirst(", ".toRegex(), "_").replace(" ".toRegex(), "_")
            .replace(":".toRegex(), "-")
        Log.d("fileMimeType ====> ", fileMimeType!!)
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val extension = mimeTypeMap.getExtensionFromMimeType(fileMimeType)
        val dwldsPath = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).toString() + "/" + newTime + "_." + extension
        )
        val regex = "^data:" + fileMimeType + ";base64,"
        val pdfAsBytes = Base64.decode(base64PDf.replaceFirst(regex.toRegex(), ""), Base64.DEFAULT)
        try {
            val os = FileOutputStream(dwldsPath)
            os.write(pdfAsBytes)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Toast.makeText(context, "FAILED TO DOWNLOAD THE FILE!", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
        if (dwldsPath.exists()) {
            val intent = Intent()
            intent.setAction(Intent.ACTION_VIEW)
            val apkURI = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                dwldsPath
            )
            intent.setDataAndType(
                apkURI,
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val CHANNEL_ID = "MYCHANNEL"
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentText("You have got something new!")
                .setContentTitle("Report downloaded")
                .setContentIntent(pendingIntent)
                .setChannelId(CHANNEL_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.stat_sys_download_done)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create the NotificationChannel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "name", NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(notificationId, builder.build())
        }
        Toast.makeText(context, "Report downloaded!", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private var fileMimeType: String? = null

        @JvmStatic
        fun getBase64StringFromBlobUrl(blobUrl: String, mimeType: String): String {
            if (blobUrl.startsWith("blob")) {
                fileMimeType = mimeType
                return "javascript: var xhr = new XMLHttpRequest();" +
                        "xhr.open('GET', '" + blobUrl + "', true);" +
                        "xhr.setRequestHeader('Content-type','" + mimeType + ";charset=UTF-8');" +
                        "xhr.responseType = 'blob';" +
                        "xhr.onload = function(e) {" +
                        "    if (this.status == 200) {" +
                        "        var blobFile = this.response;" +
                        "        var reader = new FileReader();" +
                        "        reader.readAsDataURL(blobFile);" +
                        "        reader.onloadend = function() {" +
                        "            base64data = reader.result;" +
                        "            Android.getBase64FromBlobData(base64data);" +
                        "        }" +
                        "    }" +
                        "};" +
                        "xhr.send();"
            }
            return "javascript: console.log('It is not a Blob URL');"
        }
    }
}
