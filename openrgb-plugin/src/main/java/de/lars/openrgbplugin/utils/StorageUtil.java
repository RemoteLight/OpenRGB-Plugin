package de.lars.openrgbplugin.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.lars.openrgbplugin.OpenRgbPlugin;
import de.lars.remotelightcore.settings.SettingsManager;
import de.lars.remotelightcore.settings.types.SettingObject;

import java.util.HashSet;
import java.util.Set;

public class StorageUtil {

    public static final String SETTING_DATALIST = OpenRgbPlugin.SETTING_PRE + "outputs";
    public static final String KEY_NAME = "name";
    public static final String KEY_OUTPUTID = "output_id";
    public static final String KEY_ORGB_IP = "openrgb_ip";
    public static final String KEY_ORGB_PORT = "openrgb_port";
    public static final String KEY_ORGB_DEVICEID = "openrgb_deviceid";

    /**
     * Load stored values from settings manager and parse data
     * @param sm        settings manager to get the data from
     * @return          a Set containing ValueHolders
     */
    public static Set<ValueHolder> loadData(SettingsManager sm) {
        Set<ValueHolder> setValues = new HashSet<>();
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
            String orgbIp = jsonObj.get(KEY_ORGB_IP).getAsString();
            int port = jsonObj.get(KEY_ORGB_PORT).getAsInt();
            int deviceId = jsonObj.get(KEY_ORGB_DEVICEID).getAsInt();

            // create ValueHolder
            setValues.add(new ValueHolder(name, outputId, orgbIp, port, deviceId));
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
            jsonObj.addProperty(KEY_ORGB_IP, holder.getOrgbIp());
            jsonObj.addProperty(KEY_ORGB_PORT, holder.getOrgbPort());
            jsonObj.addProperty(KEY_ORGB_DEVICEID, holder.getDeviceId());

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
