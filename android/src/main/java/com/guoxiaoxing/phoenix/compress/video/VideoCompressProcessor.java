package com.guoxiaoxing.phoenix.compress.video;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.guoxiaoxing.phoenix.compress.video.format.MediaFormatStrategyPresets;
import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.listener.OnProcessorListener;
import com.guoxiaoxing.phoenix.core.listener.Processor;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import silicompressorr.SiliCompressor;
import silicompressorr.VideoCompress;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/4 下午1:44
 */
public class VideoCompressProcessor implements Processor {

    private static final String TAG = "VideoCompressProcessor";

    @Override
    public MediaEntity syncProcess(Context context, MediaEntity mediaEntity, PhoenixOption phoenixOption) {

        if (mediaEntity == null) {
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        final MediaEntity result = mediaEntity;

        final File compressFile;
        try {
            File compressCachePath = new File(context.getCacheDir(), "outputs");
            compressCachePath.mkdir();
            compressFile = File.createTempFile("compress", ".mp4", compressCachePath);
        } catch (IOException e) {
            Toast.makeText(context, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
            return null;
        }

        try {
            String compressPath = SiliCompressor.with(context).compressVideo(mediaEntity.getLocalPath(), compressFile.getAbsolutePath()) ;
            result.setCompressed(true);
            result.setCompressPath(compressPath);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
//        try {
//           String compressPath =  VideoCompressor.with().syncTranscodeVideo(mediaEntity.getLocalPath(), compressFile.getAbsolutePath(),
//                    MediaFormatStrategyPresets.createAndroid480pFormatStrategy());
//            result.setCompressed(true);
//            result.setCompressPath(compressPath);
//            return mediaEntity;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    @Override
    public void asyncProcess(Context context, final MediaEntity mediaEntity, PhoenixOption phoenixOption, final OnProcessorListener onProcessorListener) {

        if (mediaEntity == null) {
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        if (onProcessorListener == null) {
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        final MediaEntity result = mediaEntity;

        final File compressFile;
        try {
            File compressCachePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "phoenix");
            compressCachePath.mkdir();
            compressFile = File.createTempFile("compress", ".mp4", compressCachePath);
        } catch (IOException e) {
            Toast.makeText(context, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
            return;
        }
//        VideoCompressor.Listener listener = new VideoCompressor.Listener() {
//            @Override
//            public void onTranscodeProgress(double progress) {
//                onProcessorListener.onProgress((int) progress);
//            }
//
//            @Override
//            public void onTranscodeCompleted() {
//                result.setCompressed(true);
//                result.setCompressPath(compressFile.getAbsolutePath());
//                onProcessorListener.onSuccess(result);
//            }
//
//            @Override
//            public void onTranscodeCanceled() {
//
//            }
//
//            @Override
//            public void onTranscodeFailed(Exception exception) {
//                onProcessorListener.onFailed(exception.getMessage());
//            }
//        };
        VideoCompress.CompressListener listener1 = new VideoCompress.CompressListener(){
            @Override
            public void onStart() {
                onProcessorListener.onStart(result);
            }

            @Override
            public void onSuccess() {
                result.setCompressed(true);
                result.setCompressPath(compressFile.getAbsolutePath());
                onProcessorListener.onSuccess(result);
            }

            @Override
            public void onFail() {
                onProcessorListener.onFailed("video compress failed" + compressFile.getAbsolutePath());
            }

            @Override
            public void onProgress(float percent) {
                onProcessorListener.onProgress((int) percent);
            }
        };
        VideoCompress.compressVideoMedium(mediaEntity.getLocalPath(),compressFile.getAbsolutePath(),listener1) ;
//
//        try {
//            VideoCompressor.with().asyncTranscodeVideo(mediaEntity.getLocalPath(), compressFile.getAbsolutePath(),
//                    MediaFormatStrategyPresets.createAndroid480pFormatStrategy(), listener);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
