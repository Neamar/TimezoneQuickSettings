package fr.neamar.timezonequicksettings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimezoneTileService extends TileService {
    public static final String TAG = "TimezoneTileService";

    public static final String PREFS_NAME = "timezone_tile_service";
    public static final String TIMEZONE_KEY = "timezone";
    public static final String TIMEZONE_NAME_KEY = "timezone_display_name";

    private SharedPreferences sp = null;

    private boolean isListening = false;


    private SharedPreferences getSharedPreferences() {
        if (sp == null) {
            sp = getBaseContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        }

        return sp;
    }

    private String getTimezone() {
        return getSharedPreferences().getString(TIMEZONE_KEY, "");
    }

    private String getTimezoneName() {
        return getSharedPreferences().getString(TIMEZONE_NAME_KEY, "");
    }

    private Dialog getTimezoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
        builder.setTitle(R.string.pick_timezone)
                .setItems(R.array.timezone_names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        String[] timezones = getResources().getStringArray(R.array.timezone_names);
                        String timezone = timezones[which];
                        Log.i(TAG, "Setting timezone to " + timezone);

                        SharedPreferences.Editor editor = getSharedPreferences().edit();
                        editor.putString(TIMEZONE_KEY, timezone);
                        String timezoneName = timezone.replaceAll("^.+/", "");
                        editor.putString(TIMEZONE_NAME_KEY, timezoneName);

                        editor.apply();

                        Toast.makeText(getBaseContext(), String.format(getString(R.string.new_timezone_toast), timezoneName), Toast.LENGTH_SHORT).show();

                        updateTile();
                    }
                });
        return builder.create();
    }

    private String getTime(String timezone) {
        TimeZone tz = TimeZone.getTimeZone(timezone);
        Calendar calendar = new GregorianCalendar(tz);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return hour + ":" + (minute < 10 ? "0" + minute : minute);
    }

    private Bitmap getBitmap(String timezone) {
        String text = getTime(timezone);

        Bitmap bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ALPHA_8);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setTextSize(70);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;

        canvas.drawText(text, x, y, paint);

        return bitmap;
    }

    private void updateTile() {
        if (!isListening) {
            return;
        }

        String timezoneToUse = getTimezone();


        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        if (timezoneToUse.isEmpty()) {
            Log.i(TAG, "Timezone not defined yet.");
            tile.setLabel(getString(R.string.tile_label_unitialized));
        } else {
            tile.setLabel(getTimezoneName());
            tile.setIcon(Icon.createWithBitmap(getBitmap(timezoneToUse)));
        }

        try {
            getQsTile().updateTile();
        } catch (NullPointerException e) {
            // Drawing to a bitmap can take time, and when drag-dropping the quicksettings into the active part (on init) we might get a NullPointerException on clearPendingBing()
            e.printStackTrace();
        }
    }

    @Override
    public void onTileAdded() {
        Log.i(TAG, "Tile added.");

        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        Log.i(TAG, "Tile removed.");
        getSharedPreferences().edit().clear().apply();

        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        Log.i(TAG, "Start listening.");
        isListening = true;

        updateTile();

        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        Log.i(TAG, "Stop listening.");
        isListening = false;

        super.onStopListening();
    }

    @Override
    public void onClick() {
        Log.i(TAG, "Tile clicked.");
        showDialog(getTimezoneDialog());

        super.onClick();
    }
}
