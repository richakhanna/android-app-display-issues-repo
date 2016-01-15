package com.richdroid.doctor.http;

import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONGetWebService {

    private static final String LOG_TAG = JSONGetWebService.class.getSimpleName();
    private final int connectTimeOut;
    private final int readTimeOut;


    public JSONGetWebService() {
        this.connectTimeOut = 30000;
        this.readTimeOut = 60000;
    }

    public JSONWebServiceResponse hit(URL url) {

        JSONArray jsonResponse = new JSONArray();
        int httpStatusCode = HttpURLConnection.HTTP_NO_CONTENT;
        Log.d(LOG_TAG, "starting http request with url: " + url);

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder sb = null;

        long hitStartMillis = System.currentTimeMillis();
        try {
            long startMillis = hitStartMillis;
            connection = (HttpURLConnection) url.openConnection();
            // Set timeouts in milliseconds
            connection.setConnectTimeout(connectTimeOut);
            connection.setReadTimeout(readTimeOut);

            time(url + " http connection opened ", startMillis);

            connection.setDoOutput(false);

            startMillis = System.currentTimeMillis();

            try {
                httpStatusCode = connection.getResponseCode();
            } catch (IOException e) {
                httpStatusCode = connection.getResponseCode();
            }
            Log.d(LOG_TAG, url + " http status: " + httpStatusCode);
            if (HttpURLConnection.HTTP_OK == httpStatusCode) {

                InputStream inputStream = connection.getInputStream();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    Log.d(LOG_TAG, "line : " + line);
                }

                if (sb.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                jsonResponse = new JSONArray(sb.toString());
                time(url + " http response read", startMillis);

            } else if (HttpURLConnection.HTTP_UNAUTHORIZED == httpStatusCode) {
                Log.d(LOG_TAG, "unauthorized httpStatusCode: " + httpStatusCode);
            } else {
                Log.d(LOG_TAG, "unhandled httpStatusCode: " + httpStatusCode);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, url + " It came in Exception catch block");
            if (sb != null) {
                Log.e(LOG_TAG, url + " " + e.getMessage()
                        + " : Response string from server :  " + sb.toString()
                        + "\n", e);
            } else {
                Log.e(LOG_TAG, url + " " + e.getMessage(), e);
            }

        } finally {
            if (null != reader) {
                Log.d(LOG_TAG, url + " closing reader");
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, url + " " + e.getMessage(), e);
                }
            }
            if (null != connection) {
                Log.d(LOG_TAG, url + " disconnecting connection ");
                connection.disconnect();
            }
        }

        time(url + " http request completed ", hitStartMillis);

        JSONWebServiceResponse response = new JSONWebServiceResponse(
                jsonResponse, httpStatusCode);
        return response;
    }

    private void time(final String message, final long startMillis) {
        long timeTakeMillis = System.currentTimeMillis() - startMillis;
        Log.d(LOG_TAG, "{ logApiTime + } " + message + " timeTaken: "
                + timeTakeMillis + "ms");
    }
}
