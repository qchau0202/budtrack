package vn.edu.tdtu.lhqc.budtrack.controllers.exchangerate;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;

/**
 * Service for fetching exchange rates from external API.
 * Uses exchangerate-api.com (free tier, no API key required for basic usage).
 */
public final class ExchangeRateService {

    private static final String TAG = "ExchangeRateService";
    // Using exchangerate-api.com free tier (no API key needed for basic usage)
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD";

    private ExchangeRateService() {
    }

    /**
     * Fetch USD to VND exchange rate from external API
     * @param context Application context
     * @return Exchange rate (USD to VND) or -1 if failed
     */
    public static float fetchExchangeRate(Context context) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject rates = jsonResponse.getJSONObject("rates");
                
                // Get VND rate (1 USD = X VND)
                if (rates.has("VND")) {
                    float rate = (float) rates.getDouble("VND");
                    Log.d(TAG, "Fetched exchange rate: 1 USD = " + rate + " VND");
                    return rate;
                } else {
                    Log.e(TAG, "VND rate not found in API response");
                    return -1;
                }
            } else {
                Log.e(TAG, "API request failed with code: " + responseCode);
                return -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching exchange rate", e);
            return -1;
        }
    }

    /**
     * Update exchange rate from API and save to settings
     * @param context Application context
     * @return true if successful, false otherwise
     */
    public static boolean updateExchangeRateFromAPI(Context context) {
        float rate = fetchExchangeRate(context);
        if (rate > 0) {
            SettingsHandler.setExchangeRate(context, rate);
            Log.d(TAG, "Exchange rate updated successfully: " + rate);
            return true;
        }
        return false;
    }
}

