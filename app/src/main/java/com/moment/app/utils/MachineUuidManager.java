package com.moment.app.utils;

import static android.content.Context.TELEPHONY_SERVICE;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.tencent.mmkv.MMKV;

import java.util.UUID;

public class MachineUuidManager {
    protected static final String PREFS_FILE = "sp_moment_device_id";
    protected static final String PREFS_DEVICE_ID = "device_id";
    protected static String uuid;

    public MachineUuidManager(Context context) {
        if (uuid == null) {
            Class var2 = MachineUuidManager.class;
            synchronized(MachineUuidManager.class) {
                if (uuid == null) {
                    MMKV prefs = MMKV.defaultMMKV();
                    String id = prefs.getString("device_id", (String)null);
                    if (id != null) {
                        uuid = id;
                    } else {
                        try {
                            String androidId = Settings.Secure.getString(context.getContentResolver(), "android_id");
                            if (!"9774d56d682e549c".equals(androidId)) {
                                uuid = androidId;
                            } else {
                                String deviceId = ((TelephonyManager)context.getSystemService(TELEPHONY_SERVICE)).getDeviceId();
                                uuid = deviceId != null ? deviceId : UUID.randomUUID().toString();
                            }
                        } catch (Exception var8) {
                            Exception e = var8;
                            throw new RuntimeException(e);
                        }

                        prefs.edit().putString("device_id", uuid.toString()).apply();
                    }
                }
            }
        }

    }

    public String getDeviceUuid() {
        return uuid;
    }
}