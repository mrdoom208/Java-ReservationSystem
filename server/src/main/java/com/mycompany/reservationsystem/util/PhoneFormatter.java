package com.mycompany.reservationsystem.util;

public class PhoneFormatter {

    public static String normalizePHNumber(String phone) {
        if (phone == null) return null;
        
        String digits = phone.replaceAll("\\D", "");
        
        if (digits.startsWith("63")) {
            return digits;
        } else if (digits.startsWith("0")) {
            return "63" + digits.substring(1);
        } else if (digits.length() == 10) {
            return "63" + digits;
        }
        
        return digits;
    }
}
