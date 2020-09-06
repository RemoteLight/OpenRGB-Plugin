package de.lars.openrgbplugin.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import de.lars.openrgbplugin.OpenRgbPlugin;
import de.lars.remotelightcore.settings.SettingsManager;
import de.lars.remotelightcore.settings.types.SettingObject;

import java.lang.reflect.Type;
import java.util.*;

public class StorageUtil {

    public static final String SETTING_DATALIST = OpenRgbPlugin.SETTING_PRE + "outputs";
    public static final String KEY_NAME = "name";
    public static final String KEY_OUTPUTID = "output_id";
    public static final String KEY_ORGB_DEVICES = "openrgb_devices";

    /**
     * Load stored values from settings manager and parse data
     * @param sm        settings manager to get the data from
     * @return          a Set containing ValueHolders
     */
    public static Set<ValueHolder> loadData(SettingsManager sm) {
        Set<ValueHolder> setValues = new LinkedHashSet<>();
        SettingObject settingJson = sm.getSettingObject(SETTING_DATALIST);
        if(settingJson == null)
            return setValues; // return empty set when no data exists

        if(!(settingJson.getValue() instanceof String))
            return setValues; // return empty set when data is invalid

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();

        // parse json array
        JsonArray jsonRoot = gson.fromJson((String) settingJson.getValue(), JsonArray.class);
        // build all values from json data
        for(int i = 0; i < jsonRoot.size(); i++) {
            JsonObject jsonObj = jsonRoot.get(i).getAsJsonObject();

            String name = jsonObj.get(KEY_NAME).getAsString();
            String outputId = jsonObj.get(KEY_OUTPUTID).getAsString();
            Type listType = new TypeToken<List<Integer>>() {}.getType();
            List<Integer> devices = gson.fromJson(jsonObj.get(KEY_ORGB_DEVICES), listType);

            // create ValueHolder
            setValues.add(new ValueHolder(name, outputId, devices));
        }
        return setValues;
    }

    public static void storeData(SettingsManager sm, Set<ValueHolder> setValues) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();

        JsonArray jsonRoot = new JsonArray();
        for(ValueHolder holder : setValues) {
            JsonObject jsonObj = new JsonObject();

            // store data in json object
            jsonObj.addProperty(KEY_NAME, holder.getName());
            jsonObj.addProperty(KEY_OUTPUTID, holder.getOutputId());
            Type listType = new TypeToken<List<Integer>>() {}.getType();
            jsonObj.add(KEY_ORGB_DEVICES, gson.toJsonTree(holder.getDevices(), listType));

            // add to json root array
            jsonRoot.add(jsonObj);
        }

        // create json string
        String jsonData = gson.toJson(jsonRoot);
        // store data as setting
        SettingObject settingJson = sm.addSetting(new SettingObject(SETTING_DATALIST, "OpenRGB Plugin Data", null), false);
        settingJson.setValue(jsonData);
    }

}
