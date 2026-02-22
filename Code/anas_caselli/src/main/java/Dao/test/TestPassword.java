package Dao.test;

import org.mindrot.jbcrypt.BCrypt;

public class TestPassword {
    public static void main(String[] args) {
        String passwordInput = "password";
        String hashFromDB = "$2a$10$F8g9qfbQKz3zSCNhslaPp.P5S..zE8VoAc.NGILRx7Z8or.5u97qu";

        boolean match = BCrypt.checkpw(passwordInput, hashFromDB);
        System.out.println("Password corretta? " + match);

        /*String password = "password";
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("Hash generato: " + hashed);*/
    }

}
