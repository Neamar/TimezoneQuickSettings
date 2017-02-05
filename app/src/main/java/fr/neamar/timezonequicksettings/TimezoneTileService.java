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
import android.os.Handler;
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
    private String lastKnownTime = "";


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

        return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
    }

    /**
     * Return a bitmap whose content is the text passed as a parameter, drawn in the bitmap
     *
     * @param text probably the time to display
     * @return bitmap to be used as an icon
     */
    private Bitmap getBitmap(String text) {

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

    /**
     * Update the tile,
     * Display the time if a timezone has been set,
     * Otherwise display a message asking the user to define timezone
     */
    private void updateTile() {
        if (!isListening) {
            return;
        }

        // Retrieve tile, abort if not tile currently displayed
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        // Retrieve timezone, abort if no timezone set
        String timezoneToUse = getTimezone();
        if (timezoneToUse.isEmpty()) {
            return;
        }

        // Retrieve current time in specified timezone, abort if time already displayed
        String time = getTime(timezoneToUse);
        if (time.equals(lastKnownTime)) {
            return;
        }

        // Otherwise, update tile!
        tile.setLabel(getTimezoneName());
        tile.setIcon(Icon.createWithBitmap(getBitmap(time)));
        lastKnownTime = time;

        // And send to screen
        try {
            tile.updateTile();
        } catch (NullPointerException e) {
            // Drawing to a bitmap can take time, and when drag-dropping the quicksettings into the active part (on init) we might get a NullPointerException on clearPendingBing()
            e.printStackTrace();
        }

    }

    /**
     * Update the tile, and schedule next update in a couple seconds
     */
    private void regularlyUpdateTile() {
        // Do not register a new handler if we're not listening right now
        if (!isListening) {
            return;
        }

        // Update tile content
        updateTile();

        // Register a new update
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                regularlyUpdateTile();
            }
        }, 5000);
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

        // handle the empty use case
        if (getTimezone().isEmpty()) {
            Tile tile = getQsTile();
            tile.setLabel(getString(R.string.tile_label_unitialized));
            tile.setIcon(Icon.createWithResource(getBaseContext(), R.drawable.tile_icon_before_add));
            tile.updateTile();
        }

        // Ensure we update the content regularly while we're listening
        regularlyUpdateTile();

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
