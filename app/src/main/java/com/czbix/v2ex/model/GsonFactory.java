package com.czbix.v2ex.model;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class GsonFactory {
    private static final Gson INSTANCE;

    static {
        final GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingStrategy(new AndroidFieldNamingStrategy());

        INSTANCE = builder.create();
    }

    public static Gson getInstance() {
        return INSTANCE;
    }

    private static class AndroidFieldNamingStrategy implements FieldNamingStrategy {
        private static final String JSON_WORD_DELIMITER = "_";

        @Override
        public String translateName(final Field f) {
            if (f.getName().startsWith("m")) {
                return handleWords(f.getName().substring(1));
            }
            else {
                return f.getName();
            }
        }

        private final static Pattern UPPERCASE_PATTERN = Pattern.compile("(?=\\p{Lu})");

        private String handleWords(final String fieldName) {
            String[] words = UPPERCASE_PATTERN.split(fieldName);
            final StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (sb.length() > 0) {
                    sb.append(JSON_WORD_DELIMITER);
                }
                sb.append(word.toLowerCase());
            }
            return sb.toString();
        }


    }
}
