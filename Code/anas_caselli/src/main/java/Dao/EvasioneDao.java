package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class EvasioneDao {

    /**
     * Inserisce una evasione (passaggio senza pagamento).
     * @return id evasione inserita, oppure -1 se errore
     */
    public static int inserisciEvasione(
            Integer idBiglietto,
            String targa,
            Double importo,
            String metodo,
            Integer distanza,

            String capIn,
            Integer idCaselloIn,
            String direzioneIn,

            String capOut,
            Integer idCaselloOut,
            String direzioneOut,

            String note
    ) {
        String sql = "INSERT INTO evasioni (" +
                "id_biglietto, targa, importo, metodo, distanza, " +
                "cap_in, id_casello_in, direzione_in, " +
                "cap_out, id_casello_out, direzione_out, note" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (idBiglietto == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, idBiglietto);

            ps.setString(2, targa);

            if (importo == null) ps.setNull(3, java.sql.Types.REAL);
            else ps.setDouble(3, importo);

            ps.setString(4, metodo);

            if (distanza == null) ps.setNull(5, java.sql.Types.INTEGER);
            else ps.setInt(5, distanza);

            ps.setString(6, capIn);

            if (idCaselloIn == null) ps.setNull(7, java.sql.Types.INTEGER);
            else ps.setInt(7, idCaselloIn);

            ps.setString(8, direzioneIn);

            ps.setString(9, capOut);

            if (idCaselloOut == null) ps.setNull(10, java.sql.Types.INTEGER);
            else ps.setInt(10, idCaselloOut);

            ps.setString(11, direzioneOut);

            ps.setString(12, note);

            int rows = ps.executeUpdate();
            if (rows != 1) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;

        } catch (Exception e) {
            System.err.println("Errore inserimento evasione: " + e.getMessage());
            return -1;
        }
    }
}