package MQTT.client.publishers;

import MQTT.client.publishers.util.SSLTrust;
import MQTT.messages.PagamentoMsg;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class PublisherPagamento {

    private static final Gson gson = new Gson();
    private static final Random rnd = new Random();

    private static final String BROKER = "tcp://broker.emqx.io:1883";
    private static final String TOPIC_PREFIX = "casello/pagamento/";

    // probabilità simulazione
    private static final double P_TELEPASS = 0.45;
    private static final double P_CONTANTI = 0.45;
    private static final double P_EVASIONE = 0.10;

    /**
     * Pubblica un pagamento per un dato idBiglietto.
     * Legge entrata/uscita dai file e ricava prezzo dalla tabella tratte.
     */
    public static void publishPagamento(int idBiglietto, int distanza) throws Exception {

        InputStream isUscita = PublisherPagamento.class.getClassLoader().getResourceAsStream("pedaggi_uscita.txt");
        InputStream isEntrata = PublisherPagamento.class.getClassLoader().getResourceAsStream("pedaggi_entrata.txt");

        if (isUscita == null || isEntrata == null) {
            System.out.println("⚠️ pedaggi_uscita.txt o pedaggi_entrata.txt non trovato nel classpath!");
            return;
        }

        List<String> uscite = new BufferedReader(new InputStreamReader(isUscita)).lines().toList();
        List<String> entrate = new BufferedReader(new InputStreamReader(isEntrata)).lines().toList();

        // 1) uscita per idBiglietto
        Optional<JsonObject> uscitaOpt = uscite.stream()
                .map(r -> gson.fromJson(r, JsonObject.class))
                .filter(o -> o.has("idBiglietto") && o.get("idBiglietto").getAsInt() == idBiglietto)
                .findFirst();

        if (uscitaOpt.isEmpty()) {
            System.out.println("❌ Pagamento: uscita non trovata per idBiglietto=" + idBiglietto);
            return;
        }

        JsonObject uscita = uscitaOpt.get();

        // 2) entrata associata (targa + stessa data + idBiglietto)
        String targa = uscita.get("targa").getAsString();
        String dataSolo = uscita.get("timestamp").getAsString().substring(0, 10);

        Optional<JsonObject> entrataOpt = entrate.stream()
                .map(r -> gson.fromJson(r, JsonObject.class))
                .filter(o ->
                        o.has("targa") && targa.equals(o.get("targa").getAsString()) &&
                                o.has("timestamp") && o.get("timestamp").getAsString().startsWith(dataSolo) &&
                                o.has("idBiglietto") && o.get("idBiglietto").getAsInt() == idBiglietto
                ).findFirst();

        if (entrataOpt.isEmpty()) {
            System.out.println("❌ Pagamento: entrata non trovata per targa=" + targa + " idBiglietto=" + idBiglietto);
            return;
        }

        JsonObject entrata = entrataOpt.get();

        // 3) dati tratta
        String capIn = entrata.get("cap").getAsString();
        int idCaselloIn = entrata.get("idCasello").getAsInt();
        String dirIn = entrata.get("direzione").getAsString();

        String capOut = uscita.get("cap").getAsString();
        int idCaselloOut = uscita.get("idCasello").getAsInt();
        String dirOut = uscita.get("direzione").getAsString();

        // 4) lookup prezzo: prima match completo, poi fallback CAP->CAP
        Double prezzo = prezzoDaDb(capIn, idCaselloIn, dirIn, capOut, idCaselloOut, dirOut);

        if (prezzo == null) {
            prezzo = prezzoDaDbSoloCap(capIn, capOut);
        }

        if (prezzo == null) {
            System.out.println("⚠️ Pagamento: prezzo NON trovato in DB per:");
            System.out.println("   - match completo: " + capIn + "/" + idCaselloIn + "/" + dirIn + " -> " + capOut + "/" + idCaselloOut + "/" + dirOut);
            System.out.println("   - fallback CAP->CAP: " + capIn + " -> " + capOut);
            return;
        }

        // 5) scelta metodo pagamento
        MetodoEsito me = scegliMetodo();

        PagamentoMsg msg = new PagamentoMsg();
        msg.setIdBiglietto(idBiglietto);
        msg.setTarga(targa);

        msg.setCapIn(capIn);
        msg.setIdCaselloIn(idCaselloIn);
        msg.setDirezioneIn(dirIn);

        msg.setCapOut(capOut);
        msg.setIdCaselloOut(idCaselloOut);
        msg.setDirezioneOut(dirOut);

        msg.setDistanza(distanza);
        msg.setImporto(prezzo);

        msg.setMetodo(me.metodo);
        msg.setPagato(me.pagato);

        // 6) publish mqtt
        SSLTrust.trustAllCerts();
        MqttClient client = new MqttClient(BROKER, MqttClient.generateClientId());
        client.connect();

        String topic = TOPIC_PREFIX + idBiglietto;
        String payload = gson.toJson(msg);

        MqttMessage mqttMsg = new MqttMessage(payload.getBytes());
        mqttMsg.setQos(0);
        client.publish(topic, mqttMsg);

        System.out.printf("💳 Pagamento pubblicato su %s | %s | pagato=%s | €%.2f | targa=%s%n",
                topic, me.metodo, me.pagato, prezzo, targa);

        client.disconnect();
        client.close();
    }

    /**
     * Lookup prezzo con match completo (cap/id/dir -> cap/id/dir).
     */
    private static Double prezzoDaDb(String capIn, int idIn, String dirIn,
                                     String capOut, int idOut, String dirOut) {

        String sql =
                "SELECT prezzo FROM tratte " +
                        "WHERE UPPER(cap_in)=UPPER(?) AND id_casello_in=? AND UPPER(dir_in)=UPPER(?) " +
                        "  AND UPPER(cap_out)=UPPER(?) AND id_casello_out=? AND UPPER(dir_out)=UPPER(?) " +
                        "  AND stato=1 " +
                        "LIMIT 1";

        try (Connection conn = Dao.Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, capIn);
            ps.setInt(2, idIn);
            ps.setString(3, dirIn);
            ps.setString(4, capOut);
            ps.setInt(5, idOut);
            ps.setString(6, dirOut);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("prezzo");
            }

        } catch (Exception e) {
            System.out.println("❌ Errore DB lookup prezzo (match completo): " + e.getMessage());
        }
        return null;
    }

    /**
     * Fallback demo: se non trovi la tratta con id casello specifici,
     * prova solo CAP->CAP (prende una tratta qualsiasi attiva).
     */
    private static Double prezzoDaDbSoloCap(String capIn, String capOut) {

        String sql =
                "SELECT prezzo FROM tratte " +
                        "WHERE UPPER(cap_in)=UPPER(?) AND UPPER(cap_out)=UPPER(?) " +
                        "  AND UPPER(dir_in)='INGRESSO' AND UPPER(dir_out)='USCITA' " +
                        "  AND stato=1 " +
                        "ORDER BY prezzo DESC " +  // se ce ne sono più, ne prendi una “decisa”
                        "LIMIT 1";

        try (Connection conn = Dao.Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, capIn);
            ps.setString(2, capOut);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("prezzo");
            }

        } catch (Exception e) {
            System.out.println("❌ Errore DB lookup prezzo (fallback CAP->CAP): " + e.getMessage());
        }
        return null;
    }

    private static MetodoEsito scegliMetodo() {
        double x = rnd.nextDouble();
        if (x < P_TELEPASS) return new MetodoEsito("TELEPASS", true);
        if (x < P_TELEPASS + P_CONTANTI) return new MetodoEsito("CONTANTI", true);
        return new MetodoEsito("EVASIONE", false);
    }

    private static class MetodoEsito {
        String metodo;
        boolean pagato;
        MetodoEsito(String metodo, boolean pagato) {
            this.metodo = metodo;
            this.pagato = pagato;
        }
    }
}