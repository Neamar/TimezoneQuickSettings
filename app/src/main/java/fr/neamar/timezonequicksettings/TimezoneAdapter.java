package fr.neamar.timezonequicksettings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by neamar on 27/01/18.
 */

public class TimezoneAdapter extends ArrayAdapter<String> implements SectionIndexer {
    final static String TAG = "TimezoneAdapter";
    final static String NESTED_INDICATOR = " / ...";

    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;

    String prefix;

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
        this.prefix = filterPrefix;

        currentOptions.clear();

        String currentPrefix = "";
        for (String tz : allOptions) {
            if (tz.startsWith(filterPrefix)) {
                tz = tz.replace(filterPrefix, "");
                tz = tz.replaceAll("_" , " ");
                if (tz.contains("/")) {
                    String prefix = tz.substring(0, tz.indexOf("/"));
                    if (prefix.equals(currentPrefix)) {
                        // We've already added this prefix
                        continue;
                    }
                    currentPrefix = prefix;
                    currentOptions.add(currentPrefix + NESTED_INDICATOR);
                } else {
                    currentOptions.add(tz);
                }
            }
        }

        this.clear();
        this.addAll(currentOptions);
    }

    String getFullName(int position) {
        this.notifyDataSetChanged();
        return (this.prefix + getItem(position)).replaceAll(" ", "_");
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        alphaIndexer = new HashMap<String, Integer>();
        int size = currentOptions.size();

        for (int x = 0; x < size; x++) {
            String s = currentOptions.get(x);

            // get the first letter of the store
            String ch =  s.substring(0, 1);
            // convert to uppercase otherwise lowercase a -z will be sorted after upper A-Z
            ch = ch.toUpperCase();

            // HashMap will prevent duplicates
            alphaIndexer.put(ch, x);
        }

        Set<String> sectionLetters = alphaIndexer.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
        Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        sectionList.toArray(sections);
    }

    public int getPositionForSection(int section) {
        return alphaIndexer.get(sections[section]);
    }

    public int getSectionForPosition(int position) {
        return 1;
    }

    public Object[] getSections() {
        return sections;
    }
}
