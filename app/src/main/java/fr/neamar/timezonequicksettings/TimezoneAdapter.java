package fr.neamar.timezonequicksettings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by neamar on 27/01/18.
 */

public class TimezoneAdapter extends ArrayAdapter<String> {
    final static String TAG = "TimezoneAdapter";
    final static String NESTED_INDICATOR = " / ...";

    // all available timezones
    private final List<String> allOptions;

    // options currently displayed to the user, based on filterPrefix
    private final List<String> currentOptions;

    TimezoneAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_dropdown_item_1line);

        allOptions = Arrays.asList(context.getResources().getStringArray(R.array.timezone_names));
        currentOptions = new ArrayList<>();
        applyPrefix("");
    }

    void applyPrefix(String filterPrefix) {
        Log.i(TAG, "Filtering list on " + filterPrefix);
        currentOptions.clear();

        String currentPrefix = "";
        for (String tz : allOptions) {
            if (tz.startsWith(filterPrefix)) {
                tz = tz.replace(filterPrefix, "");
                if (tz.contains("/")) {
                    String prefix = tz.substring(0, tz.indexOf("/"));
                    if (prefix.equals(currentPrefix)) {
                        // We've already added this prefix
                        continue;
                    }
                    currentPrefix = prefix;
                    currentOptions.add(currentPrefix + NESTED_INDICATOR);
                }
                else {
                    currentOptions.add(tz);
                }
            }
        }

        this.clear();
        this.addAll(currentOptions);
    }
}
