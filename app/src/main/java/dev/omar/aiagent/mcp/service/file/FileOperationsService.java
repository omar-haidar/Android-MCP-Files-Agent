package dev.omar.aiagent.mcp.service.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

public class FileOperationsService {
    // قائمة بالامتدادات النصية الشائعة التي قد لا يتعرف عليها المفسر الافتراضي كـ text/plain
    private static final List<String> TEXT_EXTENSIONS = Arrays.asList(
            "txt", "json", "xml", "java", "kt", "grad", "gradle", "md", "csv", "html", "css", "js", "log", "properties", "sh"
    );

    // 🛡️ قائمة المسارات الحساسة والمحظور حذفها نهائياً للحفاظ على النظام
    private static final List<String> PROTECTED_DIRECTORIES = Arrays.asList(
            "/",
            "/system",
            "/system/bin",
            "/system/etc",
            "/vendor",
            "/data",
            "/data/data",
            "/proc",
            "/sys",
            "/storage/emulated/0", // منع حذف المجلد الرئيسي بالكامل
            "/sdcard",            // منع حذف البطاقة بالكامل
            "/sdcard/Android"     // منع حذف مجلد بيانات التطبيقات
    );
    /**
     * 📋 نسخ ملف من مسار إلى مسار آخر
     */
    public String copyFile(String sourcePath, String destinationPath, boolean overwrite) {
        try {
            File src = new File(sourcePath);
            File dest = new File(destinationPath);

            // 1. التحقق من وجود المصدر
            if (!src.exists() || !src.isFile()) {
                return createErrorJson("ملف المصدر غير موجود أو أنه ليس ملفاً.");
            }

            // 2. إذا كان المسار المستهدف مجلداً، نقوم بدمج اسم الملف معه تلقائياً
            if (dest.isDirectory()) {
                dest = new File(dest, src.getName());
            }

            // 3. التحقق من الاستبدال
            if (dest.exists() && !overwrite) {
                return createErrorJson("الملف المستهدف موجود بالفعل. استخدم خيار overwrite = true للاستبدال.");
            }

            // 4. إنشاء المجلدات الأبوية إذا لم تكن موجودة
            File parent = dest.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            // 5. تنفيذ النسخ
            Path copied = Files.copy(
                    src.toPath(),
                    dest.toPath(),
                    overwrite ? new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new StandardCopyOption[]{}
            );

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("message", "تم نسخ الملف بنجاح 📋");
            result.put("source", src.getAbsolutePath());
            result.put("destination", copied.toAbsolutePath().toString());
            return result.toString();

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء عملية النسخ: " + e.getMessage());
        }
    }

    /**
     * 🚚 نقل (أو إعادة تسمية) ملف من مسار إلى مسار آخر
     */
    public String moveFile(String sourcePath, String destinationPath, boolean overwrite) {
        try {
            File src = new File(sourcePath);
            File dest = new File(destinationPath);

            // 1. التحقق من وجود المصدر
            if (!src.exists()) {
                return createErrorJson("المصدر المراد نقله غير موجود.");
            }

            // 2. إذا كان المسار المستهدف مجلداً، ندمج اسم الملف/المجلد معه
            if (dest.isDirectory()) {
                dest = new File(dest, src.getName());
            }

            // 3. التحقق من الاستبدال
            if (dest.exists() && !overwrite) {
                return createErrorJson("المسار المستهدف موجود بالفعل. استخدم خيار overwrite = true للاستبدال.");
            }

            // 4. إنشاء المجلدات الأبوية للوجهة
            File parent = dest.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            // 5. تنفيذ النقل
            Path moved = Files.move(
                    src.toPath(),
                    dest.toPath(),
                    overwrite ? new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new StandardCopyOption[]{}
            );

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("message", "تم نقل الملف بنجاح 🚚");
            result.put("source", src.getAbsolutePath());
            result.put("destination", moved.toAbsolutePath().toString());
            return result.toString();

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء عملية النقل: " + e.getMessage());
        }
    }
    /**
     * جلب التفاصيل الكاملة للملف أو المجلد
     */
    public String getFileInfo(String filePath) {
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                return createErrorJson("الملف أو المجلد غير موجود.");
            }

