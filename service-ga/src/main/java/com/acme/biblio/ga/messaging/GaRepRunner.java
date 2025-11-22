package com.acme.biblio.ga.messaging;

import com.acme.biblio.ga.domain.GaOutbox;
import com.acme.biblio.ga.repository.GaOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Component
public class GaRepRunner {

  private static final Logger log = LoggerFactory.getLogger(GaRepRunner.class);

  private final GaOutboxRepository outboxRepo;
  private final RestTemplate restTemplate = new RestTemplate();

  // URL base del GA de la sede B (ej: http://ga-b:8080)
  private final String replicaBaseUrl;

  // Flag para activar/desactivar la réplica (solo true en Sede A)
  private final boolean replicaEnabled;

  public GaRepRunner(
      GaOutboxRepository outboxRepo,
      @Value("${ga.replica.base-url:}") String replicaBaseUrl,
      @Value("${ga.replica.enabled:false}") boolean replicaEnabled
  ) {
    this.outboxRepo = outboxRepo;
    this.replicaBaseUrl = replicaBaseUrl;
    this.replicaEnabled = replicaEnabled;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onReady() {
    if (!replicaEnabled) {
      log.info("GA replica deshabilitada (ga.replica.enabled=false)");
    } else if (replicaBaseUrl == null || replicaBaseUrl.isBlank()) {
      log.warn("GA replica habilitada pero ga.replica.base-url NO está configurada");
    } else {
      log.info("GA replica habilitada. Enviando comandos a {}", replicaBaseUrl);
    }
  }

  /**
   * Lee periódicamente el GA_OUTBOX y envía los comandos pendientes a la sede B.
   */
  @Scheduled(fixedDelayString = "${ga.replica.poll-interval-ms:2000}")
  public void processOutbox() {
    if (!replicaEnabled || replicaBaseUrl == null || replicaBaseUrl.isBlank()) {
      return;
    }

    List<GaOutbox> all = outboxRepo.findAll();

    for (GaOutbox entry : all) {
      // Solo procesamos los que aún no han sido enviados
      if (entry.getProcessedAt() != null) {
        continue;
      }

      String eventType = entry.getEventType();
      String url = resolveUrlFor(eventType);
      if (url == null) {
        log.warn("No se encontró URL de réplica para eventType={}, se omite", eventType);
        continue;
      }

      try {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(entry.getPayload(), headers);

        // Enviar JSON del PrestamoCmd a la sede B
        restTemplate.postForEntity(url, entity, Void.class);

        // Marcar como procesado
        entry.setProcessedAt(LocalDate.now());
        outboxRepo.save(entry);

        log.info("Réplica enviada a {} para eventType={} id={}",
            url, eventType, entry.getId());
      } catch (Exception e) {
        log.error("Error replicando eventType={} id={} hacia sede B",
            eventType, entry.getId(), e);
      }
    }
  }

  /**
   * Mapea el tipo de evento al endpoint remoto.
   * Por ahora solo manejamos PrestamoCmd → /api/commands/prestamo en la sede B.
   */
  private String resolveUrlFor(String eventType) {
    if ("PrestamoCmd".equals(eventType)) {
      return replicaBaseUrl + "/api/commands/prestamo";
    }
    // Aquí a futuro puedes mapear otros comandos/eventos (RenovacionCmd, DevolucionCmd, etc.)
    return null;
  }

  @Bean
  public HealthIndicator gaHealth() {
    return () -> {
      Health.Builder builder = Health.up();
      builder.withDetail("replicaEnabled", replicaEnabled);
      builder.withDetail("replicaBaseUrl", replicaBaseUrl);
      return builder.build();
    };
  }
}
