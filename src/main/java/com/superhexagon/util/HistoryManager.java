package com.superhexagon.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.superhexagon.model.GameRecord;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private List<GameRecord> records;
    private static final String HISTORY_FILE = "history.json";
    private final Gson gson;

    public HistoryManager() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        this.gson = gsonBuilder.setPrettyPrinting().create();

        records = new ArrayList<>();
        loadHistory();
    }

    public void addRecord(GameRecord record) {
        records.add(record);
        saveHistory();
    }

    public List<GameRecord> getRecords() {
        return new ArrayList<>(records);
    }

    private void loadHistory() {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            System.out.println("History file does not exist: " + HISTORY_FILE);
            return;
        }

        try (Reader reader = new FileReader(file)) {
            List<GameRecord> loadedRecords = gson.fromJson(reader, new TypeToken<List<GameRecord>>(){}.getType());
            if (loadedRecords != null) {
                records = loadedRecords;
                System.out.println("Loaded " + records.size() + " records from history.json");
            } else {
                System.out.println("No records loaded from history.json (loadedRecords is null)");
            }
        } catch (IOException e) {
            System.err.println("Error loading history: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error loading history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveHistory() {
        try (Writer writer = new FileWriter(HISTORY_FILE)) {
            gson.toJson(records, writer);
            System.out.println("Saved " + records.size() + " records to history.json");
        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }
}