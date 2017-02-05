package fr.neamar.timezonequicksettings;

import android.content.SharedPreferences;
import android.service.quicksettings.TileService;
import android.util.Log;

public class TimezoneTileService extends TileService {
    public static final String TAG = "TimezoneTileService";

    public static final String PREFS_NAME = "timezone_tile_service";

    @Override
    public void onTileAdded() {
        Log.i(TAG, "Tile added.");

        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        Log.i(TAG, "Tile removed.");

        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        Log.i(TAG, "Start listening.");
        SharedPreferences sp = getBaseContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String timezoneToUse = sp.getString("timezone", "");

        if(timezoneToUse.isEmpty()) {
            Log.i(TAG, "Timezone not defined yet.");
            getQsTile().setLabel(getString(R.string.tile_label_unitialized));
        }

        getQsTile().updateTile();
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        Log.i(TAG, "Stop listening.");
        super.onStopListening();
    }
}
