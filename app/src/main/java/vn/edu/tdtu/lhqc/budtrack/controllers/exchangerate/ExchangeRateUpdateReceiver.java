package vn.edu.tdtu.lhqc.budtrack.controllers.exchangerate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;
import vn.edu.tdtu.lhqc.budtrack.services.ExchangeRateUpdateService;

/**
 * BroadcastReceiver for automatically updating exchange rates weekly.
 * Starts the ExchangeRateUpdateService to handle the update in the background.
 */
public class ExchangeRateUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "ExchangeRateUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Exchange rate update triggered");
        
        // Start the service to fetch and update exchange rate
        Intent serviceIntent = ExchangeRateUpdateService.createUpdateIntent(context);
        context.startService(serviceIntent);
        
        // Reschedule for next week
        SettingsHandler.scheduleWeeklyExchangeRateUpdate(context);
    }
}

