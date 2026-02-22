package Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoricoPassaggioDao {

    /**
     * Inserisce un record in storico_passaggi.
     * @return id inserito, -1 se errore
     */
    public static int inserisciStoricoPassaggio(StoricoPassaggioRecord s) {
        String sql = """
            INSERT INTO storico_passaggi (
                targa,
                cap_ingresso, id_casello_ingresso, direzione_ingresso,
                cap_uscita, id_casello_uscita, direzione_uscita,
                data_ingresso, data_uscita,
                chilometri, velocita_media,
                telepass, prezzo_pagato, metodo_pagamento
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (
                Connection conn = Database.connect();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, s.targa);

            ps.setString(2, s.capIngresso);
            ps.setInt(3, s.idCaselloIngresso);
            ps.setString(4, s.direzioneIngresso);

            ps.setString(5, s.capUscita);
            ps.setInt(6, s.idCaselloUscita);
            ps.setString(7, s.direzioneUscita);

            ps.setString(8, s.dataIngresso); // ISO string ok
            ps.setString(9, s.dataUscita);   // ISO string ok

            ps.setInt(10, s.chilometri);
            ps.setDouble(11, s.velocitaMedia);

            ps.setInt(12, s.telepass);       // 1/0
            ps.setDouble(13, s.prezzoPagato);
            ps.setString(14, s.metodoPagamento);

            int rows = ps.executeUpdate();
            if (rows != 1) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;

        } catch (SQLException e) {
            System.err.println("[DAO][ERROR] inserisciStoricoPassaggio: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public static List<StoricoPassaggioRecord> getStoricoByTarga(String targa) {
        List<StoricoPassaggioRecord> out = new ArrayList<>();
        String sql = "SELECT * FROM storico_passaggi WHERE targa = ? ORDER BY data_uscita DESC";

        try (
                Connection conn = Database.connect();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, targa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(fromRs(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DAO][ERROR] getStoricoByTarga: " + e.getMessage());
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Storico filtrato per uscita (coerente con il tuo tab "pagati")
     */
    public static List<StoricoPassaggioRecord> getStoricoPerUscita(String capOut, int idCaselloOut, String dirOut) {
        List<StoricoPassaggioRecord> out = new ArrayList<>();
        String sql = """
            SELECT * FROM storico_passaggi
            WHERE cap_uscita = ? AND id_casello_uscita = ? AND direzione_uscita = ?
            ORDER BY data_uscita DESC
        """;

        try (
                Connection conn = Database.connect();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, capOut);
            ps.setInt(2, idCaselloOut);
            ps.setString(3, dirOut);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(fromRs(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DAO][ERROR] getStoricoPerUscita: " + e.getMessage());
            e.printStackTrace();
        }
        return out;
    }

    private static StoricoPassaggioRecord fromRs(ResultSet rs) throws SQLException {
        StoricoPassaggioRecord s = new StoricoPassaggioRecord();
        s.id = rs.getInt("id");
        s.targa = rs.getString("targa");

        s.capIngresso = rs.getString("cap_ingresso");
        s.idCaselloIngresso = rs.getInt("id_casello_ingresso");
        s.direzioneIngresso = rs.getString("direzione_ingresso");

        s.capUscita = rs.getString("cap_uscita");
        s.idCaselloUscita = rs.getInt("id_casello_uscita");
        s.direzioneUscita = rs.getString("direzione_uscita");

        s.dataIngresso = rs.getString("data_ingresso");
        s.dataUscita = rs.getString("data_uscita");

        s.chilometri = rs.getInt("chilometri");
        s.velocitaMedia = rs.getDouble("velocita_media");

        s.telepass = rs.getInt("telepass");
        s.prezzoPagato = rs.getDouble("prezzo_pagato");
        s.metodoPagamento = rs.getString("metodo_pagamento");
        return s;
    }


    public static double getTotaleGuadagniPerTratta(
            String capIn, int idIn, String dirIn,
            String capOut, int idOut, String dirOut
    ) {
        String sql = """
        SELECT COALESCE(SUM(prezzo_pagato), 0) AS totale
        FROM storico_passaggi
        WHERE cap_ingresso = ? AND id_casello_ingresso = ? AND direzione_ingresso = ?
          AND cap_uscita = ? AND id_casello_uscita = ? AND direzione_uscita = ?
    """;

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, capIn);
            ps.setInt(2, idIn);
            ps.setString(3, dirIn);

            ps.setString(4, capOut);
            ps.setInt(5, idOut);
            ps.setString(6, dirOut);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("totale");
            }
        } catch (SQLException e) {
            System.err.println("[DAO][ERROR] getTotaleGuadagniPerTratta: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public static class StoricoPassaggioRecord {
        public int id;
        public String targa;

        public String capIngresso;
        public int idCaselloIngresso;
        public String direzioneIngresso;

        public String capUscita;
        public int idCaselloUscita;
        public String direzioneUscita;

        public String dataIngresso;
        public String dataUscita;

        public int chilometri;
        public double velocitaMedia;

        public int telepass; // 1/0
        public double prezzoPagato;
        public String metodoPagamento;
    }


}