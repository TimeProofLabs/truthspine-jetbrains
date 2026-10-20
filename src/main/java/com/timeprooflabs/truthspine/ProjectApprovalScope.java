package com.timeprooflabs.truthspine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/** Local scope continuity, not authentication of the person granting approval. */
final class ProjectApprovalScope {
    private ProjectApprovalScope() {}

    static String identity(String root) {
        if (root == null || root.isBlank()) return null;
        try {
            Path physical = Path.of(root).toRealPath();
            BasicFileAttributes attributes = Files.readAttributes(physical, BasicFileAttributes.class);
            if (!attributes.isDirectory()) return null;
            // Windows providers may omit fileKey. Retain a creation-time stamp
            // in that case; it is a change detector, not unforgeable identity.
            String stamp = attributes.fileKey() == null
                ? "created:" + attributes.creationTime()
                : "file-key:" + attributes.fileKey();
            return physical.toString() + "\n" + stamp;
        } catch (IOException | RuntimeException error) {
            return null;
        }
    }

    static boolean matches(String root, String approvedIdentity) {
        String current = identity(root);
        return current != null && current.equals(approvedIdentity);
    }
}
