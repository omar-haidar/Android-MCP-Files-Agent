package dev.omar.aiagent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import dev.omar.aiagent.databinding.ActivitySetApiKeyBinding;
import dev.omar.aiagent.ui.base.BaseActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SetApiKeyActivity extends BaseActivity {

    private ActivitySetApiKeyBinding binding;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetApiKeyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSave.setOnClickListener(v -> {
            String apiKey = binding.txtInputText.getText().toString().trim();
            if (apiKey.isEmpty()) {
                binding.txtInputLayout1.setError("Please enter API Key");
                return;
            }
            validateAndSaveApiKey(apiKey);
        });
        binding.btnCreateNew.setOnClickListener(v -> {
            String url = "https://aistudio.google.com/app/apikey";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }

    private void validateAndSaveApiKey(String apiKey) {
        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Checking...");

        // نستخدم الموديل الذي ذكرته للتحقق
        String url = App.getModelUrl() + apiKey;

        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject().put("text", "test");
            contents.put(new JSONObject().put("parts", new JSONArray().put(part)));
            jsonBody.put("contents", contents);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText("Save");
                    Toast.makeText(SetApiKeyActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    binding.btnSave.setEnabled(!response.isSuccessful());
                    binding.btnSave.setText("Save");
                    if (response.isSuccessful()) {
                        App.get().getConfigs().setApikey(apiKey);
                        Toast.makeText(SetApiKeyActivity.this, "API Key is valid and saved!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SetApiKeyActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SetApiKeyActivity.this, "Invalid API Key or Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
