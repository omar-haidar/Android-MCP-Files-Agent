package dev.omar.aiagent.mcp.model;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class GeminiModel {

    public static final DiffCallback DIFF_CALLBACK = new DiffCallback();
    private final String name;
    private final String displayName;
    private final String description;
    private final List<String> supportedGenerationMethods;

    public GeminiModel(String name, String displayName, String description, List<String> supportedGenerationMethods) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.supportedGenerationMethods = supportedGenerationMethods;
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public List<String> getSupportedGenerationMethods() { return supportedGenerationMethods; }

    /**
     * تحويل بيانات الموديل إلى صيغة Markdown المنسقة
     */
    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("### 🤖 ").append(displayName).append("\n");
        sb.append("* **🏷️ الاسم التقني:** `").append(name.replace("models/", "")).append("`\n");
        sb.append("* **📝 الوصف:** ").append(description).append("\n");
        sb.append("* **⚙️ القدرات المدعومة:** ").append(supportedGenerationMethods.toString()).append("\n\n");
        return sb.toString();
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<GeminiModel>{
        @Override
        public boolean areItemsTheSame(@NonNull GeminiModel oldItem, @NonNull GeminiModel newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull GeminiModel oldItem, @NonNull GeminiModel newItem) {
            return oldItem.name.equals(newItem.name);
        }
    }
}