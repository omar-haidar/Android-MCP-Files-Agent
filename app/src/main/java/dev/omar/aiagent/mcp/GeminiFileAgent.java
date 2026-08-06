package dev.omar.aiagent.mcp;


import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import dev.omar.aiagent.App;
import dev.omar.aiagent.mcp.context.AppContextProvider;
import dev.omar.aiagent.mcp.service.file.FileOperationsService;
import dev.omar.aiagent.mcp.tool.FileManagerToolRegistry;
import dev.omar.aiagent.mcp.tool.McpTool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiFileAgent {

    private static final String GEMINI_URL = App.getModelUrl();;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String apiKey;
    private final OkHttpClient httpClient;
    private final FileManagerToolRegistry toolRegistry;
    private final ChatSessionManager chatSession;

    private final AppContextProvider contextProvider;
    private String systemInstruction = "أنت مساعد ذكي ومتكامل لإدارة الملفات. " +
            "يمكنك قراءة، إنشاء، إلحاق، حذف، البحث، عرض تفاصيل الملفات، بالإضافة إلى نسخ ونقل الملفات وإعادة تسميتها بين المجلدات. " +
            "عرض نتائج العمليات دائماً بتنسيق Markdown احترافي، مفرداً الجداول والرموز للتوضيح بأسلوب قريب لـ ChatGPT.";

    public interface AgentCallback {
        void onSuccess(String message);

        void onError(String error);
    }

    public GeminiFileAgent(Context context, String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient();
        this.toolRegistry = new FileManagerToolRegistry(new FileOperationsService());
        this.chatSession = new ChatSessionManager(10, true);
        this.contextProvider = new AppContextProvider(context);
        // بناء التعليمات الأساسية مع تضمين سياق البيئة المخصص
        this.systemInstruction = buildFullSystemInstruction();
    }

    private String buildFullSystemInstruction() {
        return "أنت مساعد ذكي ومتخصص لإدارة الملفات والعمليات داخل نظام أندرويد. 🤖\n\n" +
                contextProvider.buildAgentEnvironmentContext() + "\n" +
                "### 🎯 قواعد وإرشادات التعامل:\n" +
                "1. عندما يطلب المستخدم عمليات داخل التطبيق دون تحديد مسار، استخدم مسار الملفات الداخلية الافتراضي: `" + contextProvider.getInternalFilesDir() + "`.\n" +
                "2. يمكنك قراءة، إنشاء، تعديل، حذف، بحث، نقل، ونسخ الملفات باستخدام الأدوات المتاحة لديك (Tools).\n" +
                "3. اعرض جميع الإجابات والنتائج بتنسيق Markdown منظم وأنظف ما يكون (استخدم الجداول والقوائم المنسقة والأيقونات 📁📄).\n" +
                "4. عند مواجهة أي خطأ في الصلاحيات أو المسارات، وضح السبب للمستخدم بلغة عربية سليمة وأسلوب تفاعلي مريح.";
    }

    public String getSystemInstruction() {
        return systemInstruction;
    }


    public void setMemoryEnabled(boolean enable) {
        chatSession.setMemoryEnabled(enable);
    }

    public boolean isMemoryEnabled() {
        return chatSession.isMemoryEnabled();
    }

    public void processUserPrompt(String userPrompt, AgentCallback callback) {
        new Thread(() -> {
            try {
                // 1️⃣ إضافة رسالة المستخدم الجديدة إلى السجل التاريخي
                chatSession.addUserMessage(userPrompt);

                // 2️⃣ بناء الطلب الكامل بالاعتماد على سجل الذاكرة التراكمي
                JSONObject requestBody = buildGeminiRequestBody();

                // 3️⃣ إرسال الطلب لـ API
                String responseStr = sendPostRequest(GEMINI_URL + apiKey, requestBody.toString());
                JSONObject jsonResponse = new JSONObject(responseStr);

                // معالجة الأخطاء المحتملة
                if (jsonResponse.has("error")) {
                    callback.onError(jsonResponse.getJSONObject("error").optString("message"));
                    return;
                }

                JSONObject candidate = jsonResponse.getJSONArray("candidates").getJSONObject(0);
                JSONObject candidateContent = candidate.getJSONObject("content");

                // 4️⃣ إضافة استجابة الموديل إلى الذاكرة لحفظ السياق
                chatSession.addModelResponse(candidateContent);

                JSONArray parts = candidateContent.getJSONArray("parts");
                JSONObject firstPart = parts.getJSONObject(0);

                // 5️⃣ التحقق هل طلب الموديل استدعاء أداة (Tool Call)
                if (firstPart.has("functionCall")) {
                    JSONObject functionCall = firstPart.getJSONObject("functionCall");
                    String toolName = functionCall.getString("name");
                    JSONObject args = functionCall.getJSONObject("args");

                    McpTool tool = toolRegistry.getTool(toolName);
                    if (tool != null) {
                        // تنفيذ الأداة محلياً
                        String toolResult = tool.execute(args);

                        // إضافة نتيجة الأداة إلى الذاكرة السياقية
                        chatSession.addFunctionResponse(toolName, toolResult);

                        // إرسال السجل مجدداً للموديل ليصيغ إجابته النهائية بالاعتماد على نتيجة الأداة
                        String finalResponse = requestFinalModelResponse();
                        callback.onSuccess(finalResponse);
                    } else {
                        callback.onError("الأداة غير موجودة: " + toolName);
                    }
                } else if (firstPart.has("text")) {
                    callback.onSuccess(firstPart.getString("text"));
                }

            } catch (Exception e) {
                callback.onError("خطأ أثناء معالجة الطلب: " + e.getMessage());
            }
        }).start();
    }

    private JSONObject buildGeminiRequestBody() throws Exception {

        JSONObject systemInstructionObj = new JSONObject()
                .put("parts", new JSONArray().put(new JSONObject().put("text", systemInstruction)));

        return new JSONObject()
                .put("system_instruction", systemInstructionObj)
                .put("contents", chatSession.getHistoryAsJsonArray())
                .put("tools", toolRegistry.getToolsDeclarationAsJson());
    }

    private String requestFinalModelResponse() throws Exception {
        JSONObject requestBody = buildGeminiRequestBody();
        String responseStr = sendPostRequest(GEMINI_URL + apiKey, requestBody.toString());
        JSONObject jsonResponse = new JSONObject(responseStr);

        JSONObject candidateContent = jsonResponse.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content");

        // إضافة الإجابة النهائية المعتمدة على الـ Tool إلى الذاكرة
        chatSession.addModelResponse(candidateContent);

        return candidateContent.getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");
    }

    private String sendPostRequest(String url, String jsonPayload) throws IOException {
        RequestBody body = RequestBody.create(jsonPayload, JSON);
        Request request = new Request.Builder().url(url).post(body).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            throw new IOException("استجابة فارغة من الـ Server");
        }
    }

    public void resetChatSession() {
        chatSession.clearHistory();
    }
}