            JSONObject info = new JSONObject();
            info.put("status", "success");
            info.put("name", file.getName());
            info.put("path", file.getAbsolutePath());
            info.put("is_directory", file.isDirectory());
            info.put("is_file", file.isFile());
            info.put("is_hidden", file.isHidden());

            // 1. الحجم بـ Bytes والصيغة المنسقة (KB / MB / GB)
            long bytes = file.length();
            info.put("size_bytes", bytes);
            info.put("size_formatted", formatFileSize(bytes));

            // 2. تاريخ آخر تعديل
            long lastModifiedMillis = file.lastModified();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            info.put("last_modified", sdf.format(new Date(lastModifiedMillis)));

            // 3. الصلاحيات (Permissions)
            JSONObject permissions = new JSONObject();
            permissions.put("can_read", file.canRead());
            permissions.put("can_write", file.canWrite());
            permissions.put("can_execute", file.canExecute());
            info.put("permissions", permissions);

            // 4. نوع MIME Type إن كان ملفاً
            if (file.isFile()) {
                String mimeType = URLConnection.guessContentTypeFromName(file.getName());
                info.put("mime_type", mimeType != null ? mimeType : "unknown");
            } else if (file.isDirectory()) {
                String[] children = file.list();
                info.put("items_count", children != null ? children.length : 0);
            }

