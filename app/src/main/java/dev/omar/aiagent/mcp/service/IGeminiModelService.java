package dev.omar.aiagent.mcp.service;

import java.util.List;

import dev.omar.aiagent.mcp.model.GeminiModel;

public interface IGeminiModelService {
    interface ModelCallback {
        void onSuccess(List<GeminiModel> models, String markdownFormattedResult);

        void onError(String errorMessage);
    }

    void fetchAvailableModels(ModelCallback callback);

    // 🎯 دالة جلب النماذج الداعمة لـ Function Calling فقط
    void fetchFunctionCallingModels(ModelCallback callback);
}