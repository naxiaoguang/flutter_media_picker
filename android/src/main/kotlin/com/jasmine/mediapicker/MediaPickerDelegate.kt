package com.jasmine.mediapicker

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.bumptech.glide.Glide
import com.jasmine.phoenix.core.PhoenixOption
import com.jasmine.phoenix.core.model.MimeType
import com.jasmine.phoenix.picker.Phoenix
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class MediaPickerDelegate(private val activity: Activity) : PluginRegistry.ActivityResultListener {
    private val PICKER_REQUEST = 61110
    private var pendingResult: MethodChannel.Result? = null

    fun initPicker(options: MethodCall, result: MethodChannel.Result) {
        pendingResult = result
        val maxSelectCount = if (options.hasArgument("maxCount")) options.argument<Int>("maxCount") else 1
        val allowPickVideo = if (options.hasArgument("allowPickVideo")) options.argument<Boolean>("allowPickVideo") else false
        var mime = MimeType.ofImage()
        if(allowPickVideo == true ){
            mime = MimeType.ofAll()
        }
        if(Phoenix.config().imageLoader == null){
            Phoenix.config().imageLoader { context, imageView, imagePath, type ->
                Glide.with(context)
                        .load(imagePath)
                        .into(imageView)
            }
        }
        Phoenix.with()
                .theme(PhoenixOption.THEME_DEFAULT)// 主题
                .fileType(mime)//显示的文件类型图片、视频、图片和视频
                .maxPickNumber(maxSelectCount)// 最大选择数量
                .minPickNumber(1)// 最小选择数量
                .spanCount(4)// 每行显示个数
                .enablePreview(true)// 是否开启预览
                .enableCamera(false)// 是否开启拍照
                .enableAnimation(true)// 选择界面图片点击效果
                .enableCompress(true)// 是否开启压缩
                .compressPictureFilterSize(2048)//多少kb以下的图片不压缩
                .compressVideoFilterSize(2048)//多少kb以下的视频不压缩
                .thumbnailHeight(160)// 选择界面图片高度
                .thumbnailWidth(160)// 选择界面图片宽度
                .enableClickSound(false)// 是否开启点击声音
//                .pickedMediaList(mMediaAdapter.getData())// 已选图片数据
                .videoFilterTime(0)//显示多少秒以内的视频
                .mediaFilterSize(0)//显示多少kb以下的图片/视频，默认为0，表示不限制
                //如果是在Activity里使用就传Activity，如果是在Fragment里使用就传Fragment
                .start(activity, PhoenixOption.TYPE_PICK_MEDIA, PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val result = Phoenix.result(data)
            val mediaArr = JSONArray()
            for (media in result) {
                val mediaData = JSONObject()
                if(media.isCompressed)
                    mediaData.put("compressPath",media.compressPath)
                if(media.isCut){
                    mediaData.put("cutPath",media.cutPath)
                    mediaData.put("cropWidth",media.cropWidth)
                    mediaData.put("cropHeight",media.cropHeight)
                }else{
                    mediaData.put("width",media.width)
                    mediaData.put("height",media.height)
                }
                if(media.isUploaded)
                    mediaData.put("onlinePath",media.onlinePath)
                mediaData.put("localPath",media.localPath)
                if(media.mimeType.contains("video/") || media.mimeType.contains("audio/"))
                    mediaData.put("duration",media.duration)
                mediaData.put("mimeType",media.mimeType)
                mediaData.put("createTime",media.createTime)
                mediaData.put("latitude",media.latitude)
                mediaData.put("longitude",media.longitude)
                mediaArr.put(mediaData)
            }
            finishWithSuccess(mediaArr.toString())
        } else {
            finishWithNothing()
        }
        return true
    }

    private fun getAsyncSelection(activity: Activity, uri: Uri?): JSONObject? {
        val path = resolveRealPath(activity, uri!!)
        if (path == null || path.isEmpty()) {
            Log.i("MediaPicker:", "Cannot resolve asset path null || empty")
            return null
        }

        val mime = getMimeType(path)
        if (mime != null && mime.startsWith("video/")) {
            return getVideo(path)
        } else if (mime != null && mime.startsWith("image/")) {
            return getImage(path)
        } else if (mime != null && mime.startsWith("audio/")) {
            return getAudio(path)
        }
        return null
    }

    @Throws(IOException::class)
    private fun resolveRealPath(activity: Activity, uri: Uri): String {
        return RealPathUtil.getRealPathFromURI(activity, uri)
    }

    private fun getMimeType(url: String): String? {
        var mimeType: String? = null
        val uri = Uri.fromFile(File(url))
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr = activity.contentResolver
            mimeType = cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString())
            if (fileExtension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
            }
        }
        return mimeType
    }

    @Throws(Exception::class)
    private fun getImage(path: String): JSONObject? {
        val file = File(path)
        val image = JSONObject()
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return null
        }
        val original = validateImage(path)
        image.put("path", path)
        image.put("width", original.outWidth)
        image.put("height", original.outHeight)
        image.put("mime", original.outMimeType)
        image.put("size", file.length())
        image.put("modificationDate", file.lastModified().toString())
        try {
            val exif = ExifExtractor.extract(path)
            image.put("exif", exif)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return image
    }

    private fun getVideo(videoPath: String): JSONObject? {
        val bmp = validateVideo(videoPath)
        val modificationDate = File(videoPath).lastModified()
        val video = JSONObject()
        video.put("width", bmp.width)
        video.put("height", bmp.height)
        video.put("size", File(videoPath).length().toInt())
        video.put("path", "file://$videoPath")
        video.put("modificationDate", modificationDate.toString())
        return video
    }

    @Throws(Exception::class)
    private fun getAudio(path: String): JSONObject? {
        val file = File(path)
        val audio = JSONObject()
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return null
        }
        val original = validateAudio(path)
        audio.put("path", path)
        audio.put("duration", original.getString("duration"))
        audio.put("title", original.getString("title"))
        audio.put("date", original.getString("date"))
        audio.put("size", file.length())
        audio.put("modificationDate", file.lastModified().toString())
        try {
            val exif = ExifExtractor.extract(path)
            audio.put("exif", exif)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return audio
    }

    @Throws(Exception::class)
    private fun validateImage(path: String): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inPreferredConfig = Bitmap.Config.RGB_565
        BitmapFactory.decodeFile(path, options)

        if (options.outMimeType == null || options.outWidth == 0 || options.outHeight == 0) {
            throw Exception("Invalid image selected")
        }

        return options
    }

    @Throws(Exception::class)
    private fun validateVideo(path: String): Bitmap {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        retriever.release()
        return retriever.frameAtTime ?: throw Exception("Cannot retrieve video data")
    }

    private fun validateAudio(path: String): JSONObject {
        val audioData = JSONObject()
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        audioData.put("duration", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
        audioData.put("title", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
        audioData.put("date", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE))
        retriever.release()
        return audioData
    }

    private fun finishWithSuccess(data: String) {
        pendingResult!!.success(data)
        clearMethodCallAndResult()
    }
    private fun finishWithNothing() {
        pendingResult!!.notImplemented()
        clearMethodCallAndResult()
    }

    private fun finishWithError(errorCode: String, errorMessage: String) {
        pendingResult!!.error(errorCode, errorMessage, null)
        clearMethodCallAndResult()
    }

    private fun clearMethodCallAndResult() {
        pendingResult = null
    }
}