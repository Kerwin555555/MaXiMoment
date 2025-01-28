package com.moment.app.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.File;
import java.io.InputStream;
import java.net.Proxy;

import okhttp3.Call;
import okhttp3.OkHttpClient;

@GlideModule
public final class SquareHttpGlideModule extends AppGlideModule {
    private static final String TAG = "OkModule";
    private int failCount = 0;

    private void handleFail() {

    }

    private void track(String path, int code, long cost, long bytes) {
       // Cat.getInstance().track(path, code, cost, bytes);
    }


    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .proxy(Proxy.NO_PROXY)
                    // .dns(LitDns.INSTANCE)
                    .build();
            registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory((Call.Factory) client));
        } catch (Exception e) {

        }
    }


    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
        //20MB
        int memoryCacheSizeBytes = 1024 * 1024 * 40;
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));

        long diskCacheSizeBytes = 1024 * 1024 * 250L;
        DiskLruCacheFactory.CacheDirectoryGetter directoryGetter = () -> {
            File cacheDir =
                    Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                            || !Environment.isExternalStorageRemovable() ?
                            context.getExternalCacheDir(): context.getCacheDir();
            File file = new File(cacheDir.getAbsolutePath() + File.separator + "images");
            if (!file.exists()) {
                file.mkdirs();
            }
            return file;
        };
        builder.setDiskCache(new DiskLruCacheFactory(directoryGetter, diskCacheSizeBytes));

        builder.setLogLevel(Log.ERROR);
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}