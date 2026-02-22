package Dao;

import Model.Biglietto;
import Model.StoricoConPagamento;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static MQTT.client.publishers.util.CalcolatoreDistanza.TEMPI_PERCORRENZA_ORE;
import static MQTT.client.publishers.util.CalcolatoreDistanza.calcolaDistanza;

public class BigliettiDao {

    /**
     * Metodo per visualizzare tutti i biglietti di un casello
     * @return lista di biglietti
     */
    public static List<Biglietto> getAllBigliettiCasello(String cap, int idCaselloIngresso, String direzione) {
        List<Biglietto> biglietti = new ArrayList<>();
        String query = "SELECT * FROM biglietti WHERE id_casello_ingresso = ? AND cap_ingresso = ? AND direzione_ingresso = ?";

        System.out.println("[DEBUG] Esecuzione query biglietti per casello=" + idCaselloIngresso + ", cap=" + cap + ", direzione=" + direzione);

        try (
                Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(query)
        ) {
            pstmt.setInt(1, idCaselloIngresso);
            pstmt.setString(2, cap);
            pstmt.setString(3, direzione);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Biglietto b = new Biglietto();
                    b.setId(rs.getLong("id"));
                    b.setTarga(rs.getString("targa"));
                    b.setCapIngresso(rs.getString("cap_ingresso"));
                    b.setIdCaselloIngresso(rs.getInt("id_casello_ingresso"));
                    b.setDirezioneIngresso(rs.getString("direzione_ingresso"));
                    b.setDataIngresso(rs.getTimestamp("data_ingresso").toLocalDateTime());
                    b.setValido(rs.getBoolean("valido"));
                    biglietti.add(b);

                    System.out.println("[DEBUG] Biglietto caricato: ID=" + b.getId() + ", targa=" + b.getTarga());
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Errore recuperando lista biglietti: " + e.getMessage());
        }

        return biglietti;
    }


    public static List<StoricoConPagamento> getStoricoConPagamento(
            String capIngresso, int idCaselloIn, String direzioneIn,
            String capUscita, int idCaselloOut, String direzioneOut) {

        List<StoricoConPagamento> lista = new ArrayList<>();

        String query = """
        SELECT 
            s.id,
            s.targa,
            s.cap_uscita,
            s.id_casello_uscita,
            s.direzione_uscita,
            s.data_uscita,
            s.prezzo_pagato,
            s.metodo_pagamento,
            p.data AS data_pagamento,
            p.importo,
            p.metodo,
            p.stato_pagamento
        FROM storico_passaggi s
        LEFT JOIN pagamenti p ON s.targa = p.targa
        WHERE 
            s.cap_ingresso = ? AND 
            s.id_casello_ingresso = ? AND 
            s.direzione_ingresso = ? AND 
            s.cap_uscita = ? AND 
            s.id_casello_uscita = ? AND 
            s.direzione_uscita = ?
        ORDER BY s.data_uscita DESC
    """;

        try (
                Connection conn = Database.connect();
                PreparedStatement pstmt = conn.prepareStatement(query)
        ) {
            // Set parametri INGRESSO
            pstmt.setString(1, capIngresso);
            pstmt.setInt(2, idCaselloIn);
            pstmt.setString(3, direzioneIn);

            // Set parametri USCITA
            pstmt.setString(4, capUscita);
            pstmt.setInt(5, idCaselloOut);
            pstmt.setString(6, direzioneOut);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StoricoConPagamento record = new StoricoConPagamento();

                    // Dati dallo storico
                    record.setId(rs.getLong("id"));
                    record.setTarga(rs.getString("targa"));
                    record.setCapUscita(rs.getString("cap_uscita"));
                    record.setIdCaselloUscita(rs.getInt("id_casello_uscita"));
                    record.setDirezioneUscita(rs.getString("direzione_uscita"));
                    record.setDataUscita(rs.getTimestamp("data_uscita").toLocalDateTime());
                    record.setPrezzoPagato(rs.getDouble("prezzo_pagato"));
                    record.setMetodoPagamentoStorico(rs.getString("metodo_pagamento"));

                    // Dati da pagamenti
                    Timestamp ts = rs.getTimestamp("data_pagamento");
                    record.setDataPagamento(ts != null ? ts.toLocalDateTime() : null);
                    record.setImporto(rs.getDouble("importo"));
                    record.setMetodoPagamento(rs.getString("metodo"));
                    record.setStatoPagamento(rs.getInt("stato_pagamento"));

                    lista.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Errore durante il recupero storico con pagamenti: " + e.getMessage());
        }

        return lista;
    }

