package dev.omar.aiagent.mcp.tool.file;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;

public class MoveFileTool extends BaseFileTool{
    public MoveFileTool(FileOperationsService fileService) {
        super(fileService);
    }

    @Override
    public String getName() {
        return "move_file";
    }

    @Override
    public String getDescription() {
        return "نقل ملف أو إعادة تسميته من مسار إلى مسار آخر.";
    }

    @Override
    public JSONObject getParametersSchema() {
        try {
            JSONObject props = new JSONObject();

            JSONObject src = new JSONObject();
            src.put("type", "STRING");
            src.put("description", "المسار الحالي للملف المراد نقله أو إعادة تسميته");
            props.put("source_path", src);

            JSONObject dest = new JSONObject();
            dest.put("type", "STRING");
            dest.put("description", "المسار الجديد للملف أو المجلد المستهدف");
            props.put("destination_path", dest);

            JSONObject overwrite = new JSONObject();
            overwrite.put("type", "BOOLEAN");
            overwrite.put("description", "هل يتم استبدال الملف في الوجهة إذا كان موجوداً؟ (افتراضياً false)");
            props.put("overwrite", overwrite);

            JSONObject schema = new JSONObject();
            schema.put("type", "OBJECT");
            schema.put("properties", props);
            schema.put("required", new JSONArray().put("source_path").put("destination_path"));
            return schema;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @Override
    public String execute(JSONObject args) {
        String src = args.optString("source_path");
        String dest = args.optString("destination_path");
        boolean overwrite = args.optBoolean("overwrite", false);
        return fileService.moveFile(src, dest, overwrite);
    }
}