            return info.toString();

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء جلب تفاصيل الملف: " + e.getMessage());
        }
    }

    /**
     * 📏 تنسيق حجم الملف بشكل مقروء
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.US, "%.2f %cB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * البحث عن الملفات داخل مجلد محدد باستخدام جزء من الاسم أو الامتداد
     */
    public String searchFiles(String directoryPath, String query, String extension, int maxResults) {
        try {
            File dir = new File(directoryPath);

            // 1. التحقق من وجود المجلد
            if (!dir.exists() || !dir.isDirectory()) {
                return createErrorJson("المسار المحدد غير موجود أو ليس مجلداً.");
            }

            JSONArray results = new JSONArray();
            int limit = (maxResults > 0 && maxResults <= 100) ? maxResults : 50; // حد أقصى افتراضي 50 نتيجة

            // 2. بدء البحث التكراري
            performSearch(dir, query != null ? query.toLowerCase() : "",
                    extension != null ? extension.toLowerCase().replace(".", "") : "",
                    results, limit);

            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("directory", dir.getAbsolutePath());
            response.put("found_count", results.length());
            response.put("files", results);
            return response.toString();

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء البحث عن الملفات: " + e.getMessage());
        }
    }

    /**
     * 🔍 دالة البحث التكرارية (Recursive Search)
     */
    private void performSearch(File dir, String query, String extension, JSONArray results, int maxResults) {
        if (results.length() >= maxResults) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (results.length() >= maxResults) break;

            if (file.isDirectory()) {
                // تجنب البحث داخل مجلدات النظام المخفية المحمية لتسريع العملية
                if (!file.getName().startsWith(".")) {
                    performSearch(file, query, extension, results, maxResults);
                }
            } else {
                String fileName = file.getName().toLowerCase();

                // فحص شرط الاسم
                boolean matchesQuery = query.isEmpty() || fileName.contains(query);

                // فحص شرط الامتداد
                boolean matchesExt = extension.isEmpty() || fileName.endsWith("." + extension);

                if (matchesQuery && matchesExt) {
                    try {
                        JSONObject item = new JSONObject();
                        item.put("name", file.getName());
                        item.put("path", file.getAbsolutePath());
                        item.put("size_bytes", file.length());
                        results.put(item);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    /**
     * حذف ملف معين مع التحقق من معايير الأمان
     */
    public String deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            String canonicalPath = file.getCanonicalPath();

            // 1. التحقق من وجود الملف
            if (!file.exists()) {
                return createErrorJson("الملف المراد حذفه غير موجود.");
            }

            // 2. فحص الأمان: منع حذف المجلدات الجذرية والحساسة
            if (isProtectedPath(canonicalPath)) {
                return createErrorJson("⚠️ خطأ أمان: هذا المسار مجلد رئيسي/نظام وحذفه محظور لحماية النظام.");
            }

            // 3. منع حذف المجلدات إذا كانت تحتوي على عناصر أو لحصر الأداة في الملفات
            if (file.isDirectory()) {
                String[] children = file.list();
                if (children != null && children.length > 0) {
                    return createErrorJson("⚠️ لا يمكن حذف المجلد لأنه غير فارغ. (الحذف مقتصر على الملفات الفردية).");
                }
            }

            // 4. تنفيذ الحذف
            boolean deleted = file.delete();

            if (deleted) {
                JSONObject result = new JSONObject();
                result.put("status", "success");
                result.put("message", "تم حذف الملف بنجاح 🗑️");
                result.put("file_path", canonicalPath);
                return result.toString();
            } else {
                return createErrorJson("تعذر حذف الملف. قد لا تتوفر صلاحيات الكافية.");
            }

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء عملية الحذف: " + e.getMessage());
        }
    }

    /**
     * 🛡️ دالة التحقق من المسارات المحمية
     */
    private boolean isProtectedPath(String canonicalPath) {
        for (String protectedPath : PROTECTED_DIRECTORIES) {
            if (canonicalPath.equalsIgnoreCase(protectedPath) ||
                    canonicalPath.equalsIgnoreCase(protectedPath + "/")) {
                return true;
            }
        }
        return false;
    }
    /**
     * إلحاق محتوى نصي جديد في نهاية الملف بترميز UTF-8 دون مسح المحتوى القديم
     */
    public String appendToFileContent(String filePath, String content) {
        try {
            File file = new File(filePath);

            // 1. التحقق من وجود الملف
            if (!file.exists() || !file.isFile()) {
                return createErrorJson("الملف غير موجود. يرجى استخدام أداة create_file لإنشاء ملف جديد أولاً.");
            }

            // 2. التحقق من إمكانية الكتابة
            if (!file.canWrite()) {
                return createErrorJson("لا توجد صلاحيات كتابة على هذا الملف.");
            }

            // 3. الإلحاق بترميز UTF-8 (تعديل القيمة إلى true في FileOutputStream يضمن الإلحاق)
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file, true), StandardCharsets.UTF_8)) {

                // إضافة سطر جديد قبل المحتوى الملحق لضمان التنسيق إن لم يكن موجوداً
                writer.write("\n" + (content != null ? content : ""));
            }

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("message", "تم إلحاق المحتوى بالملف بنجاح 📝");
            result.put("file_path", file.getAbsolutePath());
            result.put("new_size_bytes", file.length());
            return result.toString();

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء الإلحاق بالملف: " + e.getMessage());
        }
    }

    /**
     * إنشاء ملف جديد بكتابة المحتوى المطلوب بترميز UTF-8
     */
    public String createFileWithContent(String filePath, String content, boolean overwrite) {
        try {
            File file = new File(filePath);

            // التحقق مما إذا كان الملف موجوداً مسبقاً
            if (file.exists() && !overwrite) {
                return createErrorJson("الملف موجود بالفعل. إذا كنت تريد استبداله يرجى إرسال overwrite = true");
            }

            // إنشاء المجلدات الأبوية إن لم تكن موجودة
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    return createErrorJson("تعذر إنشاء المجلدات المطلوبة للمسار المحدد.");
                }
            }

            // كتابة المحتوى بترميز UTF-8
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
                writer.write(content != null ? content : "");
            }

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("message", "تم إنشاء الملف بنجاح 📝");
            result.put("file_path", file.getAbsolutePath());
            result.put("size_bytes", file.length());
            return result.toString();

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء إنشاء الملف: " + e.getMessage());
        }
    }
    /**
     * قراءة محتوى ملف نصي بشرط ألا يتجاوز الحجم 1.5 ميغابايت وأن يكون ملفاً نصياً غير ثنائي
     */
    public String readFileContent(String filePath) {
        long maxSizeBytes = (long) (1.5 * 1024 * 1024); // 1.5 Megabytes

        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                return createErrorJson("الملف غير موجود أو المسار يرمز إلى مجلد وليس ملفاً.");
            }

            // 1. فحص نوع الملف (MIME Type) أولاً
            if (!isTextFile(file)) {
                return createErrorJson("عذراً، الملف ثنائي (Binary/Media) ولا يمكن قراءته كنص. يسمح فقط بقراءة الملفات النصية.");
            }

            // 2. فحص الحجم
            if (file.length() > maxSizeBytes) {
                double fileSizeMb = file.length() / (1024.0 * 1024.0);
                return createErrorJson(String.format(
                        "حجم الملف (%.2f MB) يتجاوز الحد الأقصى المسموح به (1.5 MB).",
                        fileSizeMb
                ));
            }

            // 3. قراءة المحتوى بترميز UTF-8
            StringBuilder contentBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
            }

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("file_path", filePath);
            result.put("size_bytes", file.length());
            result.put("content", contentBuilder.toString());
            return result.toString();

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء قراءة الملف: " + e.getMessage());
        }
    }

    /**
     * 🔍 التحقق الذكي من أن الملف نصي وقابل للقراءة
     */
    private boolean isTextFile(File file) {
        String fileName = file.getName().toLowerCase();

        // أ) التحقق أولاً عن طريق امتداد الملف (Extension Check)
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot != -1) {
            String extension = fileName.substring(lastDot + 1);
            if (TEXT_EXTENSIONS.contains(extension)) {
                return true;
            }
        }

        // ب) التحقق عن طريق URLConnection MIME Type
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType != null) {
            return mimeType.startsWith("text/") ||
                    mimeType.contains("json") ||
                    mimeType.contains("xml") ||
                    mimeType.contains("javascript");
        }

        // ج) فحص احتياطي (Fallback): التحقق من عدم وجود محارف ثنائية (Null Bytes) في البداية
        return isTextContentByBytes(file);
    }

    /**
     * 🛡️ فحص أعمق لشرائح البايت الأولى من الملف للتأكد من خلوه من Null Bytes (معيار شائع للملفات الثنائية)
     */
    private boolean isTextContentByBytes(File file) {
        byte[] buffer = new byte[512];
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(buffer);
            for (int i = 0; i < bytesRead; i++) {
                // البايت 0x00 يدل في معظم الأحوال على ملف ثنائي (Binary)
                if (buffer[i] == 0) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String createErrorJson(String message) {
        try {
            JSONObject err = new JSONObject();
            err.put("status", "error");
            err.put("message", message);
            return err.toString();
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + message + "\"}";
        }
    }

    /**
     * عرض قائمة الملفات والمجلدات داخل مسار محدد
     */
    public String listFiles(String folderPath) {
        try {
            File dir = new File(folderPath);
            if (!dir.exists() || !dir.isDirectory()) {
                return createErrorJson("المسار غير موجود أو أنه ليس مجلداً.");
            }

            File[] files = dir.listFiles();
            JSONArray jsonArray = new JSONArray();

            if (files != null) {
                for (File f : files) {
                    JSONObject item = new JSONObject();
                    item.put("name", f.getName());
                    item.put("path", f.getAbsolutePath());
                    item.put("is_directory", f.isDirectory());
                    item.put("size_bytes", f.length());
                    jsonArray.put(item);
                }
            }

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("path", folderPath);
            result.put("items", jsonArray);
            return result.toString();

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء عرض الملفات: " + e.getMessage());
        }
    }

    /**
     * حذف ملف أو مجلد (بما فيه من محتويات)
     */
    public String deleteFileOrFolder(String targetPath) {
        try {
            File target = new File(targetPath);
            if (!target.exists()) {
                return createErrorJson("الملف أو المجلد المطلوب حذفه غير موجود.");
            }

            boolean deleted = deleteRecursive(target);
            JSONObject result = new JSONObject();
            result.put("status", deleted ? "success" : "failed");
            result.put("message", deleted ? "تم الحذف بنجاح." : "فشل حذف العنصر.");
            return result.toString();

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء الحذف: " + e.getMessage());
        }
    }

    /**
     * إعادة تسمية ملف أو مجلد
     */
    public String renameFileOrFolder(String currentPath, String newName) {
        try {
            File target = new File(currentPath);
            if (!target.exists()) {
                return createErrorJson("الملف أو المجلد المراد تغيير اسمه غير موجود.");
            }

            File destination = new File(target.getParent(), newName);
            boolean renamed = target.renameTo(destination);

            JSONObject result = new JSONObject();
            result.put("status", renamed ? "success" : "failed");
            result.put("new_path", destination.getAbsolutePath());
            return result.toString();

        } catch (Exception e) {
            return createErrorJson("خطأ أثناء إعادة التسمية: " + e.getMessage());
        }
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }
/*
    private String createErrorJson(String message) {
        try {
            JSONObject err = new JSONObject();
            err.put("status", "error");
            err.put("message", message);
            return err.toString();
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"" + message + "\"}";
        }
    }*/
}
