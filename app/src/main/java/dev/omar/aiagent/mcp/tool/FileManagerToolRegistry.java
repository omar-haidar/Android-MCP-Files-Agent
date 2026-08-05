package dev.omar.aiagent.mcp.tool;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;

public class FileManagerToolRegistry {

    private final Map<String, McpTool> toolsMap = new HashMap<>();
    private final FileOperationsService fileService;

    public FileManagerToolRegistry(FileOperationsService fileService) {
        this.fileService = fileService;
        registerDefaultTools();
    }

    private void registerDefaultTools() {
        // 1. أداة نسخ الملفات
        registerTool(new McpTool() {
            @Override
            public String getName() {
                return "copy_file";
            }

            @Override
            public String getDescription() {
                return "نسخ ملف من مسار محدد إلى مسار أو مجلد آخر.";
            }

            @Override
            public JSONObject getParametersSchema() {
                try {
                    JSONObject props = new JSONObject();

                    JSONObject src = new JSONObject();
                    src.put("type", "STRING");
                    src.put("description", "مسار الملف المصدر (مثل /sdcard/Download/test.txt)");
                    props.put("source_path", src);

                    JSONObject dest = new JSONObject();
                    dest.put("type", "STRING");
                    dest.put("description", "مسار الملف الوجهة أو المجلد المستهدف (مثل /sdcard/Documents)");
                    props.put("destination_path", dest);

                    JSONObject overwrite = new JSONObject();
                    overwrite.put("type", "BOOLEAN");
                    overwrite.put("description", "هل يتم استبدال الملف في حال وجوده بالوجهة؟ (افتراضياً false)");
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
                return fileService.copyFile(src, dest, overwrite);
            }
        });

// 2. أداة نقل الملفات
        registerTool(new McpTool() {
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
        });
// أداة جلب تفاصيل ومعلومات الملف
        registerTool(new McpTool() {
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
        });
// أداة البحث عن الملفات
        registerTool(new McpTool() {
            @Override
            public String getName() {
                return "search_files";
            }

            @Override
            public String getDescription() {
                return "البحث عن الملفات داخل مجلد محدد بأسماء أو امتدادات معينة (مثال: البحث عن جميع ملفات pdf أو ملف باسم معين).";
            }

            @Override
            public JSONObject getParametersSchema() {
                try {
                    JSONObject props = new JSONObject();

                    JSONObject dirPath = new JSONObject();
                    dirPath.put("type", "STRING");
                    dirPath.put("description", "مسار المجلد المُراد البحث داخله (مثال: /sdcard/Download)");
                    props.put("directory_path", dirPath);

                    JSONObject query = new JSONObject();
                    query.put("type", "STRING");
                    query.put("description", "جزء من اسم الملف المراد البحث عنه (اختياري)");
                    props.put("query", query);

                    JSONObject extension = new JSONObject();
                    extension.put("type", "STRING");
                    extension.put("description", "امتداد الملف للفلترة مثل: pdf, json, java, txt (اختياري)");
                    props.put("extension", extension);

                    JSONObject maxResults = new JSONObject();
                    maxResults.put("type", "INTEGER");
                    maxResults.put("description", "الحد الأقصى لعدد النتائج المرجعة (افتراضياً 50)");
                    props.put("max_results", maxResults);

                    JSONObject schema = new JSONObject();
                    schema.put("type", "OBJECT");
                    schema.put("properties", props);
                    schema.put("required", new JSONArray().put("directory_path"));
                    return schema;
                } catch (Exception e) {
                    return new JSONObject();
                }
            }

            @Override
            public String execute(JSONObject args) {
                String dirPath = args.optString("directory_path");
                String query = args.optString("query", null);
                String extension = args.optString("extension", null);
                int maxResults = args.optInt("max_results", 50);

                return fileService.searchFiles(dirPath, query, extension, maxResults);
            }
        });
        // أداة حذف ملف
        registerTool(new McpTool() {
            @Override
            public String getName() {
                return "delete_file";
            }

            @Override
            public String getDescription() {
                return "حذف ملف معين من الجهاز. تتضمن الأداة حماية تمنع حذف ملفات النظام والمجلدات الرئيسية.";
            }

            @Override
            public JSONObject getParametersSchema() {
                try {
                    JSONObject props = new JSONObject();

                    JSONObject path = new JSONObject();
                    path.put("type", "STRING");
                    path.put("description", "المسار المطلق للملف المراد حذفه (مثل /sdcard/Download/temp.txt)");
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
                return fileService.deleteFile(path);
            }
        });
// أداة إلحاق نص جديد في نهاية ملف موجود
        registerTool(new McpTool() {
            @Override
            public String getName() {
                return "append_to_file";
            }

            @Override
            public String getDescription() {
                return "إضافة أو إلحاق نص جديد في نهاية ملف موجود مسبقاً دون مسح أو تعديل بياناته القديمة.";
            }

            @Override
            public JSONObject getParametersSchema() {
                try {
                    JSONObject props = new JSONObject();

                    JSONObject path = new JSONObject();
                    path.put("type", "STRING");
                    path.put("description", "المسار المطلق للملف المراد الإلحاق به (مثل /sdcard/log.txt)");
                    props.put("file_path", path);

                    JSONObject content = new JSONObject();
                    content.put("type", "STRING");
                    content.put("description", "النص أو البيانات المراد إضافتها في نهاية الملف");
                    props.put("content", content);

                    JSONObject schema = new JSONObject();
                    schema.put("type", "OBJECT");
                    schema.put("properties", props);
                    schema.put("required", new JSONArray().put("file_path").put("content"));
                    return schema;
                } catch (Exception e) {
                    return new JSONObject();
                }
            }

            @Override
            public String execute(JSONObject args) {
                String path = args.optString("file_path");
                String content = args.optString("content");

                return fileService.appendToFileContent(path, content);
            }
        });
        // أداة إنشاء ملف جديد بمحتوى مخصص
        registerTool(new McpTool() {
            @Override
            public String getName() {
                return "create_file";
            }

            @Override
            public String getDescription() {
                return "إنشاء ملف نصي جديد (مثل txt, json, java, md) وتحديد محتواه بترميز UTF-8.";
            }

            @Override
            public JSONObject getParametersSchema() {
                try {
                    JSONObject props = new JSONObject();

                    JSONObject path = new JSONObject();
                    path.put("type", "STRING");
                    path.put("description", "المسار المطلق كاملاً شامل اسم الملف والامتداد (مثال: /sdcard/config.json)");
                    props.put("file_path", path);

                    JSONObject content = new JSONObject();
                    content.put("type", "STRING");
                    content.put("description", "المحتوى النصي المراد كتابته داخل الملف");
                    props.put("content", content);

                    JSONObject overwrite = new JSONObject();
                    overwrite.put("type", "BOOLEAN");
                    overwrite.put("description", "هل يتم استبدال الملف إن كان موجوداً من قبل (افتراضياً false)");
                    props.put("overwrite", overwrite);

                    JSONObject schema = new JSONObject();
                    schema.put("type", "OBJECT");
                    schema.put("properties", props);
                    schema.put("required", new JSONArray().put("file_path").put("content"));
                    return schema;
                } catch (Exception e) {
                    return new JSONObject();
                }
            }

            @Override
            public String execute(JSONObject args) {
                String path = args.optString("file_path");
                String content = args.optString("content");
                boolean overwrite = args.optBoolean("overwrite", false);

                return fileService.createFileWithContent(path, content, overwrite);
            }
        });
        // 4. أداة قراءة محتوى الملف
        registerTool(new McpTool() {
            @Override
            public String getName() {
                return "read_file_content";
            }

            @Override
            public String getDescription() {
                return "قراءة وعرض النص الداخلي لملف معين بشرط ألا يتجاوز حجمه 1.5 ميغابايت.";
            }

            @Override
            public JSONObject getParametersSchema() {
                try {
                    JSONObject props = new JSONObject();
                    JSONObject path = new JSONObject();
                    path.put("type", "STRING");
                    path.put("description", "المسار المطلق للملف المراد قراءة محتواه");
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
                return fileService.readFileContent(args.optString("file_path"));
            }
        });
        // 1. أداة عرض الملفات
        registerTool(new McpTool() {
            @Override
            public String getName() { return "list_files"; }

            @Override
            public String getDescription() { return "عرض قائمة الملفات والمجلدات الموجودة في مسار معين."; }

            @Override
            public JSONObject getParametersSchema() {
                try {
                    JSONObject props = new JSONObject();
                    JSONObject path = new JSONObject();
                    path.put("type", "STRING");
                    path.put("description", "المسار المطلق للمجلد المراد عرض محتوياته");
                    props.put("folder_path", path);

                    JSONObject schema = new JSONObject();
                    schema.put("type", "OBJECT");
                    schema.put("properties", props);
                    schema.put("required", new JSONArray().put("folder_path"));
                    return schema;
                } catch (Exception e) {
                    return new JSONObject();
                }
            }

            @Override
            public String execute(JSONObject args) {
                return fileService.listFiles(args.optString("folder_path"));
            }
        });

        // 2. أداة حذف ملف أو مجلد
        registerTool(new McpTool() {
            @Override
            public String getName() { return "delete_file_or_folder"; }

            @Override
            public String getDescription() { return "حذف ملف أو مجلد بالكامل بناءً على المسار الموفر."; }

            @Override
            public JSONObject getParametersSchema() {
                try {
                    JSONObject props = new JSONObject();
                    JSONObject path = new JSONObject();
                    path.put("type", "STRING");
                    path.put("description", "المسار المطلق للملف أو المجلد المراد حذفه");
                    props.put("target_path", path);

                    JSONObject schema = new JSONObject();
                    schema.put("type", "OBJECT");
                    schema.put("properties", props);
                    schema.put("required", new JSONArray().put("target_path"));
                    return schema;
                } catch (Exception e) { return new JSONObject(); }
            }

            @Override
            public String execute(JSONObject args) {
                return fileService.deleteFileOrFolder(args.optString("target_path"));
            }
        });

        // 3. أداة إعادة التسمية
        registerTool(new McpTool() {
            @Override
            public String getName() { return "rename_file_or_folder"; }

            @Override
            public String getDescription() { return "تغيير اسم ملف أو مجلد إلى اسم جديد."; }

            @Override
            public JSONObject getParametersSchema() {
                try {
                    JSONObject props = new JSONObject();

                    JSONObject currentPath = new JSONObject();
                    currentPath.put("type", "STRING");
                    currentPath.put("description", "المسار الحالي للملف أو المجلد");
                    props.put("current_path", currentPath);

                    JSONObject newName = new JSONObject();
                    newName.put("type", "STRING");
                    newName.put("description", "الاسم الجديد فقط بدون المسار كامل");
                    props.put("new_name", newName);

                    JSONObject schema = new JSONObject();
                    schema.put("type", "OBJECT");
                    schema.put("properties", props);
                    schema.put("required", new JSONArray().put("current_path").put("new_name"));
                    return schema;
                } catch (Exception e) { return new JSONObject(); }
            }

            @Override
            public String execute(JSONObject args) {
                return fileService.renameFileOrFolder(
                        args.optString("current_path"),
                        args.optString("new_name")
                );
            }
        });
    }

    public void registerTool(McpTool tool) {
        toolsMap.put(tool.getName(), tool);
    }

    public McpTool getTool(String name) {
        return toolsMap.get(name);
    }

    /**
     * بناء صيغة الأدوات لملاءمة طلب Gemini API
     */
    public JSONArray getToolsDeclarationAsJson() {
        JSONArray functionDeclarations = new JSONArray();
        try {
            for (McpTool tool : toolsMap.values()) {
                JSONObject decl = new JSONObject();
                decl.put("name", tool.getName());
                decl.put("description", tool.getDescription());
                decl.put("parameters", tool.getParametersSchema());
                functionDeclarations.put(decl);
            }
        } catch (Exception ignored) {}

        JSONArray tools = new JSONArray();
        try {
            JSONObject container = new JSONObject();
            container.put("function_declarations", functionDeclarations);
            tools.put(container);
        } catch (Exception ignored) {}

        return tools;
    }
}
