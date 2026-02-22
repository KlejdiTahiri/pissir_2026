package MQTT.client.publishers.util;

import java.util.Random;

public class CoordinateIncidente {
    public static double generaLatCasuale() {
        // Italia approssimata: 36.0 - 46.5
        double min = 36.0;
        double max = 46.5;
        return min + (Math.random() * (max - min));
    }

    public static double generaLonCasuale() {
        // Italia approssimata: 6.5 - 18.8
        double min = 6.5;
        double max = 18.8;
        return min + (Math.random() * (max - min));
    }

    public static String generaGravitaCasuale() {
        String[] livelli = {"BASSA", "MEDIA", "ALTA"};
        return livelli[new Random().nextInt(livelli.length)];
    }

    public static String generaDescrizioneCasuale() {
        String[] descrizioni = {
                "Tamponamento tra due veicoli",
                "Auto fuori strada",
                "Collisione multipla",
                "Veicolo in fiamme",
                "Ostacolo improvviso sulla carreggiata",
                "Ribaltamento del veicolo"
        };
        return descrizioni[new Random().nextInt(descrizioni.length)];
    }

}
