package dev.omar.aiagent.mcp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatSessionManager {

    private final List<JSONObject> conversationHistory;
    private int maxWindowSize;
    private boolean isMemoryEnabled;

    public ChatSessionManager(int maxWindowSize, boolean isMemoryEnabled) {
        this.conversationHistory = new ArrayList<>();
        this.maxWindowSize = maxWindowSize;
        this.isMemoryEnabled = isMemoryEnabled;
    }

    public ChatSessionManager() {
        this(10, true);
    }

    /**
     * ⚙️ تفعيل أو تعطيل الذاكرة
     */
    public void setMemoryEnabled(boolean memoryEnabled) {
        this.isMemoryEnabled = memoryEnabled;
        if (!memoryEnabled) {
            clearHistory();
        }
    }

    public boolean isMemoryEnabled() {
        return isMemoryEnabled;
    }

    public void setMaxWindowSize(int maxWindowSize) {
        this.maxWindowSize = maxWindowSize;
        trimToWindowSize();
    }

    public void addUserMessage(String text) {
        if (!isMemoryEnabled) {
            clearHistory();
        }

        try {
            JSONObject textPart = new JSONObject().put("text", text);
            JSONObject userContent = new JSONObject()
                    .put("role", "user")
                    .put("parts", new JSONArray().put(textPart));
            conversationHistory.add(userContent);

            if (isMemoryEnabled) {
                trimToWindowSize();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addModelResponse(JSONObject candidateContent) {
        if (!isMemoryEnabled) return;

        try {
            if (candidateContent.has("parts")) {
                JSONObject modelContent = new JSONObject()
                        .put("role", "model")
                        .put("parts", candidateContent.getJSONArray("parts"));
                conversationHistory.add(modelContent);
                trimToWindowSize();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addFunctionResponse(String toolName, String jsonResult) {

        try {
            JSONObject responseFunctionPart = new JSONObject()
                    .put("functionResponse", new JSONObject()
                            .put("name", toolName)
                            .put("response", new JSONObject().put("content", new JSONObject(jsonResult))));

            JSONObject functionContent = new JSONObject()
                    .put("role", "function")
                    .put("parts", new JSONArray().put(responseFunctionPart));

            conversationHistory.add(functionContent);
            if (isMemoryEnabled) {
                trimToWindowSize();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void trimToWindowSize() {
        if (conversationHistory.size() <= maxWindowSize) {
            return;
        }

        int startIndex = conversationHistory.size() - maxWindowSize;
        while (startIndex < conversationHistory.size()) {
            JSONObject content = conversationHistory.get(startIndex);
            String role = content.optString("role", "");
            if ("user".equalsIgnoreCase(role)) {
                break;
            }
            startIndex++;
        }

        if (startIndex > 0 && startIndex < conversationHistory.size()) {
            conversationHistory.subList(0, startIndex).clear();
        }
    }

    public JSONArray getHistoryAsJsonArray() {
        JSONArray array = new JSONArray();
        for (JSONObject content : conversationHistory) {
            array.put(content);
        }
        return array;
    }

    public void clearHistory() {
        conversationHistory.clear();
    }
}