package Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagamentoDao {

    /**
     * Inserisce un pagamento nella tabella pagamenti.
     *
     * @return id pagamento inserito (oppure -1 se errore)
     */
    public static int inserisciPagamento(
            String targa,
            double importo,
            String metodo,
            int statoPagamento,      // 1 pagato, 0 non pagato
            int idCasello,
            String cap,
            String direzione
    ) {
        String sql = "INSERT INTO pagamenti (targa, importo, metodo, stato_pagamento, id_casello, cap, direzione) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = Database.connect();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, targa);
            ps.setDouble(2, importo);
            ps.setString(3, metodo);
            ps.setInt(4, statoPagamento);
            ps.setInt(5, idCasello);
            ps.setString(6, cap);
            ps.setString(7, direzione);

            int rows = ps.executeUpdate();
            if (rows != 1) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;

        } catch (SQLException e) {
            System.err.println("Errore inserimento pagamento: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Utility: statoPagamento da boolean pagato.
     */
    public static int statoDaPagato(boolean pagato) {
        return pagato ? 1 : 0;
    }

    /**
     * (Opzionale) Lista pagamenti per targa.
     */
    public static List<PagamentoRecord> getPagamentiByTarga(String targa) {
        List<PagamentoRecord> out = new ArrayList<>();
        String sql = "SELECT * FROM pagamenti WHERE targa = ? ORDER BY data DESC";

        try (
                Connection conn = Database.connect();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, targa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(fromRs(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore getPagamentiByTarga: " + e.getMessage());
        }
        return out;
    }

    /**
     * (Opzionale) Lista pagamenti recenti.
     */
    public static List<PagamentoRecord> getPagamentiRecenti(int limit) {
        List<PagamentoRecord> out = new ArrayList<>();
        String sql = "SELECT * FROM pagamenti ORDER BY data DESC LIMIT ?";

        try (
                Connection conn = Database.connect();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(fromRs(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore getPagamentiRecenti: " + e.getMessage());
        }
        return out;
    }

    private static PagamentoRecord fromRs(ResultSet rs) throws SQLException {
        PagamentoRecord p = new PagamentoRecord();
        p.id = rs.getInt("id");
        p.targa = rs.getString("targa");
        p.data = rs.getString("data"); // puoi usare Timestamp se preferisci
        p.importo = rs.getDouble("importo");
        p.metodo = rs.getString("metodo");
        p.statoPagamento = rs.getInt("stato_pagamento");
        p.idCasello = rs.getInt("id_casello");
        p.cap = rs.getString("cap");
        p.direzione = rs.getString("direzione");
        return p;
    }

    /**
     * DTO semplice (evita di creare Model se non vuoi).
     * Se preferisci, spostalo in package Model.
     */
    public static class PagamentoRecord {
        public int id;
        public String targa;
        public String data;
        public double importo;
        public String metodo;
        public int statoPagamento;
        public int idCasello;
        public String cap;
        public String direzione;
    }
}