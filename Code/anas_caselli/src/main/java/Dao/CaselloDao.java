package Dao;

import Model.Casello;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CaselloDao {

    /**
     * Metodo per il cambio di stato di un casello ( TRUE | FALSE )
     */
    public static boolean aggiornaStatoCasello(String cap, int idCasello, String direzione, boolean nuovoStato) {
        String query = "UPDATE caselli SET stato = ? WHERE cap = ? AND id_casello = ? AND direzione = ?";

        try (
                Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            // Conversione booleano → intero: true → 1, false → 0
            stmt.setInt(1, nuovoStato ? 1 : 0);
            stmt.setString(2, cap);
            stmt.setInt(3, idCasello);
            stmt.setString(4, direzione);

            System.out.println("🛠️ Aggiornamento stato casello:");
            System.out.println("   CAP: " + cap);
            System.out.println("   ID: " + idCasello);
            System.out.println("   Direzione: " + direzione);
            System.out.println("   Nuovo stato (int): " + (nuovoStato ? 1 : 0));

            int updatedRows = stmt.executeUpdate();
            System.out.println("🚧 Righe aggiornate: " + updatedRows);

            return updatedRows == 1;

        } catch (SQLException e) {
            System.err.println("❌ Errore aggiornando stato casello: " + e.getMessage());
            return false;
        }
    }


    /**
     * Metodo per restituire i dati di un casello
     */
    public static Casello getCaselloByKey(String cap, int idCasello, String direzione) {
        String query = "SELECT * FROM caselli WHERE cap = ? AND id_casello = ? AND direzione = ?";
        Casello c = null;

        try (
                Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setString(1, cap);
            stmt.setInt(2, idCasello);
            stmt.setString(3, direzione);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    c = new Casello();
                    c.cap = rs.getString("cap");
                    c.idCasello = rs.getInt("id_casello");
                    c.direzione = rs.getString("direzione");
                    c.stato = rs.getBoolean("stato");
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore recuperando casello: " + e.getMessage());
        }

        return c;
    }

    /**
     * Metodo per visualizzare tutti i caselli
     */
    public static List<Casello> getAllCaselli() {
        List<Casello> caselli = new ArrayList<>();
        String query = "SELECT * FROM caselli";

        try (
                Connection conn = Database.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)
        ) {
            while (rs.next()) {
                Casello c = new Casello();
                c.cap = rs.getString("cap");
                c.idCasello = rs.getInt("id_casello");
                c.direzione = rs.getString("direzione");
                c.stato = rs.getBoolean("stato");
                caselli.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Errore recuperando lista caselli: " + e.getMessage());
        }

        return caselli;
    }


    public static boolean addNuovoCasello(String cap, String direzione) {
        int statoIniziale = 1;

        String queryMaxId = "SELECT MAX(id_casello) FROM caselli WHERE cap = ? AND direzione = ?";
        String insertQuery = "INSERT INTO caselli (cap, id_casello, direzione, stato) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement psMaxId = conn.prepareStatement(queryMaxId);
             PreparedStatement psInsert = conn.prepareStatement(insertQuery)) {

            // Step 1: Recupera il massimo id_casello per quel CAP e direzione
            psMaxId.setString(1, cap);
            psMaxId.setString(2, direzione);

            ResultSet rs = psMaxId.executeQuery();
            int nuovoId = 1; // Default se non ce ne sono ancora

            if (rs.next()) {
                int maxId = rs.getInt(1);
                if (!rs.wasNull()) {
                    nuovoId = maxId + 1;
                }
            }

            // Step 2: Inserisci il nuovo casello
            psInsert.setString(1, cap);
            psInsert.setInt(2, nuovoId);
            psInsert.setString(3, direzione);
            psInsert.setInt(4, statoIniziale);

            int rowsInserted = psInsert.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean eliminaCasello(String cap, int id, String direzione) {
        String query = "DELETE FROM caselli WHERE cap = ? AND id_casello = ? AND direzione = ?";

        try (
                Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setString(1, cap);
            stmt.setInt(2, id);
            stmt.setString(3, direzione);

            int rowsDeleted = stmt.executeUpdate();
            System.out.println("🗑️ Righe eliminate: " + rowsDeleted);

            return rowsDeleted == 1; // True se una riga eliminata, false altrimenti

        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'eliminazione del casello: " + e.getMessage());
            return false;
        }
    }



}
