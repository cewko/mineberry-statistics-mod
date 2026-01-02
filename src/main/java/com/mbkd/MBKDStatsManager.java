package com.mbkd;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MBKDStatsManager {
    
    private final ConcurrentHashMap<String, String> kdCache = new ConcurrentHashMap<>();
    private final Set<String> pendingPlayers = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Gson gson = new Gson();
    private final AtomicBoolean cancelFlag = new AtomicBoolean(false);
    private Future<?> currentTask = null;
    
    public String getKD(String playerName) {
        return kdCache.get(playerName.toLowerCase());
    }
    
    public boolean hasKD(String playerName) {
        return kdCache.containsKey(playerName.toLowerCase());
    }
    
    public int getCacheSize() {
        return kdCache.size();
    }
    
    public void clearCache() {
        kdCache.clear();
        pendingPlayers.clear();
    }
    
    public boolean isAPIOnline() {
        try {
            URL url = new URL(MBKDConfig.apiUrl + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void cancelPending() {
        cancelFlag.set(true);
        pendingPlayers.clear();
        if (currentTask != null) {
            currentTask.cancel(true);
        }
        executor.submit(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            cancelFlag.set(false);
        });
    }
    
    public void fetchPlayers(List<String> players) {
        if (!MBKDConfig.enabled) return;
        
        List<String> toFetch = new ArrayList<>();
        for (String name : players) {
            String lower = name.toLowerCase();
            if (!kdCache.containsKey(lower) && !pendingPlayers.contains(lower)) {
                toFetch.add(name);
                pendingPlayers.add(lower);
            }
        }
        
        if (toFetch.isEmpty()) {
            System.out.println("[MBKD] No new players to fetch");
            return;
        }
        
        System.out.println("[MBKD] Sending API request for " + toFetch.size() + " players: " + toFetch);
        
        cancelFlag.set(false);
        
        currentTask = executor.submit(() -> {
            try {
                if (cancelFlag.get()) return;
                
                Map<String, String> results = fetchFromAPI(toFetch);
                
                if (cancelFlag.get()) return;
                
                System.out.println("[MBKD] Got results: " + results);
                
                for (Map.Entry<String, String> entry : results.entrySet()) {
                    kdCache.put(entry.getKey().toLowerCase(), entry.getValue());
                    pendingPlayers.remove(entry.getKey().toLowerCase());
                }
                
            } catch (Exception e) {
                System.err.println("[MBKD] Error fetching stats: " + e.getMessage());
                e.printStackTrace();
                for (String name : toFetch) {
                    pendingPlayers.remove(name.toLowerCase());
                }
            }
        });
    }
    
    private Map<String, String> fetchFromAPI(List<String> players) throws IOException {
        URL url = new URL(MBKDConfig.apiUrl + "/stats");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            
            JsonObject body = new JsonObject();
            body.add("players", gson.toJsonTree(players));
            
            String bodyStr = body.toString();
            System.out.println("[MBKD] Request body: " + bodyStr);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyStr.getBytes("UTF-8"));
            }
            
            int responseCode = conn.getResponseCode();
            System.out.println("[MBKD] Response code: " + responseCode);
            
            if (responseCode != 200) {
                throw new IOException("API returned code " + responseCode);
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                System.out.println("[MBKD] Response: " + response.toString());
                
                java.lang.reflect.Type type = new TypeToken<Map<String, String>>(){}.getType();
                return gson.fromJson(response.toString(), type);
            }
            
        } finally {
            conn.disconnect();
        }
    }
}