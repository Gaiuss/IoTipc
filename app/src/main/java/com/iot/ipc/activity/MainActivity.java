package com.iot.ipc.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.WriterException;
import com.iot.ipc.R;
import com.iot.ipc.google.zxing.activity.CaptureActivity;
import com.iot.ipc.google.zxing.encoding.EncodingHandler;
import com.tuya.smart.aiipc.ipc_sdk.IPCSDK;
import com.tuya.smart.aiipc.ipc_sdk.api.IMediaTransManager;
import com.tuya.smart.aiipc.ipc_sdk.api.IMqttProcessManager;
import com.tuya.smart.aiipc.ipc_sdk.api.INetConfigManager;
import com.tuya.smart.aiipc.ipc_sdk.service.IPCServiceManager;
import com.tuya.smart.aiipc.netconfig.mqtt.TuyaNetConfig;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 0;
    private TextView tvQR;
    private AppCompatEditText etQR;
    private ImageView ivQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvQR = findViewById(R.id.tv_qr_code);
        Button btnNet = findViewById(R.id.btn_net);
        Button btnUnNet = findViewById(R.id.btn_un_net);
        etQR = findViewById(R.id.et_qr_code);
        ivQR = findViewById(R.id.iv_qr_code);

//        PermissionUtil.check(this, new String[]{
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WAKE_LOCK}, this::initSDK);

        btnNet.setOnClickListener(view -> {
//            if (hasPermission(this, new String[]{Manifest.permission.CAMERA})) {
//                Intent intent = new Intent(this, CaptureActivity.class);
//                startActivityForResult(intent, REQUEST_CODE);
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 123);
//            }
            getScan();
        });

        btnUnNet.setOnClickListener(view -> setScan());
    }

    private void setScan() {
        try {
            String str = Objects.requireNonNull(etQR.getText()).toString().trim();
            if (!"".equals(str)) {
                Bitmap bitmap = EncodingHandler.createQRCode(str, 700);
                ivQR.setImageBitmap(bitmap);
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void getScan() {
        if (Build.VERSION.SDK_INT >= 23) {
            int request = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (request != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 123);
            } else {
                Intent intent = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (hasPermission(MainActivity.this, permissions)) {
//            Intent intent = new Intent(this, CaptureActivity.class);
//            startActivityForResult(intent, REQUEST_CODE);
//        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "权限申请失败，用户拒绝权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 161) {
            assert data != null;
            Bundle bundle = data.getExtras();
            assert bundle != null;
            String scanResult = bundle.getString("qr_scan_result");
            //Toast.makeText(MainActivity.this, scanResult, Toast.LENGTH_LONG).show();
            tvQR.setText(scanResult);
        }
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

//    public static boolean hasPermission(@NonNull Context context, @NonNull String[] permission) {
//        return ContextCompat.checkSelfPermission(context, permission[0]) == PackageManager.PERMISSION_GRANTED
//                && PermissionChecker.checkSelfPermission(context, permission[0]) == PermissionChecker.PERMISSION_GRANTED;
//    }

}
