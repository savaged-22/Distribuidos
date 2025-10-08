package com.acme.biblio.ga.store;

import com.acme.biblio.infra.JsonCodec;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class GaStore {
  private static final Logger log = LoggerFactory.getLogger(GaStore.class);

  private final Path baseDir;
  private final Path eventsFile;
  private final Path idemFile;
  private final Set<String> applied = new HashSet<>();

  public GaStore(Path baseDir) {
    this.baseDir = baseDir;
    this.eventsFile = baseDir.resolve("events.jsonl");
    this.idemFile   = baseDir.resolve("idempotency.log");
  }

  public void init() {
    try {
      Files.createDirectories(baseDir);
      if (Files.exists(idemFile)) {
        for (String line : Files.readAllLines(idemFile, StandardCharsets.UTF_8)) {
          String k = line.strip();
          if(!k.isEmpty()) applied.add(k);
        }
      }
      if (!Files.exists(eventsFile)) Files.createFile(eventsFile);
      log.info("GA store ready at {} ({} applied keys)", baseDir, applied.size());
    } catch (IOException e) {
      throw new RuntimeException("Init GA store failed: " + e.getMessage(), e);
    }
  }

  public synchronized boolean isApplied(String idempotencyKey) {
    return applied.contains(idempotencyKey);
  }

  public synchronized void markApplied(String idempotencyKey) {
    try (BufferedWriter w = Files.newBufferedWriter(idemFile, StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      w.write(idempotencyKey); w.newLine();
      applied.add(idempotencyKey);
    } catch (IOException e) {
      throw new RuntimeException("Persist idempotency failed: " + e.getMessage(), e);
    }
  }

  public synchronized void appendEvent(Object evt) {
    try (BufferedWriter w = Files.newBufferedWriter(eventsFile, StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      w.write(JsonCodec.toJson(evt)); w.newLine();
    } catch (IOException e) {
      throw new RuntimeException("Append event failed: " + e.getMessage(), e);
    }
  }

  public boolean writable() {
    try {
      Path probe = baseDir.resolve(".w");
      Files.writeString(probe, Instant.now().toString(), StandardCharsets.UTF_8,
          StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      Files.deleteIfExists(probe);
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
