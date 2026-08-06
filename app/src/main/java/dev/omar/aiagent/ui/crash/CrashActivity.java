package dev.omar.aiagent.ui.crash;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.omar.aiagent.ui.base.BaseActivity;

public class CrashActivity extends BaseActivity {
    public static final String EXTRA_CRASH_MESSAGE = "dev.omar.aiagent.ui.cras.EXTRA_CRASH_MESSAGE";
    private String crashMessage = "No crash message!";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra(EXTRA_CRASH_MESSAGE)){
            crashMessage = getIntent().getStringExtra(EXTRA_CRASH_MESSAGE);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Crash info")
                .setMessage(crashMessage)
                .setCancelable(false)
                .setPositiveButton("Exit",(d,i)->{
                    finish();
                })
                .create()
                .show();

    }

    public static void init(Application application){
        if (application == null) return;
        Thread.setDefaultUncaughtExceptionHandler((thread,throwable)->{
            Intent intent = new Intent(application.getApplicationContext(), CrashActivity.class);
            intent.putExtra(EXTRA_CRASH_MESSAGE, Log.getStackTraceString(throwable));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            application.startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        });
    }
}
