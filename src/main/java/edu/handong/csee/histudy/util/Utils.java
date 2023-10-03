package edu.handong.csee.histudy.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();

        return (month >= 3 && month <= 8)
                ? "spring"
                : "fall";
    }

    public static int getCurrentYear() {
        return LocalDate.now().getYear();
    }

    public static String getCurrentFormattedDateTime(String pattern) {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(pattern));
    }
}
