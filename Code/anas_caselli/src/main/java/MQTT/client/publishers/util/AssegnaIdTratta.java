package MQTT.client.publishers.util;

import java.util.*;

public class AssegnaIdTratta {
    public static final Map<String, List<Integer>> trattePerCap = new HashMap<>();

    static {
        trattePerCap.put("MI", Arrays.asList(1,4,7,12,15,16,17,45,46,47));
        trattePerCap.put("RM", Arrays.asList(5,6,10,13,34,35,36,37,64,65,66,67));
        trattePerCap.put("NA", Arrays.asList(11,24,25,26,27,28,54,55,56,57,58));
        trattePerCap.put("TO", Arrays.asList(8,29,30,31,32,33,59,60,61,62,63));
        trattePerCap.put("FI", Arrays.asList(9,18,19,20,21,22,23,48,49,50,51,52,53));
        trattePerCap.put("BA", Arrays.asList(13,42,43,44,72,73,74));
        trattePerCap.put("AL", Arrays.asList(14,38,39,40,41,68,69,70,71));
    }

    public static int scegliTrattaCompatibile(String cap) {
        Random r = new Random();
        List<Integer> possibili = trattePerCap.get(cap);

        if (possibili == null || possibili.isEmpty()) {
            return -1; // nessuna tratta disponibile
        }

        return possibili.get(r.nextInt(possibili.size()));
    }
}