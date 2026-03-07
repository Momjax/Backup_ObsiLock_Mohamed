package com.coffrefort.client.config;

import com.fasterxml.jackson.databind.MappingIterator;

import java.util.Properties;

public class AppProperties {

    //méthodes
    private static Properties prop = new Properties();

    public static void set(String key, String value) {
        prop.setProperty(key, value);
        System.out.println("AppProperties.set -> key: " + key + " value: " + value);
    }

    public static String get(String key) {
        String value = prop.getProperty(key);
        System.out.println("AppProperties.get -> key: " + key + " value: " + value);
        return value;
    }

    public static void remove(String key) {

        prop.remove(key); // properties est ton ConcurrentHashMap
    }

    private AppProperties() {
        // private => pour empêcher instancier la classe
    }

}
