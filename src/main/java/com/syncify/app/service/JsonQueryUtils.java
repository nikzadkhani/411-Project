package com.syncify.app.service;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * This class is intended to make the API call to retrieve the JWT
 */
public class JsonQueryUtils {

    private int responseCode;
    private String responseTrimmed;
    private JsonObject jsonObject;
    //a Preferences object to store the token, avoiding repetitive calls
    private Preferences preferences = Preferences.userNodeForPackage(JsonQueryUtils.class);
    private String keyId = "R7SQHMWJKU";
    private String teamId = "A76HA22K75";

    public JsonQueryUtils() {
        this.responseCode = 0;
        this.responseTrimmed = "";
        this.jsonObject = null;
    }




    private String getToken() throws NoSuchAlgorithmException, InvalidKeySpecException {

        JsonParser parser = new JsonParser();

        String secret = null;

        secret="-----BEGIN PRIVATE KEY-----MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgVJrJRAoZI6S2UkwZA7EhHhKW5zSloouwue/Q3D4cyOmgCgYIKoZIzj0DAQehRANCAAQZyu0OBCN4XCY+4F/E0d+pqXJoLZ7hXbYSN4TMgiEjNnpeX0anKWMk8zeRmHfDa05waNKs5XAFQUyVr1X3PpvF-----END PRIVATE KEY-----";

            //secret = "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgVJrJRAoZI6S2UkwZ A7EhHhKW5zSloouwue/Q3D4cyOmgCgYIKoZIzj0DAQehRANCAAQZyu0OBCN4XCY+ 4F/E0d+pqXJoLZ7hXbYSN4TMgiEjNnpeX0anKWMk8zeRmHfDa05waNKs5XAFQUyV r1X3PpvF";

            System.out.println("secret key is: " + secret);


        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.ES256;
        //byte[] publicBytes = Base64.decodeBase64(secret);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(secret.getBytes("utf-8"));
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey prvKey = keyFactory.generatePrivate(keySpec);
        ECPrivateKey eckey = (ECPrivateKey)prvKey;


        long nowMillis = System.currentTimeMillis();
        System.out.println("nowMillis: " + nowMillis);
        Date now = new Date(nowMillis);
        Date expiry = new Date(nowMillis + 3100100100L);

        long expiryPref = nowMillis + 3100100100L;

        preferences.getLong("expiry",0);
        preferences.putLong("expiry", expiryPref);

        System.out.println("EXP: " + expiry.toString());
        System.out.println("NOW: " + now.toString());

        //Setting the JWT Claims
        JwtBuilder builder = Jwts.builder()
            .setIssuedAt(now)
            .setHeaderParam("alg","ES256")
            .setHeaderParam("kid",keyId)
            .setExpiration(expiry)
            .setIssuer(teamId)
            .signWith( keyFactory.generatePrivate(secret), signatureAlgorithm);



        System.out.println("BUILDER: " + builder.compact());

        preferences.get("builderString","");
        preferences.put("builderString", builder.compact().toString());

        return builder.compact().toString();
    }


    public JsonObject getJson(String link) throws InvalidKeySpecException, NoSuchAlgorithmException {

        String response = "";
        BufferedReader in = null;

        try {
            URL url = new URL(link);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

            String basicAuth = "";

            long prefExpiry = preferences.getLong("expiry",0);

            System.out.println("prefExpiry: " + prefExpiry);
            System.out.println("System.currentTimeMillis(): " + System.currentTimeMillis());

            String prefBuilder = preferences.get("builderString","");

            System.out.println("preferences.get(builderString,0): " + preferences.get("builderString",""));

            if(preferences.getLong("expiry",0) < System.currentTimeMillis() || preferences.get("builderString","") == null) {
                basicAuth = "Bearer " + getToken();
                System.out.println("###getToken()###");
            } else {
                basicAuth = "Bearer " + preferences.get("builderString","");
                System.out.println("LINE 128 basicAuth: " + basicAuth);
                System.out.println("Token retrieved from preferences");
            }

            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("Authorization", basicAuth);
            this.responseCode = httpCon.getResponseCode();

            System.out.println("Sending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            Map<String, List<String>> headers = httpCon.getHeaderFields();

            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                if (key != null) {
                    if (key.equals("Retry-After")) {
                        List<String> value = entry.getValue();
                        //this.retryAfterSeconds = value.get(0);

                        //System.out.println("RETRY AFTER: " + this.retryAfterSeconds + " seconds!");
//                        throw new CustomException(seconds);
                    } else {
                        //System.out.println("NO RETRY AFTER!");
                    }
                }
                System.out.println("Header Name: " + key);
                List<String> value = entry.getValue();
                System.out.println("Header Value: " + value.get(0));

            }

            if (this.responseCode != 200) {
                return null;

            }

            in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
            String inputLine;

            while((inputLine = in.readLine()) != null) {
                response += inputLine;
            }
            //in.close();

        } catch (MalformedURLException ex) {
            System.out.println("MalformedURLException!!");
        } catch (ProtocolException ex) {
            System.out.println("ProtocolException!!");
        } catch (IOException ex) {
            System.out.println("IOException!!");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        this.responseTrimmed = response.trim();
        this.jsonObject = new JsonParser().parse(responseTrimmed).getAsJsonObject();

        System.out.println("jsonObject from JsonQueryUtils: " + this.jsonObject.toString());

        return this.jsonObject;
    }
}
