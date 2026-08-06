package dev.omar.aiagent;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.atomic.AtomicBoolean;

import dev.omar.aiagent.ui.crash.CrashActivity;
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
        System.setProperty("MODEL_ID", getConfigs().getGeminiModel());
        //MAIN_HANDLER.postAtFrontOfQueue(new PartCrashHandler(this));
        CrashActivity.init(this);
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
    public static String getApiKey() {
        return System.getProperty("API_KEY");
    }
    public static String getModelUrl() {
        return System.getProperty("MODEL_URL");
    }
    public static void showAboutDialog(final Context context){
        new MaterialAlertDialogBuilder(context)
                .setTitle("About Files AiAgent")
                .setMessage("An experimental AI assistant that integrates Google Gemini with the Model Context Protocol (MCP) to help you manage your files efficiently.\n\n" +
                        "This project is currently under slow development and is intended for testing and demonstration purposes.\n\n" +
                        "Developed by Omar.")
                .setPositiveButton("Close", null)
                .setNeutralButton("GITHUB",(d,i)->{
                    String url = "https://github.com/omar-haidar/Android-MCP-Files-Agent";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    context.startActivity(intent);
                })
                .setIcon(R.mipmap.ic_launcher)
                .show();
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
