
package net.dto;

public class LeaderboardEntry {
    private String name;
    private int score;
    public LeaderboardEntry() {}  // for Gson
    public LeaderboardEntry(String name, int score){this.name=name;this.score=score;}
    public String getName(){return name;}
    public int getScore(){return score;}
}
