package com.moment.app.hilt.app_level.interceptors.formatter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import okio.Buffer;


class MoshiFormatter extends JSONFormatter {
    private final Moshi MOSHI = new Moshi.Builder().build();
    private final JsonAdapter<Object> ADAPTER = MOSHI.adapter(Object.class).indent("    ");

    @Override
    String format(String source) {
        Buffer buffer = new Buffer().writeUtf8(source);
        JsonReader reader = JsonReader.of(buffer);
        try {
            return ADAPTER.toJson(reader.readJsonValue());
        } catch (IOException e) {
            return  "";
        }
    }

    static JSONFormatter buildIfSupported() {
        try {
            Class.forName("com.squareup.moshi.Moshi");
            return new MoshiFormatter();
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }
}
