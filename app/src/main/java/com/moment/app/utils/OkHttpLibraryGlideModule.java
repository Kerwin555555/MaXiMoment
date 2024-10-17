package com.moment.app.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;
import java.net.Proxy;

import okhttp3.Call;
import okhttp3.OkHttpClient;

@GlideModule
public final class OkHttpLibraryGlideModule extends AppGlideModule {
    private static final String TAG = "OkModule";
    private int failCount = 0;

    private void handleFail() {

    }

    private void track(String path, int code, long cost, long bytes) {
       // Cat.getInstance().track(path, code, cost, bytes);
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
//        if (Gsr.INSTANCE.getBoolean("glideLowDpi", true)) {
//            builder.setDefaultRequestOptions(new RequestOptions()
//                    .format(DecodeFormat.PREFER_RGB_565));
//        }
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        OkHttpClient client = new OkHttpClient.Builder()
                .proxy(Proxy.NO_PROXY)
               // .dns(LitDns.INSTANCE)
                .build();
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory((Call.Factory)client));
    }
}