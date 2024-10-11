package aceita.Core.Giveways;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Util {

    public static String createCode(int length) {
        StringBuilder result = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int charactersLength = characters.length();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(charactersLength)));
        }
        return result.toString();
    }

    private static final Map<String, Long> timeUnits = new HashMap<>();
    static {
        timeUnits.put("s", 1L);
        timeUnits.put("m", 60L);
        timeUnits.put("h", 3600L);
        timeUnits.put("d", 86400L);
        timeUnits.put("semana", 604800L);
        timeUnits.put("mes", 2592000L);
        timeUnits.put("ano", 31536000L);
    }
    public static long convertToSeconds(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            throw new IllegalArgumentException("Invalid time string");
        }

        char unit = timeStr.charAt(timeStr.length() - 1);
        String unitStr = String.valueOf(unit);
        long multiplier = timeUnits.getOrDefault(unitStr, 0L);

        if (unitStr.equals("s") && timeStr.length() > 2 && timeStr.charAt(timeStr.length() - 2) == 'e' && timeStr.charAt(timeStr.length() - 3) == 'm') {
            unitStr = "mo";
            multiplier = timeUnits.getOrDefault(unitStr, 0L);
        }

        if (multiplier == 0L) {
            return 0L;
        }
        long value = 0;
        try {
            value = Long.parseLong(timeStr.substring(0, timeStr.length() - unitStr.length()));
        } catch (NumberFormatException e) {
            return 0L;
        }

        return value * multiplier;
    }
    public static long convertToTimestamp(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            throw new IllegalArgumentException("Invalid time string");
        }

        String unitStr = getUnitStr(timeStr);
        long multiplier = timeUnits.getOrDefault(unitStr, 0L);

        if (multiplier == 0L) {
            throw new IllegalArgumentException("Invalid time unit");
        }

        long value = Long.parseLong(timeStr.substring(0, timeStr.length() - unitStr.length()));
        return value * multiplier;
    }
    private static String getUnitStr(String timeStr) {
        String unitStr = timeStr.substring(timeStr.length() - 1);
        if (unitStr.equals("o") && timeStr.length() > 1 && timeStr.charAt(timeStr.length() - 2) == 'm') {
            unitStr = "mo";
        }
        return unitStr;
    }
    public static Instant addTimeToCurrent(String timeStr) {
        long secondsToAdd = convertToSeconds(timeStr);
        Instant currentInstant = Instant.now();
        return currentInstant.plusSeconds(secondsToAdd);
    }

}
