package dev.omar.aiagent.mcp.service;


import dev.omar.aiagent.mcp.model.GeminiModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GeminiModelService implements IGeminiModelService {

    private static final String MODELS_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models?key=";
    private final String apiKey;
    private final OkHttpClient client;
    public GeminiModelService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    @Override
    public void fetchAvailableModels(ModelCallback callback) {
        executeFetch(false, callback);
    }

    @Override
    public void fetchFunctionCallingModels(ModelCallback callback) {
        executeFetch(true, callback);
    }

    private void executeFetch(boolean filterFunctionCallingOnly, ModelCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(MODELS_ENDPOINT + apiKey)
                        .get()
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        callback.onError("فشل جلب النماذج، كود الاستجابة: " + response.code());
                        return;
                    }

                    String jsonResponse = response.body().string();
                    List<GeminiModel> allModels = parseModelsJson(jsonResponse);

                    List<GeminiModel> resultModels;
                    if (filterFunctionCallingOnly) {
                        resultModels = filterSupportedFunctionCallingModels(allModels);
                    } else {
                        resultModels = allModels;
                    }

                    String markdownOutput = generateMarkdownReport(resultModels, filterFunctionCallingOnly);
                    callback.onSuccess(resultModels, markdownOutput);
                }

            } catch (Exception e) {
                callback.onError("حدث خطأ أثناء الاتصال: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 🔍 معيار الفلترة: التحقق من دعم generateContent واستبعاد نماذج الـ Embeddings والتضمين
     */
    private List<GeminiModel> filterSupportedFunctionCallingModels(List<GeminiModel> models) {
        List<GeminiModel> filteredList = new ArrayList<>();

        for (GeminiModel model : models) {
            List<String> methods = model.getSupportedGenerationMethods();
            String name = model.getName().toLowerCase();

            // النماذج التي تدعم Function Calling تكون ضمن فئة generateContent
            // ونستبعد النماذج المخصصة للـ Text Embedding أو AQA حصراً
            boolean supportsGenerate = methods.contains("generateContent");
            boolean isEmbeddingOrSpecial = name.contains("embedding") || name.contains("aqa");

            if (supportsGenerate && !isEmbeddingOrSpecial) {
                filteredList.add(model);
            }
        }

        return filteredList;
    }

    private List<GeminiModel> parseModelsJson(String jsonStr) throws Exception {
        List<GeminiModel> modelList = new ArrayList<>();
        JSONObject rootObj = new JSONObject(jsonStr);

        if (rootObj.has("models")) {
            JSONArray modelsArray = rootObj.getJSONArray("models");

            for (int i = 0; i < modelsArray.length(); i++) {
                JSONObject item = modelsArray.getJSONObject(i);

                String name = item.optString("name", "");
                String displayName = item.optString("displayName", "نموذج بدون اسم");
                String description = item.optString("description", "لا يوجد وصف متوفر.");

                List<String> methods = new ArrayList<>();
                if (item.has("supportedGenerationMethods")) {
                    JSONArray methodsArray = item.getJSONArray("supportedGenerationMethods");
                    for (int j = 0; j < methodsArray.length(); j++) {
                        methods.add(methodsArray.getString(j));
                    }
                }

                modelList.add(new GeminiModel(name, displayName, description, methods));
            }
        }
        return modelList;
    }

    private String generateMarkdownReport(List<GeminiModel> models, boolean isFiltered) {
        StringBuilder markdown = new StringBuilder();

        if (isFiltered) {
            markdown.append("# 🛠️ النماذج الداعمة لـ Function Calling & MCP\n\n");
            markdown.append("تم فلترة **").append(models.size()).append("** نموذج متوافق مع استدعاء الأدوات للربط في تطبيقك:\n\n");
        } else {
            markdown.append("# 🚀 جميع النماذج المتاحة في Gemini API\n\n");
            markdown.append("تم العثور على **").append(models.size()).append("** نموذج/نماذج متاحة:\n\n");
        }

        markdown.append("---\n\n");

        for (GeminiModel model : models) {
            markdown.append(model.toMarkdown());
        }

        return markdown.toString();
    }
}
