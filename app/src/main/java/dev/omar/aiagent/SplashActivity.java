package dev.omar.aiagent;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import dev.omar.aiagent.ui.base.BaseActivity;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(App.get().getConfigs().haseApiKey()){
            startActivity(new Intent(this,MainActivity.class));
        }else {
            startActivity(new Intent(this, SetApiKeyActivity.class));
        }
        finish();
    }
}
