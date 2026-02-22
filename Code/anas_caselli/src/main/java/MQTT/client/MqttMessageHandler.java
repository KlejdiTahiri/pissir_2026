package MQTT.client;

import MQTT.messages.AutomobileMsg;
import MQTT.messages.IncidenteMsg;
import MQTT.messages.InfrazioneVelocitaMsg;
import MQTT.messages.PagamentoMsg;
import com.google.gson.JsonObject;
import org.springframework.http.*;
import org.springframework.web.client.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MqttMessageHandler implements MessageHandler {
    private static final String BASE_URL = "https://localhost:4567";
    private final RestTemplate rest = new RestTemplate();
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Override
    public void handleMessage(String topic, AutomobileMsg msg) {
        if (msg == null) {
            System.out.println("⚠️ Messaggio MQTT nullo o non parsabile.");
            return;
        }


        if (topic.equals("casello/ingresso")) {
            handleIngresso(msg);
        } else if (topic.startsWith("casello/uscita/")) {
            String distanzaStr = topic.substring("casello/uscita/".length());
            int distanza = Integer.parseInt(distanzaStr);
            handleUscita(msg, distanza);
        } else if (topic.startsWith("casello/guasto")) {
            handleGuasto(msg);
        } else {
            System.out.println("⚠️ Topic non riconosciuto: " + topic);
        }
    }

    @Override
    public void handleMessage(String topic, IncidenteMsg msg) {
        System.out.println("🚨 [INCIDENTE] Messaggio incidente ricevuto dal topic: " + topic);

        // Estrazione targa dal topic se presente (opzionale)
        if (topic.startsWith("tratta/incidente/")) {
            String targaTopic = topic.substring("tratta/incidente/".length());
            if (!targaTopic.isEmpty()) {
                msg.setTarga(targaTopic);
                System.out.println("🔍 Targa estratta dal topic: " + targaTopic);
            }
        }

        handleIncidente(msg);
    }

    private void handleIncidente(IncidenteMsg msg) {
        if (!msg.isValid()) {
            System.out.println("❌ Messaggio incidente NON valido: " + msg);
            return;
        }

        System.out.println("💥 INCIDENTE rilevato:");
        System.out.println("  - ID Biglietto: " + msg.getIdBiglietto());
        System.out.println("  - Targa: " + msg.getTarga());
        System.out.println("  - CAP: " + msg.getCap());
        System.out.println("  - ID Casello: " + msg.getIdCasello());
        System.out.println("  - Direzione: " + msg.getDirezione());
        System.out.println("  - ID Tratta: " + msg.getIdTratta());
        System.out.println("  - Gravità: " + msg.getGravita());
        System.out.println("  - Descrizione: " + msg.getDescrizione());
        System.out.println("  - Timestamp: " + msg.getTimestamp());

        String jsonBody = String.format(Locale.ROOT,
                "{\"idTratta\":%d,\"descrizione\":\"%s\",\"gravita\":\"%s\",\"idBiglietto\":%d," +
                        "\"targa\":\"%s\",\"cap\":\"%s\",\"idCasello\":%d,\"direzione\":\"%s\",\"km\":%.2f}",
                msg.getIdTratta(),
                msg.getDescrizione(),
                msg.getGravita(),
                msg.getIdBiglietto(),
                msg.getTarga(),
                msg.getCap(),
                msg.getIdCasello(),
                msg.getDirezione(),
                0.0
        );

        String url = BASE_URL + "/incidente/" + msg.getIdTratta();

        boolean success = executeWithRetryPostJson(url, jsonBody, "incidente", msg.getTarga());
        logResult(success, "Incidente", msg.getTarga());
    }


    public void handleInfrazioneVelocita(String topic, InfrazioneVelocitaMsg msg) {
        if (msg == null || !msg.isValid()) {
            System.out.println("⚠️ InfrazioneVelocitaMsg nullo o non valido.");
            return;
        }

        System.out.printf("🚓 [INFRAZIONE] targa=%s v=%d vmax=%d km=%.1f €%.2f%n",
                msg.getTarga(), msg.getVelocitaRilevata(), msg.getVelocitaMassima(),
                msg.getKmRilevazione(), msg.getImporto());

        // JSON robusto (zero problemi di virgole/escape)
        JsonObject body = new JsonObject();
        body.addProperty("idBiglietto", msg.getIdBiglietto());
        body.addProperty("targa", msg.getTarga());

        body.addProperty("cap", msg.getCap());
        body.addProperty("idCasello", msg.getIdCasello());
        body.addProperty("direzione", msg.getDirezione());

        body.addProperty("idTratta", msg.getIdTratta());
        body.addProperty("kmRilevazione", msg.getKmRilevazione());
        body.addProperty("velocitaRilevata", msg.getVelocitaRilevata());
        body.addProperty("velocitaMassima", msg.getVelocitaMassima());
        body.addProperty("importo", msg.getImporto());
        body.addProperty("timestamp", msg.getTimestamp());

        String url = BASE_URL + "/infrazione/velocita";
        boolean ok = executeWithRetryPostJson(url, body.toString(), "infrazione velocita", msg.getTarga());
        logResult(ok, "InfrazioneVelocità", msg.getTarga());
    }

    private void handleIngresso(AutomobileMsg msg) {
        if (!isValidMessage(msg)) {
            System.out.println("❌ Messaggio di ingresso non valido: " + msg);
            return;
        }

        String url = String.format("%s/ingresso?targa=%s&cap=%s&idCasello=%d&direzione=%s",
                BASE_URL, msg.getTarga(), msg.getCap(), msg.getIdCasello(), msg.getDirezione());

        boolean success = executeWithRetry("POST", url, "ingresso", msg.getTarga());
        logResult(success, "Ingresso", msg.getTarga());
    }

    private void handleUscita(AutomobileMsg msg, int distanza) {
        if (!isValidMessage(msg)) {
            System.out.println("❌ Messaggio di uscita non valido: " + msg);
            return;
        }

        String url = String.format("%s/uscita?targa=%s&cap=%s&idCasello=%d&direzione=%s&distanza=%d&idBiglietto=%d",
                BASE_URL, msg.getTarga(), msg.getCap(), msg.getIdCasello(),
                msg.getDirezione(), distanza, msg.getIdBiglietto());

        boolean success = executeWithRetry("POST", url, "uscita", msg.getTarga());
        logResult(success, "Uscita", msg.getTarga());
    }

    private void handleGuasto(AutomobileMsg msg) {
        if (msg.getCap() == null || msg.getIdCasello() <= 0 || msg.getDirezione() == null) {
            System.out.println("❌ Messaggio di guasto non valido: " + msg);
            return;
        }

        String url = String.format("%s/api/caselloGuasto?cap=%s&idCasello=%d&direzione=%s",
                BASE_URL, msg.getCap(), msg.getIdCasello(), msg.getDirezione());

        String identifier = msg.getCap() + "-" + msg.getIdCasello() + "-" + msg.getDirezione();
        boolean success = executeWithRetry("PUT", url, "guasto casello", identifier);
        logResult(success, "Guasto casello", identifier);
    }

    private boolean executeWithRetry(String method, String url, String operationType, String identifier) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                System.out.printf("🔄 Tentativo %d/%d per %s: %s%n", attempt, MAX_RETRY_ATTEMPTS, operationType, identifier);

                HttpMethod httpMethod = HttpMethod.valueOf(method);
                ResponseEntity<String> response = rest.exchange(url, httpMethod, HttpEntity.EMPTY, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("✅ " + operationType + " completata con successo (tentativo " + attempt + ")");
                    return true;
                }

                System.out.println("⚠️ Risposta non successful: " + response.getStatusCode());

            } catch (HttpClientErrorException e) {
                System.out.println("❗ Errore client (4xx): " + e.getStatusCode());
                if (e.getStatusCode().is4xxClientError()) return false;

            } catch (HttpServerErrorException e) {
                System.out.println("❗ Errore server (5xx): " + e.getStatusCode());

            } catch (RestClientException e) {
                System.out.println("❗ Errore di rete: " + e.getMessage());
                if (e.getMessage().contains("Connection refused")) {
                    System.out.println("🔌 Server non raggiungibile su " + BASE_URL);
                }

            } catch (Exception e) {
                System.out.println("❗ Errore generico: " + e.getMessage());
            }

            if (attempt < MAX_RETRY_ATTEMPTS) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }



    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static String money(double v) {
        // forza il punto decimale sempre (80.00) anche su locale IT
        return String.format(Locale.ROOT, "%.2f", v);
    }

    public void handlePagamento(String topic, PagamentoMsg msg) {
        if (msg == null) {
            System.out.println("⚠️ PagamentoMsg nullo.");
            return;
        }

        System.out.printf("💳 [PAGAMENTO] idBiglietto=%d targa=%s metodo=%s pagato=%s importo=%.2f distanza=%d%n",
                msg.getIdBiglietto(), msg.getTarga(), msg.getMetodo(), msg.isPagato(),
                msg.getImporto(), msg.getDistanza());

        String url = String.format(
                "%s/pagamento?targa=%s&importo=%s&metodo=%s&statoPagamento=%d&cap=%s&idCasello=%d&direzione=%s",
                BASE_URL,
                enc(msg.getTarga()),
                money(msg.getImporto()),
                enc(msg.getMetodo()),
                msg.isPagato() ? 1 : 0,
                enc(msg.getCapOut()),
                msg.getIdCaselloOut(),
                enc(msg.getDirezioneOut())
        );

        boolean success = executeWithRetry("POST", url, "pagamento", msg.getTarga());
        logResult(success, "Pagamento", msg.getTarga());

        if (!msg.isPagato()) {
            String urlEvasione = String.format(
                    "%s/evasione?idBiglietto=%d&targa=%s&importo=%s&metodo=%s&distanza=%d" +
                            "&capIn=%s&idCaselloIn=%d&direzioneIn=%s" +
                            "&capOut=%s&idCaselloOut=%d&direzioneOut=%s",
                    BASE_URL,
                    msg.getIdBiglietto(),
                    enc(msg.getTarga()),
                    money(msg.getImporto()),
                    enc(msg.getMetodo()),
                    msg.getDistanza(),
                    enc(msg.getCapIn()),
                    msg.getIdCaselloIn(),
                    enc(msg.getDirezioneIn()),
                    enc(msg.getCapOut()),
                    msg.getIdCaselloOut(),
                    enc(msg.getDirezioneOut())
            );

            executeWithRetry("POST", urlEvasione, "evasione", msg.getTarga());
            System.out.println("🚫 EVASIONE: apertura sbarra senza pagamento per targa " + msg.getTarga());
        }
    }

    private boolean executeWithRetryPostJson(String url, String jsonBody, String operationType, String identifier) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                System.out.printf("🔄 Tentativo %d/%d per %s: %s%n", attempt, MAX_RETRY_ATTEMPTS, operationType, identifier);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
                ResponseEntity<String> response = rest.exchange(url, HttpMethod.POST, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("✅ " + operationType + " completata con successo (tentativo " + attempt + ")");
                    return true;
                }

                System.out.println("⚠️ Risposta non successful: " + response.getStatusCode());

            } catch (HttpClientErrorException e) {
                System.out.println("❗ Errore client (4xx): " + e.getStatusCode());
                if (e.getStatusCode().is4xxClientError()) return false;

            } catch (HttpServerErrorException e) {
                System.out.println("❗ Errore server (5xx): " + e.getStatusCode());

            } catch (RestClientException e) {
                System.out.println("❗ Errore di rete: " + e.getMessage());
                if (e.getMessage().contains("Connection refused")) {
                    System.out.println("🔌 Server non raggiungibile su " + BASE_URL);
                }

            } catch (Exception e) {
                System.out.println("❗ Errore generico: " + e.getMessage());
            }

            if (attempt < MAX_RETRY_ATTEMPTS) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isValidMessage(AutomobileMsg msg) {
        return msg.getTarga() != null && !msg.getTarga().isBlank()
                && msg.getCap() != null && !msg.getCap().isBlank()
                && msg.getIdCasello() > 0
                && msg.getDirezione() != null && !msg.getDirezione().isBlank();
    }

    private void logResult(boolean success, String operation, String identifier) {
        if (success) {
            System.out.println("✅ " + operation + " registrato per: " + identifier);
        } else {
            System.out.println("❌ Fallimento nella registrazione del " + operation.toLowerCase() + " per: " + identifier);
        }
    }
}