package com.viaplay.ericlindeberg.viaplaydemo;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViaplayFetcher {

    private static final String TAG = "ViaplayFetcher";

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

    public HashMap<String, String> fetchData(String url, boolean isConnected, Context ctx) {

        HashMap<String, String> items = new HashMap<>();

        if (isConnected) {
            try {
                String jsonString = getUrlString(url);
                Log.i(TAG, "Recieved JSON: " + jsonString);
                JSONObject jsonBody = new JSONObject(jsonString);
                WriteToFile(jsonBody, null, ctx);
                parseData(items, jsonBody);
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse JSON", je);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch items", ioe);
            }
            return items;
        }
        //TODO läsa från JSON fil
        else {
            try {
                parseData(items, ReadFromFile("titles", ctx));
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch items", ioe);
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse JSON", je);
            }
            return items;
        }
    }

    public List<String> fetchFromHref(String href, String title,
                                      boolean isConnected, Context ctx) {
        List<String> items = new ArrayList<>();

        if (isConnected) {
            try {
                String jsonString = getUrlString(href);
                Log.i(TAG, "Recieved JSON: " + jsonString);
                JSONObject jsonBody = new JSONObject(jsonString);
                WriteToFile(jsonBody, title, ctx);
                parseHref(items, jsonBody);
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse JSON", je);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch items", ioe);
            }

            return items;
        }
        //TODO läsa från JSON fil
        else {
            try {
                parseHref(items, ReadFromFile(title, ctx));
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch items", ioe);
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse JSON", je);
            }
            return items;
        }
    }

    private void WriteToFile(JSONObject jsonBody, String title, Context ctx) {
        try {
            OutputStreamWriter outputStreamWriter;
            if (title == null) {
                outputStreamWriter =
                        new OutputStreamWriter(ctx.openFileOutput
                                ("titles.json", Context.MODE_PRIVATE));
            }
            else {
                outputStreamWriter =
                        new OutputStreamWriter(ctx.openFileOutput
                                (title + ".json", Context.MODE_PRIVATE));
            }
            outputStreamWriter.write(jsonBody.toString());
            outputStreamWriter.close();
            Log.i("File Writing", "Successfully wrote Titles JSON to file");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Exception", "File Write Failed: " + e.toString());
        }
    }

    private JSONObject ReadFromFile(String title, Context ctx){
        String ret = "";
        JSONObject jsonBody = null;

        try {
            InputStream inputStream = ctx.openFileInput(title + ".json");

            if (inputStream != null){
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String recieveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((recieveString = bufferedReader.readLine()) != null){
                    stringBuilder.append(recieveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
                jsonBody = new JSONObject(ret);
            }
        } catch (FileNotFoundException e){
            Log.e("FileError", "File not found: " + e.toString());
        } catch (IOException ioe){
            Log.e("FileError", "Cannot read file: " + ioe.toString());
        } catch (JSONException je){
            Log.e("JsonError", "Failed to read JSON: " + je.toString());
        }
        return jsonBody;
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
