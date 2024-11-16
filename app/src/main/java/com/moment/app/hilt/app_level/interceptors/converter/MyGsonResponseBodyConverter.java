package com.moment.app.hilt.app_level.interceptors.converter;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.moment.app.datamodel.BackendException;
import com.moment.app.datamodel.Results;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;

public class MyGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private final Gson mGson;
    private final TypeAdapter<T> adapter;

    public MyGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        mGson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        MediaType mediaType = value.contentType();
        String response = value.string();
        Charset charset = mediaType != null ? mediaType.charset(UTF_8) : UTF_8;
        ByteArrayInputStream bis = new ByteArrayInputStream(response.getBytes());
        InputStreamReader reader = new InputStreamReader(bis,charset);
        JsonReader jsonReader = mGson.newJsonReader(reader);
        try {
            T object = adapter.read(jsonReader);
            if (object instanceof Results) {
                Results<?> re = (Results<?>) object;
                if (re.getCode() != 0) {
                    throw new BackendException(re.getCode(),
                            TextUtils.isEmpty(re.getMessage())? re.getMsg():re.getMessage(), response);
                }
            }
            return object;
        } finally {
            value.close();
        }
    }
}
