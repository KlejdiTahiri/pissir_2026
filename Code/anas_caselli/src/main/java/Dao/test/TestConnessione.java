package Dao.test;

import Dao.Database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Test iniziale per conessione su db online da rimuovere a fine lavoro (test)
 */
public class TestConnessione {
    public static void main(String[] args) {
        try (Connection conn = Database.connect()) {
            System.out.println("✅ Connessione riuscita a Supabase!");
        } catch (SQLException e) {
            System.err.println("❌ Connessione fallita: " + e.getMessage());
        }
    }
}
