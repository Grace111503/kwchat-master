package com.kwp.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.Bridge;
import com.getcapacitor.BridgeActivity;
import ee.forgr.capacitor_updater.CapacitorUpdaterPlugin;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BridgeActivity {

    private static final int REQUEST_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 注册 CapacitorUpdater 插件
        registerPlugin(CapacitorUpdaterPlugin.class);

        // 配置 WebView 设置，确保在 Capacitor 环境下可以正常加载 HTTP 图片/头像
        configureWebViewSettings();

        // 启动时主动请求运行时权限（麦克风、相机等）
        requestAppPermissions();
    }

    /**
     * 配置 WebView 设置
     * - 允许混合内容加载（https 下加载 http 图片）
     * - 启用 DOM 存储和数据库
     * - 允许文件访问
     */
    private void configureWebViewSettings() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getWebView() != null) {
            WebSettings settings = bridge.getWebView().getSettings();
            // 允许混合内容加载（HTTP 资源）
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            // 启用 DOM 存储
            settings.setDomStorageEnabled(true);
            // 启用数据库
            settings.setDatabaseEnabled(true);
            // 允许文件访问（头像等本地文件）
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            // 允许通过 file:// scheme 加载内容
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
            // 缓存模式
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }
    }

    /**
     * 主动请求应用所需的运行时权限
     * 这样 WebView 中的 getUserMedia 才能正常工作
     */
    private void requestAppPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // 麦克风权限（录音）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }

        // 相机权限（拍照）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        // 存储权限（Android 12及以下）
        if (android.os.Build.VERSION.SDK_INT <= 32) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // Android 13+ 细分媒体权限
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES")
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add("android.permission.READ_MEDIA_IMAGES");
            }
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO")
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add("android.permission.READ_MEDIA_VIDEO");
            }
        }

        // 通知权限（Android 13+）
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("[KWChat] 权限已授予: " + permissions[i]);
                } else {
                    System.out.println("[KWChat] 权限被拒绝: " + permissions[i]);
                }
            }
        }
    }
}
