package com.superhexagon.util;

import com.google.gson.Gson;
import com.superhexagon.model.Settings;

import java.io.*;

public class SettingsManager {
    private static final String SETTINGS_FILE = "settings.json";
    private Settings settings;
    private Gson gson;

    public SettingsManager() {
        this.gson = new Gson();
        this.settings = new Settings();
        loadSettings();
    }

    public Settings getSettings() {
        return settings;
    }

    public void saveSettings() {
        try (Writer writer = new FileWriter(SETTINGS_FILE)) {
            gson.toJson(settings, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Settings loadedSettings = gson.fromJson(reader, Settings.class);
                if (loadedSettings != null) {
                    this.settings = loadedSettings;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}