package Dao.test;

import Dao.UtenteDAO;

public class CreateTestUser {
    public static void main(String[] args) {
        String username = "admin";
        String passwordInChiaro = "password123";
        String ruolo = "OPERATORE";

        // Questo hasherà automaticamente la password prima di salvarla
        boolean created = UtenteDAO.createUser(username, passwordInChiaro, ruolo);

        if (created) {
            System.out.println("✅ Utente creato con successo!");
        } else {
            System.out.println("❌ Errore nella creazione dell'utente");
        }
    }
}