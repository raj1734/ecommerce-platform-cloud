package com.ecommerce.config.config;

import java.net.HttpURLConnection;
import java.net.URI;

public class ConfigServerTest {

    public static void main(String[] args) throws Exception {
        var url = URI.create(
                "http://127.0.0.1:8889/auth-service/default"
        ).toURL();

        var connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod("GET");

        System.out.println("Response code: " + connection.getResponseCode());
    }
}