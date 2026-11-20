package com.live12skin.devices;

import bitwig.obf.TGU; // deserialized event tree (implements CuM)
import bitwig.obf.YBi; // virtual file handle
import bml.api.At;
import bml.api.Bml;
import bml.api.Hook;
import bml.api.Return;
import bml.api.This;
import com.bitwig.base.serial.Jkg; // serialization stream reader
import com.bitwig.flt.file.format.FloatDeviceContentsFileFormat; // .bwdevice serializer

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intercepts Bitwig's built-in .bwdevice resource streams and applies Live 12 scalar style edits
 * in memory using Bitwig's own FloatDeviceContentsFileFormat.
 */
public final class DeviceHooks {
    private static final Bml.Mod MOD = Bml.mod("live12-devices");
    private static final Map<String, Map<String, EditingSink.Edit>> EDITS = loadEdits();
    private static final ConcurrentHashMap<String, byte[]> CACHE = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> IN_HOOK = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static volatile boolean loggedFirst;

    private DeviceHooks() {}

    @Hook(id = "ybi-bwdevice-stream", target = YBi.class, method = "cfM()Ljava/io/InputStream;", at = At.WRAP_RETURN)
    public static InputStream onResourceStream(@Return InputStream stream, @This YBi file) {
        if (stream == null || file == null || EDITS.isEmpty() || Boolean.TRUE.equals(IN_HOOK.get())) {
            return stream;
        }
        String path = String.valueOf(file).replace('\\', '/');
        if (!path.endsWith(".bwdevice")) return stream;
        int idx = path.lastIndexOf("/devices/");
        if (idx < 0) return stream;
        String fileName = path.substring(idx + "/devices/".length());
        if (fileName.indexOf('/') >= 0) return stream;
        Map<String, EditingSink.Edit> devEdits = EDITS.get(fileName);
        if (devEdits == null || devEdits.isEmpty()) return stream;

        byte[] cached = CACHE.get(fileName);
        if (cached != null) {
            closeQuietly(stream);
            return new ByteArrayInputStream(cached);
        }

        IN_HOOK.set(Boolean.TRUE);
        byte[] stockBytes = null;
        try {
            stockBytes = stream.readAllBytes();
            closeQuietly(stream);
            byte[] patched = applyEdits(fileName, stockBytes, devEdits);
            if (patched != null) {
                CACHE.put(fileName, patched);
                if (!loggedFirst) {
                    loggedFirst = true;
                    MOD.log("Applied in-memory device panel edits to " + fileName + " (and up to " + EDITS.size() + " devices)");
                }
                return new ByteArrayInputStream(patched);
            }
        } catch (Throwable t) {
            MOD.log("Failed applying device edits to " + fileName + "; falling back to stock bytes.", t);
        } finally {
            IN_HOOK.set(Boolean.FALSE);
        }
        return stockBytes != null ? new ByteArrayInputStream(stockBytes) : stream;
    }

    private static byte[] applyEdits(String fileName, byte[] stockBytes, Map<String, EditingSink.Edit> devEdits) throws Exception {
        FloatDeviceContentsFileFormat fmt = FloatDeviceContentsFileFormat.fJR(); // singleton device format
        if (fmt == null) return null;
        Map<String, Object> meta = new TreeMap<>();
        Jkg reader = fmt.cfM(new ByteArrayInputStream(stockBytes), meta); // open reader + populate metadata
        TGU source = fmt.cfM(reader); // read serialized tree
        EditingSink.SourceAdapter adapter = new EditingSink.SourceAdapter(source, devEdits);
        ByteArrayOutputStream out = new ByteArrayOutputStream(stockBytes.length);
        fmt.cfM(adapter, out, new TreeMap<>(meta)); // re-serialize with edits applied
        if (adapter.mismatched > 0) {
            MOD.log(fileName + ": applied=" + adapter.applied + " mismatched=" + adapter.mismatched);
        }
        return out.toByteArray();
    }

    private static Map<String, Map<String, EditingSink.Edit>> loadEdits() {
        try {
            Path p = MOD.resource("device_edits");
            HashMap<String, Map<String, EditingSink.Edit>> map = new HashMap<>();
            int count = 0;
            try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] parts = line.split("\t");
                    if (parts.length < 4) continue;
                    map.computeIfAbsent(parts[0], k -> new HashMap<>())
                            .put(parts[1], new EditingSink.Edit(parts[2], parts[3]));
                    count++;
                }
            }
            MOD.log("Loaded " + count + " device edits across " + map.size() + " devices.");
            return Collections.unmodifiableMap(map);
        } catch (Throwable t) {
            MOD.log("Could not load device-edits.tsv", t);
            return Map.of();
        }
    }

    private static void closeQuietly(InputStream s) {
        try { s.close(); } catch (Throwable ignored) {}
    }
}
