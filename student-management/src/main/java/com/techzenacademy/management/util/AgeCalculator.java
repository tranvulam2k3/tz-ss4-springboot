package com.techzenacademy.management.util;

import java.time.LocalDate;
import java.time.Period;

public class AgeCalculator {
    private AgeCalculator() {
    }

    public static boolean isAdult(LocalDate dob) {
        if (dob == null)
            return false;
        return Period.between(dob, LocalDate.now()).getYears() >= 18;
    }
}
