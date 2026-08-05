package dev.omar.aiagent;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.atomic.AtomicBoolean;

import dev.omar.aiagent.utils.Configs;

public class App extends Application {
    private static volatile Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static App sApp;
    private Configs mConfigs;
    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        System.setProperty("API_KEY",getConfigs().getApiKey());
        System.setProperty("MODEL_URL",getConfigs().getModelUrl());
        System.setProperty("MODEL_ID",getConfigs().getGeminiModel());
        MAIN_HANDLER.postAtFrontOfQueue(new PartCrashHandler(this));
    }

    public static App get(){
        return sApp;
    }

    public Configs getConfigs() {
        if (mConfigs == null) {
            mConfigs = new Configs(this);
        }
        return mConfigs;
    }

    public static void showApiKey(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("API Key")
                .setMessage(getApiKey())
                .setPositiveButton("OK", null)
                .create().show();
    }

    public static String getApiKey() {
        return System.getProperty("API_KEY");
    }
    public static String getModelUrl() {
        return System.getProperty("MODEL_URL");
    }
    public static String getModelId() {
        return System.getProperty("MODEL_ID");
    }
    private static class PartCrashHandler implements Runnable {
        private AtomicBoolean isRunning = new AtomicBoolean(true);
        private Application application;
        public PartCrashHandler(Application application) {
            this.application = application;
        }

        @Override
        public void run() {
            while (isRunning.get()) {
                try {
                    Looper.loop();
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (isRunning.get()) {
                        MAIN_HANDLER.post(() -> {
                            Toast.makeText(application.getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                        });
                    }else {
                        if (e instanceof RuntimeException){
                            throw (RuntimeException)e;
                        }else {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

        }
    }
}
