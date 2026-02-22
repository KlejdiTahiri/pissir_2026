package Dao;

import Model.Tratta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.postgresql.util.JdbcBlackHole.close;

public class TrattaDao {

    /**
     * metodo per restituire tutte le tratte
     * @return all tratte
     */
    public static List<Tratta> getAllTratte() {
        List<Tratta> tratte = new ArrayList<>();
        String query = "SELECT * FROM tratte WHERE stato = 1";

        try (
                Connection conn = Database.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)
        ) {
            while (rs.next()) {
                Tratta t = new Tratta();
                t.id = rs.getInt("id");
                t.capIn = rs.getString("cap_in");
                t.idCaselloIn = rs.getString("id_casello_in");
                t.dirIn = rs.getString("dir_in");
                t.capOut = rs.getString("cap_out");
                t.idCaselloOut = rs.getString("id_casello_out");
                t.dirOut = rs.getString("dir_out");
                t.prezzo = rs.getDouble("prezzo");
                t.chilometri = rs.getDouble("chilometri");
               // t.velocitaMedia = rs.getDouble("velocita_media");
                tratte.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero delle tratte: " + e.getMessage());
        }

        return tratte;
    }

    /**
     * metodo per modificare una tratta
     * @param t la tratta da aggiornare
     * @return true se aggiornamento riuscito
     */
    public static boolean aggiornaTratta(Tratta t) {
        String query = "UPDATE tratte SET cap_in = ?, id_casello_in = ?, dir_in = ?, cap_out = ?, " +
                "id_casello_out = ?, prezzo = ?, chilometri = ?, velocita_media = ? WHERE id = ?";

        try (
                Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setString(1, t.capIn);
            stmt.setString(2, t.idCaselloIn);
            stmt.setString(3, t.dirIn);
            stmt.setString(4, t.capOut);
            stmt.setString(5, t.idCaselloOut);
            stmt.setDouble(6, t.prezzo);
            stmt.setDouble(7, t.chilometri);
            stmt.setDouble(8, t.velocitaMax);
            stmt.setInt(9, t.id);

            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento della tratta: " + e.getMessage());
            return false;
        }
    }


    public static void cambiaStatoTratta(String capIn, int idCaselloIn, String capOut, int idCaselloOut, boolean nuovoStato) {
        String query = "UPDATE tratte SET stato = ? " +
                "WHERE (cap_in = ? AND id_casello_in = ?) " +
                "   OR (cap_out = ? AND id_casello_out = ?)";

        System.out.println("DEBUG - Parametri ricevuti:");
        System.out.println("  capIn: " + capIn + ", idCaselloIn: " + idCaselloIn);
        System.out.println("  capOut: " + capOut + ", idCaselloOut: " + idCaselloOut);
        System.out.println("  nuovoStato: " + (nuovoStato ? 1 : 0));

        try (
                Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setInt(1, nuovoStato ? 1 : 0);
            stmt.setString(2, capIn);
            stmt.setInt(3, idCaselloIn);
            stmt.setString(4, capOut);
            stmt.setInt(5, idCaselloOut);

            int righeAggiornate = stmt.executeUpdate();
            System.out.println("DEBUG - Righe aggiornate: " + righeAggiornate);

        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento dello stato della tratta: " + e.getMessage());
        }
    }




    public static Tratta getTrattaById(int id) {
        String query = "SELECT * FROM tratte WHERE id = ?";
        Tratta tratta = null;

        try (
                Connection conn = Database.connect();
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                tratta = new Tratta();
                tratta.id = rs.getInt("id");
                tratta.capIn = rs.getString("cap_in");
                tratta.idCaselloIn = rs.getString("id_casello_in");
                tratta.dirIn = rs.getString("dir_in");
                tratta.capOut = rs.getString("cap_out");
                tratta.idCaselloOut = rs.getString("id_casello_out");
                tratta.dirOut = rs.getString("dir_out");
                tratta.prezzo = rs.getDouble("prezzo");
                tratta.chilometri = rs.getDouble("chilometri");
                tratta.velocitaMax = rs.getDouble("velocita_massima");
                tratta.totalePagato = getTotaleTratta(tratta.id);
            }
           

        } catch (SQLException e) {
            System.err.println("Errore nel recupero della tratta con ID " + id + ": " + e.getMessage());
        }

        //chiama getTotaleTratta per ottenere il prezzo dalla tabella storico_passaggi

        return tratta;
    }

   public static double getTotaleTratta(int idTratta) {
    String query = "SELECT SUM(prezzo_pagato) AS totale FROM storico_passaggi WHERE id_tratta = ?";
    double totale = 0.0;

    try (
            Connection conn = Database.connect();
            PreparedStatement stmt = conn.prepareStatement(query)
    ) {
        stmt.setInt(1, idTratta);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            totale = rs.getDouble("totale");
        }

    } catch (SQLException e) {
        System.err.println("Errore nel recupero del totale della tratta con ID " + idTratta + ": " + e.getMessage());
    }

    return totale;
    }




    public static Tratta modificaPrezzoTratta(int id, double nuovoPrezzo) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Database.connect(); // tua funzione per ottenere la connessione
            String query = "UPDATE tratte SET prezzo = ? WHERE id = ?";
            ps = conn.prepareStatement(query);
            ps.setDouble(1, nuovoPrezzo);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                // Dopo update, ritorna la tratta aggiornata
                return getTrattaById(id); // Presumendo che tu abbia già questa funzione
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(ps);
            close(conn);
        }
    }

    public static Tratta modificaVelocitaTratta(int id, int nuovaVelocita) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Database.connect();
            String query = "UPDATE tratte SET velocita_massima = ? WHERE id = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, nuovaVelocita);  // usa setInt perché è un intero
            ps.setInt(2, id);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                return getTrattaById(id);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            close(ps);
            close(conn);
        }
    }



}
