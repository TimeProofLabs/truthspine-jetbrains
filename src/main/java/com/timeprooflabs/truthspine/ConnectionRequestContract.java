package com.timeprooflabs.truthspine;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

/** Request correlation only: hashes do not establish authorship or approval. */
final class ConnectionRequestContract {
    static final int MAX_BYTES = 32768;
    private static final Set<String> KEYS = Set.of("schema", "hostId", "wizardId", "projectRootHash", "createdAt", "expiresAt", "requestHash");
    private ConnectionRequestContract() {}

    static boolean valid(String json, String root, Instant now) {
        if (json == null || json.length() > MAX_BYTES || root == null) return false;
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.setLenient(false);
            HashMap<String,String> values = new HashMap<>();
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (!KEYS.contains(key) || values.containsKey(key) || reader.peek() != JsonToken.STRING) return false;
                values.put(key, reader.nextString());
            }
            reader.endObject();
            if (reader.peek() != JsonToken.END_DOCUMENT || !values.keySet().equals(KEYS)) return false;
            if (!"truthspine.jetbrains-connect-request.v1".equals(values.get("schema")) || !"jetbrains".equals(values.get("hostId"))) return false;
            String wizard = values.get("wizardId");
            if (wizard.isBlank() || wizard.length() > 256 || wizard.chars().anyMatch(c -> c < 32)) return false;
            if (!values.get("requestHash").matches("[a-f0-9]{64}")) return false;
            String normalized = Path.of(root).toAbsolutePath().normalize().toString().toLowerCase(Locale.ROOT);
            String rootHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(normalized.getBytes(StandardCharsets.UTF_8))).substring(0,16);
            if (!rootHash.equals(values.get("projectRootHash"))) return false;
            Instant created = Instant.parse(values.get("createdAt")), expires = Instant.parse(values.get("expiresAt"));
            return !created.isAfter(now) && expires.isAfter(now) && expires.isAfter(created) && !expires.isAfter(created.plusSeconds(600));
        } catch (Exception error) { return false; }
    }
}
