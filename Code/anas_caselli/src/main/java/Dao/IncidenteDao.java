package Dao;

import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IncidenteDao {

    public static void salvaIncidente(JsonObject incidenteJson) {

        String sql = "INSERT INTO incidenti (id_tratta, timestamp, descrizione, gravita, km) VALUES (?, ?, ?, ?, ?)";

        // Estrazione dati dal JSON
        int idTratta = incidenteJson.get("idTratta").getAsInt();
        String descrizione = incidenteJson.get("descrizione").getAsString();
        String gravita = incidenteJson.get("gravita").getAsString();
        double km = incidenteJson.has("km") ? incidenteJson.get("km").getAsDouble() : 0.0;
        long timestamp = System.currentTimeMillis() / 1000;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idTratta);
            stmt.setLong(2, timestamp);
            stmt.setString(3, descrizione);
            stmt.setString(4, gravita);
            stmt.setDouble(5, km);  // ➜ salvataggio km

            stmt.executeUpdate();

            System.out.println("💾 Incidente salvato nel DB (tratta: " + idTratta + ", km: " + km + ")");

        } catch (SQLException e) {
            System.out.println("❌ Errore salvataggio incidente: " + e.getMessage());
        }
    }


    public static JsonObject leggiIncidente(int idTratta) {
        String sql = "SELECT id_tratta, timestamp, descrizione, gravita, km " +
                "FROM incidenti WHERE id_tratta = ? ORDER BY timestamp DESC LIMIT 1";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idTratta);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JsonObject incidente = new JsonObject();
                    incidente.addProperty("idTratta", rs.getInt("id_tratta"));
                    incidente.addProperty("timestamp", rs.getLong("timestamp"));
                    incidente.addProperty("descrizione", rs.getString("descrizione"));
                    incidente.addProperty("gravita", rs.getString("gravita"));
                    incidente.addProperty("km", rs.getDouble("km"));  // ➜ restituisce il km
                    return incidente;
                } else {
                    System.out.println("⚠️ Nessun incidente trovato per la tratta: " + idTratta);
                    return null;
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Errore lettura incidente: " + e.getMessage());
            return null;
        }
    }

}
