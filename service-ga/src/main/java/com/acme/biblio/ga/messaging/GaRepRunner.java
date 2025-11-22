package com.acme.biblio.ga.messaging;

import com.acme.biblio.ga.domain.GaOutbox;
import com.acme.biblio.ga.repository.GaOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
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
public class GaRepRunner implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(GaRepRunner.class);

    private final GaOutboxRepository outboxRepo;
    private final RestTemplate restTemplate;

    // URL base del GA de la sede B (ej: http://localhost:8083)
    private final String replicaBaseUrl;

    // Flag para activar/desactivar la réplica (true solo en perfil ga / sede A)
    private final boolean replicaEnabled;

    public GaRepRunner(
            GaOutboxRepository outboxRepo,
            @Value("${ga.replica.base-url:}") String replicaBaseUrl,
            @Value("${ga.replica.enabled:false}") boolean replicaEnabled
    ) {
        this.outboxRepo = outboxRepo;
        this.restTemplate = new RestTemplate();
        this.replicaBaseUrl = replicaBaseUrl;
        this.replicaEnabled = replicaEnabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (!replicaEnabled) {
            log.info("GA replica DESHABILITADA (ga.replica.enabled=false)");
        } else if (replicaBaseUrl == null || replicaBaseUrl.isBlank()) {
            log.warn("GA replica habilitada pero ga.replica.base-url NO está configurada");
        } else {
            log.info("GA replica habilitada. Enviando comandos a baseUrl={}", replicaBaseUrl);
        }
    }

    /**
     * Lee periódicamente GA_OUTBOX y envía los comandos pendientes a la sede B.
     * Solo se ejecuta cuando ga.replica.enabled=true y ga.replica.base-url no está vacío.
     */
    @Scheduled(fixedDelayString = "${ga.replica.poll-interval-ms:2000}")
    public void processOutbox() {
        if (!replicaEnabled || replicaBaseUrl == null || replicaBaseUrl.isBlank()) {
            return;
        }

        // Simples por ahora: leemos todos y filtramos en memoria por processedAt
        List<GaOutbox> all = outboxRepo.findAll();

        for (GaOutbox entry : all) {
            // Solo procesar los eventos que aún no se han enviado
            if (entry.getProcessedAt() != null) {
                continue;
            }

            String eventType = entry.getEventType();
            String url = resolveUrlFor(eventType);
            if (url == null) {
                log.warn("No hay URL de réplica configurada para eventType={}, se omite id={}",
                        eventType, entry.getId());
                continue;
            }

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(entry.getPayload(), headers);

                // Enviar JSON del comando (ej: PrestamoCmd) a la sede B
                restTemplate.postForEntity(url, entity, Void.class);

                // Marcar como procesado
                entry.setProcessedAt(LocalDate.now());
                outboxRepo.save(entry);

                log.info("Réplica enviada OK a {} para eventType={} id={}",
                        url, eventType, entry.getId());
            } catch (Exception e) {
                log.error("Error replicando eventType={} id={} hacia sede B",
                        eventType, entry.getId(), e);
            }
        }
    }

    /**
     * Mapea el tipo de evento al endpoint remoto.
     * Por ahora: PrestamoCmd → /api/commands/prestamo en la sede B.
     */
    private String resolveUrlFor(String eventType) {
        if ("PrestamoCmd".equals(eventType)) {
            return replicaBaseUrl + "/api/commands/prestamo";
        }
        // Aquí luego puedes agregar otros comandos/eventos
        return null;
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        builder.withDetail("replicaEnabled", replicaEnabled);
        builder.withDetail("replicaBaseUrl", replicaBaseUrl);
        return builder.build();
    }
}
