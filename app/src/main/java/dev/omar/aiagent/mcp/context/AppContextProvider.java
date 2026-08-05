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

    /**
     * 📱 اسم التطبيق
     */
    public String getAppName() {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    /**
     * 📦 اسم الحزمة (Package Name)
     */
    public String getPackageName() {
        return context.getPackageName();
    }

    /**
     * 📁 مسار المجلد الخاص بالتطبيق في الذاكرة الداخلية (Internal Storage Path)
     * (مثال: /data/user/0/dev.omar.aiagent/files)
     */
    public String getInternalFilesDir() {
        return context.getFilesDir().getAbsolutePath();
    }

    /**
     * 🗂️ مسار مجلد الكاش الخاص بالتطبيق (Cache Directory)
     */
    public String getCacheDir() {
        return context.getCacheDir().getAbsolutePath();
    }

    /**
     * 💾 مسار المجلد الخاص بالتطبيق في الذاكرة الخارجية (App-Specific External Storage)
     * (مثال: /storage/emulated/0/Android/data/dev.omar.aiagent/files)
     */
    public String getExternalFilesDir() {
        File extDir = context.getExternalFilesDir(null);
        return extDir != null ? extDir.getAbsolutePath() : "غير متاح";
    }

    /**
     * 📂 مسار التخزين الخارجي العام (Public External Storage)
     * (مثال: /storage/emulated/0)
     */
    public String getPublicExternalStorageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 📑 بناء وصف شامل لسياق النظام (System Context Description)
     * ليمثل الانطلاقة الخاصة بالـ Agent
     */
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
        builder.append("- **مجلد التنزيلات العام (Downloads):** `").append(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()).append("`\n");
        builder.append("- **مجلد المستندات العام (Documents):** `").append(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()).append("`\n");

        return builder.toString();
    }
}
