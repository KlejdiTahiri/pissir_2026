package Dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Classe per conessione ad db supabase !!!
 */
public class Database {
   /** private static final String URL = "jdbc:postgresql://aws-0-eu-north-1.pooler.supabase.com:6543/postgres?sslmode=require";
    private static final String USER = "postgres.gkmrcozxtpkzyronzepb";
    private static final String PASSWORD = "5X0ElW4CaoLD2ISt";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }**/



   //QUESTA CONNESSIONE È FATA CON IL DB SQLITE NELLA CARTELLA SQLITE, IL CODICE COMMENTATO SOPRA È RIMASTO UGUALE
    // A VOLTE LA CONNESIONE AL DB SQLITE VA GIU
   private static final String URL = "jdbc:sqlite:src/main/java/Dao/sqlite/db_sqlite.db";

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        return DriverManager.getConnection(URL);
    }
}

