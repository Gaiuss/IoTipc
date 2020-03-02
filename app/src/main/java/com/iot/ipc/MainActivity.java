package com.iot.ipc;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.aiipc.base.permission.PermissionUtil;
import com.tuya.smart.aiipc.ipc_sdk.IPCSDK;
import com.tuya.smart.aiipc.ipc_sdk.api.IMediaTransManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IMqttProcessManager;
import com.tuya.smart.aiipc.ipc_sdk.api.INetConfigManager;
import com.tuya.smart.aiipc.ipc_sdk.service.IPCServiceManager;
import com.tuya.smart.aiipc.netconfig.mqtt.TuyaNetConfig;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionUtil.check(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WAKE_LOCK}, this::initSDK);
    }

    private void initSDK() {
        IPCSDK.initSDK(this);
        INetConfigManager iNetConfigManager = IPCServiceManager.getInstance()
                .getService(IPCServiceManager.IPCService.NET_CONFIG_SERVICE);
        iNetConfigManager.setAuthorKey("");
        iNetConfigManager.setUserId("");

        TuyaNetConfig.setDebug(true);

        INetConfigManager.NetConfigCallback netConfigCallback = new INetConfigManager.NetConfigCallback() {
            @Override
            public void configOver(boolean b, String s) {
                IMediaTransManager iMediaTransManager = IPCServiceManager.getInstance()
                        .getService(IPCServiceManager.IPCService.MEDIA_TRANS_SERVICE);
                IMqttProcessManager iMqttProcessManager = IPCServiceManager.getInstance()
                        .getService(IPCServiceManager.IPCService.MQTT_SERVICE);

                iMqttProcessManager.setMqttStatusChangedCallback(i -> Log.w("yang", "configOver: " + i));
            }

            @Override
            public void startConfig() {
                Log.d("yang", "startConfig: ");
            }

            @Override
            public void recConfigInfo() {
                Log.d("yang", "recConfigInfo: ");
            }

            @Override
            public void onNetConnectFailed() {
                iNetConfigManager.configNetInfo(this);
            }
        };

        iNetConfigManager.configNetInfo(netConfigCallback);
    }
}
