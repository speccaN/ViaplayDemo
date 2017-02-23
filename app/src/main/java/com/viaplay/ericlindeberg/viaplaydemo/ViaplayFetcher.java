package com.viaplay.ericlindeberg.viaplaydemo;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViaplayFetcher {

    private static final String TAG = "ViaplayFetcher";

    private static final String API_KEY = "https://content.viaplay.se/androidv2-se";

    // Parameterlös konstruktor
    public ViaplayFetcher(){}

    // Hämtar raw data från en URL och returnerar det som en array av bytes
    private byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);

        // Eftersom man ansluter till en http URL så kan man cast:a den till en HttpURLConnection
        // för att få tillgång till HTTP-specifika interface.
        // Ansluter inte förrän man kallar getInputStream() eller getOutputStream()
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }


    private String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public HashMap<String, String> fetchData(String url){

        HashMap<String, String> items = new HashMap<>();

        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Recieved JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseData(items, jsonBody);
        }
        catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        }
        catch (IOException ioe){
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }

    public List<String> fetchFromHref(String href) {

        List<String> items = new ArrayList<>();

        try {
            String jsonString = getUrlString(href);
            Log.i(TAG, "Recieved JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseHref(items, jsonBody);
        }
        catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        }
        catch (IOException ioe){
            Log.e(TAG, "Failed to fetch items", ioe);
        }

        return items;
    }

    // Hämtar titlarna från viaplay:sections
    private JSONArray parseData(HashMap<String, String> items, JSONObject jsonBody)
            throws IOException, JSONException {

        JSONObject jsonObject = jsonBody.getJSONObject("_links");
        JSONArray jsonArray = jsonObject.getJSONArray("viaplay:sections");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject menuItemJsonObject = jsonArray.getJSONObject(i);

            // Använder HashMap för att binda Titel med dess href
            String title = menuItemJsonObject.getString("title");
            String href = menuItemJsonObject.getString("href");

            // Tar bort flaggan för downloadable content
            items.put(title, href.replace("{?dtg}", ""));
        }
        return jsonObject.getJSONArray("viaplay:sections");
    }

    private void parseHref(List<String> items, JSONObject jsonBody)
            throws IOException, JSONException{

        items.add(jsonBody.getString("title"));
        items.add(jsonBody.getString("description"));
    }
}
