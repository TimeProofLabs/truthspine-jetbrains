package com.timeprooflabs.truthspine;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.StringReader;
import java.io.IOException;
import java.util.*;

/** Structural and scope validation only; this does not authenticate an executable. */
final class McpConfigurationContract {
    private McpConfigurationContract() {}

    private static Object read(JsonReader reader, int depth) throws IOException {
        if (depth > 12) throw new IOException("Configuration nesting limit");
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                Map<String,Object> object = new HashMap<>();
                reader.beginObject();
                while (reader.hasNext()) {
                    String key = reader.nextName();
                    if (object.containsKey(key)) throw new IOException("Duplicate configuration field");
                    object.put(key, read(reader, depth + 1));
                }
                reader.endObject(); return object;
            case BEGIN_ARRAY:
                List<Object> array = new ArrayList<>(); reader.beginArray();
                while (reader.hasNext()) array.add(read(reader, depth + 1));
                reader.endArray(); return array;
            case STRING: return reader.nextString();
            default: throw new IOException("Unexpected configuration value");
        }
    }

    static boolean valid(String json, String root) {
        if (json == null || json.length() > 65536 || root == null) return false;
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.setLenient(false);
            Object value = read(reader, 0);
            if (reader.peek() != JsonToken.END_DOCUMENT || !(value instanceof Map<?,?> config)) return false;
            if (!Set.of("mcpServers", "servers").containsAll(config.keySet())) return false;
            if (!(config.get("mcpServers") instanceof Map<?,?> servers) || !servers.keySet().equals(Set.of("truthspine"))) return false;
            if (config.containsKey("servers") && !servers.equals(config.get("servers"))) return false;
            if (!(servers.get("truthspine") instanceof Map<?,?> server)) return false;
            if (!(server.get("command") instanceof String command) || command.isBlank()) return false;
            if (server.containsKey("type") && !"stdio".equals(server.get("type"))) return false;
            if (!(server.get("args") instanceof List<?> args) || args.isEmpty() || args.stream().anyMatch(a -> !(a instanceof String))) return false;
            if (!(server.get("env") instanceof Map<?,?> env) || env.values().stream().anyMatch(v -> !(v instanceof String))) return false;
            // Owner's rule 2026-10-09: the entry follows the IDE's project folder (or finds the project); it never names
            // a project, so one entry serves every project the person opens.
            if (!"jetbrains".equals(env.get("TRUTHSPINE_HOST_ID"))) return false;
            // The setting (installed launcher) or, on the Mac App Store build, the relay's argument.
            if (!"1".equals(env.get("TRUTHSPINE_PROJECT_FROM_CWD")) && !"1".equals(env.get("TRUTHSPINE_FIND_PROJECT"))
                && !args.contains("--project-from-cwd") && !args.contains("--find-project")) return false;
            if (env.containsKey("TRUTHSPINE_PROJECT_ROOT") || env.containsKey("TRUTHSPINE_LOCK_PROJECT_ROOT")) return false;
            if (args.contains("--project-root")) return false;
            int hostFlag = args.indexOf("--host-id");
            if (hostFlag >= 0 && (hostFlag != args.lastIndexOf("--host-id") || hostFlag + 1 >= args.size()
                || !"jetbrains".equals(args.get(hostFlag + 1)))) return false;
            return true;
        } catch (Exception error) { return false; }
    }
}
