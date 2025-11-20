package com.acme.biblio.ga.util;

public class SedeMapper {

    public static String normalize(String sede) {
        if (sede == null) return null;

        String lower = sede.toLowerCase();

        if (lower.contains("sede-a")) return "A";
        if (lower.contains("sede-b")) return "B";

        return sede;
    }
}
