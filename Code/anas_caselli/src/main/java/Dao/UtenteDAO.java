package Dao;

import Model.Utente;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.postgresql.util.JdbcBlackHole.close;



import Model.Utente;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAO {

    // Metodo per login con verifica BCrypt
    public static Utente login(String username, String passwordInChiaro) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            System.out.println("🔍 [LOGIN] Inizio procedura login per username: " + username);

            conn = Database.connect();
            System.out.println("📡 [LOGIN] Connessione al DB riuscita");

            String query = "SELECT * FROM utenti WHERE username = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, username);

            System.out.println("🔎 [LOGIN] Esecuzione query: " + query);
            rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("✅ [LOGIN] Utente trovato nel DB: " + username);

                String passwordHashDb = rs.getString("password_hash");

                // Log per debug (mostra solo i primi caratteri dell'hash per sicurezza)
                System.out.println("🔐 [LOGIN] Hash DB (primi 20 char): " +
                        (passwordHashDb != null ? passwordHashDb.substring(0, Math.min(20, passwordHashDb.length())) + "..." : "NULL"));
                System.out.println("🔐 [LOGIN] Password in chiaro ricevuta (lunghezza): " +
                        (passwordInChiaro != null ? passwordInChiaro.length() + " caratteri" : "NULL"));
                System.out.println("🔐 [LOGIN] Password in chiaro (primi 3 char): " +
                        (passwordInChiaro != null && passwordInChiaro.length() >= 3 ? passwordInChiaro.substring(0, 3) + "***" : "***"));

                // Verifica se l'hash inizia con $2a$ (formato BCrypt valido)
                if (passwordHashDb != null && passwordHashDb.startsWith("$2a$")) {
                    System.out.println("✅ [LOGIN] Hash BCrypt valido rilevato");
                } else {
                    System.out.println("⚠️ [LOGIN] ATTENZIONE: L'hash NON sembra essere BCrypt! Hash: " + passwordHashDb);
                    System.out.println("⚠️ [LOGIN] Probabilmente la password è salvata in chiaro!");
                    return null;
                }

                // ✅ Usa BCrypt per verificare la password
                System.out.println("🔐 [LOGIN] Inizio verifica BCrypt...");
                boolean passwordMatch = BCrypt.checkpw(passwordInChiaro, passwordHashDb);
                System.out.println("🔐 [LOGIN] Risultato verifica BCrypt: " + (passwordMatch ? "✅ MATCH" : "❌ NO MATCH"));

                if (passwordMatch) {
                    System.out.println("🎉 [LOGIN] Login RIUSCITO per: " + username);

                    Utente u = new Utente();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(passwordHashDb);
                    u.setRuolo(rs.getString("ruolo"));

                    System.out.println("👤 [LOGIN] Utente creato - ID: " + u.getId() + ", Ruolo: " + u.getRuolo());
                    return u;
                } else {
                    System.out.println("❌ [LOGIN] Password non corrisponde per: " + username);
                }
            } else {
                System.out.println("❌ [LOGIN] Nessun utente trovato con username: " + username);
            }

            System.out.println("❌ [LOGIN] Login FALLITO per: " + username);
            return null;

        } catch (SQLException e) {
            System.out.println("❌ [LOGIN] Errore SQL nel login: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("❌ [LOGIN] Errore generico nel login: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            System.out.println("🔚 [LOGIN] Chiusura risorse...");
            closeResources(rs, ps, conn);
            System.out.println("🔚 [LOGIN] Fine procedura login per: " + username);
        }
    }

    public static Utente getByUsername(String username) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Database.connect();
            System.out.println("📡 Connessione al DB riuscita");

            ps = conn.prepareStatement("SELECT * FROM utenti WHERE username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Utente trovato nel DB: " + username);

                Utente u = new Utente();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setRuolo(rs.getString("ruolo"));
                return u;
            } else {
                System.out.println("❌ Nessun utente con username: " + username);
            }
        } catch (SQLException e) {
            System.out.println("❌ Errore SQL: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, ps, conn);
        }

        return null;
    }

    // Metodo per creare un nuovo utente con password hashata
    public static boolean createUser(String username, String passwordInChiaro, String ruolo) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Database.connect();

            // ✅ Hash della password con BCrypt
            String passwordHash = BCrypt.hashpw(passwordInChiaro, BCrypt.gensalt());

            String query = "INSERT INTO utenti (username, password_hash, ruolo) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, ruolo);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("❌ Errore nella creazione utente: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public static boolean modificaDatiUtente(String oldUsername, String newUsername, String newPassword) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Database.connect();
            System.out.println("📡 Connessione al DB riuscita");

            StringBuilder query = new StringBuilder("UPDATE utenti SET ");
            boolean modificaUsername = newUsername != null && !newUsername.isEmpty() && !newUsername.equals(oldUsername);
            boolean modificaPassword = newPassword != null && !newPassword.isEmpty();

            if (modificaUsername) query.append("username = ?");
            if (modificaPassword) {
                if (modificaUsername) query.append(", ");
                query.append("password_hash = ?");
            }
            query.append(" WHERE username = ?");

            ps = conn.prepareStatement(query.toString());

            int paramIndex = 1;

            if (modificaUsername) {
                ps.setString(paramIndex++, newUsername);
            }

            if (modificaPassword) {
                String hashedPw = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                ps.setString(paramIndex++, hashedPw);
            }

            ps.setString(paramIndex, oldUsername);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✅ Profilo aggiornato con successo per: " + oldUsername);
                return true;
            } else {
                System.out.println("❌ Nessun utente aggiornato (forse username errato)");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("❌ Errore SQL nella modifica profilo: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(null, ps, conn);
        }
    }


    // Metodo helper per chiudere le risorse
    private static void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.out.println("❌ Errore nella chiusura delle risorse: " + e.getMessage());
        }
    }
}

