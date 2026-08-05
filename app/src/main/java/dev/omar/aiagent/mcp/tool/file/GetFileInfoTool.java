package dev.omar.aiagent.mcp.tool.file;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;

public class GetFileInfoTool extends BaseFileTool{
    public GetFileInfoTool(FileOperationsService fileService) {
        super(fileService);
    }

    @Override
    public String getName() {
        return "get_file_info";
    }

    @Override
    public String getDescription() {
        return "عرض معلومات وتفاصيل كاملة عن ملف أو مجلد معين (مثل الحجم، تاريخ التعديل، الصلاحيات، ونوع MIME).";
    }

    @Override
    public JSONObject getParametersSchema() {
        try {
            JSONObject props = new JSONObject();

            JSONObject path = new JSONObject();
            path.put("type", "STRING");
            path.put("description", "المسار المطلق للملف أو المجلد (مثال: /sdcard/Download/document.pdf)");
            props.put("file_path", path);

            JSONObject schema = new JSONObject();
            schema.put("type", "OBJECT");
            schema.put("properties", props);
            schema.put("required", new JSONArray().put("file_path"));
            return schema;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @Override
    public String execute(JSONObject args) {
        String path = args.optString("file_path");
        return fileService.getFileInfo(path);
    }
}