    public static void creaBiglietto(Biglietto biglietto) {
        String sql = "INSERT INTO biglietti (targa, cap_ingresso, id_casello_ingresso, direzione_ingresso, data_ingresso, valido) VALUES (?, ?, ?, ?, ?, 1)";
        try (Connection conn = Database.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, biglietto.getTarga());
            stmt.setString(2, biglietto.getCapIngresso());
            stmt.setInt(3, biglietto.getIdCaselloIngresso());
            stmt.setString(4, biglietto.getDirezioneIngresso());
            stmt.setTimestamp(5, Timestamp.valueOf(biglietto.getDataIngresso()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registra l'ingresso di un veicolo in un casello
     */
    public static boolean registraIngresso(String targa, String cap, int idCasello, String direzione) {
        String sql = """
        INSERT INTO biglietti (targa, cap_ingresso, id_casello_ingresso, direzione_ingresso, data_ingresso, valido)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, targa);
            stmt.setString(2, cap);
            stmt.setInt(3, idCasello);
            stmt.setString(4, direzione);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(6, true); // Il biglietto è valido all'ingresso

            int result = stmt.executeUpdate();
            System.out.println("✅ Biglietto d'ingresso creato per targa: " + targa);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la registrazione dell'ingresso: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registra l'uscita di un veicolo dall'autostrada
     * Inserisce direttamente i dati nella tabella uscite
     *
     * @param targa La targa del veicolo
     * @param cap Il CAP del casello di uscita
     * @param idCasello L'ID del casello di uscita
     * @param direzione La direzione del casello di uscita
     * @return true se l'operazione è riuscita, false altrimenti
     */
    public static boolean registraUscita(int idBiglietto, String targa, String cap, int idCasello, String direzione, int distanza) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = Database.connect();

            System.out.println("🚪 Registrazione uscita:");
            System.out.println("   Targa: " + targa);
            System.out.println("   CAP: " + cap);
            System.out.println("   ID Casello: " + idCasello);
            System.out.println("   Direzione: " + direzione);

            double prezzo = calcolaPrezzo(distanza);

            // Inserisci direttamente nella tabella uscite
            // Nota: id_biglietto può essere NULL o 0 se non serve il collegamento
            String sql = "INSERT INTO uscite (id_biglietto, targa, cap_uscita, id_casello_uscita, " +
                    "direzione_uscita, prezzo, chilometri) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idBiglietto);
            pstmt.setString(2, targa);
            pstmt.setString(3, cap);
            pstmt.setInt(4, idCasello);
            pstmt.setString(5, direzione);
            pstmt.setDouble(6, prezzo);
            pstmt.setInt(7,distanza);

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("✅ Uscita registrata con successo per targa: " + targa);
                return true;
            } else {
                System.out.println("❌ Nessuna riga inserita");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore SQL durante la registrazione dell'uscita:");
            System.err.println("   Messaggio: " + e.getMessage());
            System.err.println("   SQLState: " + e.getSQLState());
            System.err.println("   ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
            return false;

        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("❌ Errore nella chiusura delle risorse: " + e.getMessage());
            }
        }
    }


    /**
     * Metodo di test per verificare il calcolo delle distanze
     */
    public static void testCalcolaDistanza() {
        System.out.println("=== TEST CALCOLO DISTANZE ===\n");

        // Test 1: Milano -> Firenze
        System.out.println("Test 1: MI (casello 1) -> FI (casello 1)");
        int dist1 = calcolaDistanza("MI", 1, "USCITA", "FI", 1, "ENTRATA");
        System.out.println("Risultato: " + dist1 + " km\n");

        // Test 2: Milano -> Alessandria
        System.out.println("Test 2: MI (casello 2) -> AL (casello 3)");
        int dist2 = calcolaDistanza("MI", 2, "USCITA", "AL", 3, "ENTRATA");
        System.out.println("Risultato: " + dist2 + " km\n");

        // Test 3: Napoli -> Bari
        System.out.println("Test 3: NA (casello 1) -> BA (casello 1)");
        int dist3 = calcolaDistanza("NA", 1, "USCITA", "BA", 1, "ENTRATA");
        System.out.println("Risultato: " + dist3 + " km\n");

        // Test 4: Stesso casello
        System.out.println("Test 4: MI (casello 1) -> MI (casello 1) - Stesso casello");
        int dist4 = calcolaDistanza("MI", 1, "ENTRATA", "MI", 1, "USCITA");
        System.out.println("Risultato: " + dist4 + " km\n");

        // Test 5: Stesso CAP, caselli diversi
        System.out.println("Test 5: MI (casello 1) -> MI (casello 4) - Caselli diversi");
        int dist5 = calcolaDistanza("MI", 1, "ENTRATA", "MI", 4, "USCITA");
        System.out.println("Risultato: " + dist5 + " km\n");

        // Test 6: Percorso non mappato
        System.out.println("Test 6: XY (casello 1) -> ZZ (casello 1) - Percorso non mappato");
        int dist6 = calcolaDistanza("XY", 1, "ENTRATA", "ZZ", 1, "USCITA");
        System.out.println("Risultato: " + dist6 + " km\n");

        System.out.println("=== FINE TEST ===");
    }


    /**
     * Calcola il prezzo del pedaggio in base ai chilometri
     * NOTA: Implementa la tua tariffa
     */
    private static double calcolaPrezzo(int chilometri) {
        // TODO: Implementa la tua logica di calcolo prezzi
        // Esempio: €0.08 per km
        return chilometri * 0.08;
    }

}
