package dev.omar.aiagent.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class Configs {
    private Context context;
    private SharedPreferences sharedPreferences;

    private ModelChangedListener modelChangedListener;
    public interface ModelChangedListener {
        void onModelChanged(String model);
    }

    public void setModelChangedListener(ModelChangedListener modelChangedListener){
        this.modelChangedListener = modelChangedListener;
    }

    public Configs(@NonNull Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("configs", Context.MODE_PRIVATE);
    }

    public boolean isMemoryEnabled() {
        return sharedPreferences.getBoolean("memory_enabled", true);
    }

    public void setMemoryEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean("memory_enabled", enabled).apply();
    }

    public boolean haseApiKey() {
        return !getApiKey().equals("");
    }

    public String getApiKey() {
        return sharedPreferences.getString("api_key", "");
    }

    public void setApikey(String apikey) {
        System.setProperty("API_KEY",apikey);
        sharedPreferences.edit().putString("api_key", apikey).apply();
    }

    public String getGeminiModel() {
        return sharedPreferences.getString("gemini_model", "gemini-3.1-flash-lite");
    }

    public void setGeminiModel(String model) {
        sharedPreferences.edit().putString("gemini_model", model).apply();
        System.setProperty("MODEL_ID",model);
        if(modelChangedListener != null){
            modelChangedListener.onModelChanged(model);
        }
    }

    public String getModelUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + getGeminiModel() + ":generateContent?key=";
    }
}
