
package net;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dto.LeaderboardEntry;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NetUtil {
    private static final String BASE = "http://localhost:8080";
    private static final Gson G = new Gson();

    public static void postScore(String name, int score) throws IOException {
        URL url = new URL(BASE + "/scores");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(G.toJson(new LeaderboardEntry(name, score)).getBytes(StandardCharsets.UTF_8));
        }
        if (conn.getResponseCode() != 200)
            throw new IOException("POST /scores failed: HTTP " + conn.getResponseCode());
    }

    public static List<LeaderboardEntry> fetchTopScores() throws IOException {
        URL url = new URL(BASE + "/scores/json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try (Reader r = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            return G.fromJson(r, new TypeToken<List<LeaderboardEntry>>(){}.getType());
        }
    }

    public static String getBase() { return BASE; }
}
