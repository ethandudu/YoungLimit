package fr.ethanduault.younglimit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HttpRequest implements Callable<String> {
    private final String url;
    private final String body;
    /**
     * @param url the url to call
     * @param latitude the latitude of the location
     * @param longitude the longitude of the location
     */
    public HttpRequest(String url, double latitude, double longitude) {
        this.url = url;
        this.body = "data=[out:json];\n" +
                "   way[highway](around:10, "+latitude+","+longitude+");\n" +
                "out body;";
    }

    /**
     * Do the request
     * @return the response of the request
     */
    @Override
    public String call() {
        String response;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Android/YoungLimit");
            //set body
            con.setDoOutput(true);
            con.getOutputStream().write(body.getBytes());

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder responseBuffer = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    responseBuffer.append(inputLine);
                }
                in.close();
                response = responseBuffer.toString();
            } else {
                response = "Error: " + con.getResponseCode();
            }

        } catch (IOException e) {
            response = "Error: " + e.getMessage();
        }
        return response;
    }

    /**
     * Execute the request in a new thread
     * @param url the url to call
     * @return the response of the request
     */
    public static Future<String> execute(String url, double latitude, double longitude) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new HttpRequest(url, latitude, longitude));
        executor.shutdown(); // it is very important to shutdown your non-singleton ExecutorService.
        return future;
    }
}