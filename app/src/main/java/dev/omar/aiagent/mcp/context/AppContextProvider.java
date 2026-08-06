package dev.omar.aiagent.mcp.context;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import java.io.File;

public class AppContextProvider {

    private final Context context;

    public AppContextProvider(Context context) {
        this.context = context.getApplicationContext();
    }


    public String getAppName() {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }


    public String getPackageName() {
        return context.getPackageName();
    }


    public String getInternalFilesDir() {
        return context.getFilesDir().getAbsolutePath();
    }
    public String getAppDir() {
        return context.getFilesDir().getParent();
    }


    public String getCacheDir() {
        return context.getCacheDir().getAbsolutePath();
    }

    public String getExternalFilesDir() {
        File extDir = context.getExternalFilesDir(null);
        return extDir != null ? extDir.getAbsolutePath() : "غير متاح";
    }

    public String getPublicExternalStorageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }


    public String buildAgentEnvironmentContext() {
        StringBuilder builder = new StringBuilder();
        builder.append("### 📲 معلومات البيئة والنظام للتطبيق:\n");
        builder.append("- **اسم التطبيق (App Name):** ").append(getAppName()).append("\n");
        builder.append("- **اسم الحزمة (Package Name):** ").append(getPackageName()).append("\n");
        builder.append("- **إصدار نظام أندرويد (Android OS):** SDK ").append(Build.VERSION.SDK_INT).append("\n");
        builder.append("\n### 📂 المسارات الأساسية المتاحة للـ Agent:\n");
        builder.append("- **المسار الخاص للتطبيق (Internal Files):** `").append(getInternalFilesDir()).append("`\n");
        builder.append("- **مسار التخزين المؤقت (Cache Directory):** `").append(getCacheDir()).append("`\n");
        builder.append("- **مسار التطبيق الخارجي (External App Files):** `").append(getExternalFilesDir()).append("`\n");
        builder.append("- **مسار الذاكرة العامة (Public Storage):** `").append(getPublicExternalStorageDir()).append("`\n");

        return builder.toString();
    }
}
