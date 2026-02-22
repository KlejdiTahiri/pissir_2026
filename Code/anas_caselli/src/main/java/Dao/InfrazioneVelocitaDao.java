package Dao;

import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InfrazioneVelocitaDao {

    // ── Mappa un ResultSet in un JsonObject ──
    private static JsonObject mapRow(ResultSet rs) throws Exception {
        JsonObject o = new JsonObject();
        o.addProperty("id",              rs.getInt("id"));
        o.addProperty("idBiglietto",     rs.getInt("id_biglietto"));
        o.addProperty("targa",           rs.getString("targa"));
        o.addProperty("cap",             rs.getString("cap"));
        o.addProperty("idCasello",       rs.getInt("id_casello"));
        o.addProperty("direzione",       rs.getString("direzione"));
        o.addProperty("idTratta",        rs.getInt("id_tratta"));
        o.addProperty("kmRilevazione",   rs.getDouble("km_rilevazione"));
        o.addProperty("velocitaRilevata",rs.getInt("velocita_rilevata"));
        o.addProperty("velocitaMassima", rs.getInt("velocita_massima"));
        o.addProperty("importo",         rs.getDouble("importo"));
        o.addProperty("timestamp",       rs.getString("timestamp"));
        return o;
    }

    // ── Salva una nuova infrazione ──
    public static void salva(JsonObject json) {
        String sql = "INSERT INTO infrazioni_velocita " +
                "(id_biglietto,targa,cap,id_casello,direzione,id_tratta,km_rilevazione,velocita_rilevata,velocita_massima,importo,timestamp) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    json.get("idBiglietto").getAsInt());
            ps.setString(2, json.get("targa").getAsString());
            ps.setString(3, json.get("cap").getAsString());
            ps.setInt(4,    json.get("idCasello").getAsInt());
            ps.setString(5, json.get("direzione").getAsString());
            ps.setInt(6,    json.has("idTratta") ? json.get("idTratta").getAsInt() : 0);
            ps.setDouble(7, json.get("kmRilevazione").getAsDouble());
            ps.setInt(8,    json.get("velocitaRilevata").getAsInt());
            ps.setInt(9,    json.get("velocitaMassima").getAsInt());
            ps.setDouble(10,json.get("importo").getAsDouble());
            ps.setString(11,json.get("timestamp").getAsString());

            ps.executeUpdate();
            System.out.println("💾 Infrazione velocità salvata: " + json.get("targa").getAsString());

        } catch (Exception e) {
            System.out.println("❌ Errore salvataggio infrazione velocità: " + e.getMessage());
        }
    }

    // ── Ultima infrazione per targa ──
    public static JsonObject ultimaPerTarga(String targa) {
        String sql = "SELECT * FROM infrazioni_velocita WHERE targa = ? ORDER BY id DESC LIMIT 1";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, targa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            System.out.println("❌ Errore lettura infrazione velocità: " + e.getMessage());
        }
        return null;
    }

    // ── Tutte le infrazioni per casello (cap + id_casello + direzione) ──
    public static List<JsonObject> tuttePerCasello(String cap, int idCasello, String direzione) {
        String sql = "SELECT * FROM infrazioni_velocita " +
                "WHERE UPPER(TRIM(cap)) = UPPER(TRIM(?)) " +
                "AND id_casello = ? " +
                "AND UPPER(TRIM(direzione)) = UPPER(TRIM(?)) " +
                "ORDER BY id DESC";
        List<JsonObject> lista = new ArrayList<>();

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cap);
            ps.setInt(2, idCasello);
            ps.setString(3, direzione);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Errore lettura infrazioni per casello: " + e.getMessage());
        }
        System.out.printf("[DEBUG][DAO] Query infrazioni: cap='%s' id=%d dir='%s'%n", cap, idCasello, direzione);
        return lista;
    }
}