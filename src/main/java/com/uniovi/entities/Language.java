package com.uniovi.entities;

import lombok.Getter;

@Getter
public enum Language {
    EN("en"),
    ES("es"),
    FR("fr"),
    DE("de");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public static Language fromCode(String code) {
        for (Language lang : values()) {
            if (lang.code.equals(code)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Invalid language code: " + code);
    }
}