package piko.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/** Small json file helpers shared by the config and profile managers. */
public final class FileUtil {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser PARSER = new JsonParser();

    private FileUtil() {
    }

    public static void ensureDirectory(File directory) {
        if (!directory.exists() && !directory.mkdirs()) {
            System.err.println("[Piko] Could not create directory " + directory.getAbsolutePath());
        }
    }

    public static JsonObject readJson(File file) {
        if (!file.exists() || file.length() == 0) {
            return null;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8));
            JsonElement element = PARSER.parse(reader);
            return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (Exception exception) {
            System.err.println("[Piko] Failed to read " + file.getName() + ": " + exception.getMessage());
            return null;
        } finally {
            close(reader);
        }
    }

    public static boolean writeJson(File file, JsonObject object) {
        Writer writer = null;
        try {
            ensureDirectory(file.getParentFile());
            writer = new OutputStreamWriter(new FileOutputStream(file), UTF8);
            GSON.toJson(object, writer);
            writer.flush();
            return true;
        } catch (Exception exception) {
            System.err.println("[Piko] Failed to write " + file.getName() + ": " + exception.getMessage());
            return false;
        } finally {
            close(writer);
        }
    }

    private static void close(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
                // Nothing useful can be done if closing fails.
            }
        }
    }

    /** Turns a profile name into something safe to use as a file name. */
    public static String sanitize(String name) {
        StringBuilder builder = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            if (Character.isLetterOrDigit(character) || character == '-' || character == '_' || character == ' ') {
                builder.append(character == ' ' ? '_' : character);
            }
        }
        String result = builder.toString();
        return result.isEmpty() ? "profile" : result;
    }
}
