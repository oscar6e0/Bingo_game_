package ict.mgame.bingogame;

/**
 * GameInfo class is a data model for storing Bingo game information.
 * It holds username, round number, winning numbers as a string, and timestamp.
 * Used for mapping to/from database records.
 */
public class GameInfo {
    // Username of the player
    private String username;
    // Round number of the game
    private int round;
    // Comma-separated string of winning numbers (e.g., "1,16,FREE,46,61")
    private String winningNumbers;  // Comma-separated string of the 5 winning numbers (e.g., "1,16,FREE,46,61")
    // Timestamp of when the game was won (in milliseconds)
    private long timestamp;  // System time in milliseconds

    /**
     * Constructor to create a GameInfo object.
     * @param username Player's username
     * @param round Game round number
     * @param winningNumbers Winning line numbers as string
     * @param timestamp Time of win
     */
    public GameInfo(String username, int round, String winningNumbers, long timestamp) {
        this.username = username;
        this.round = round;
        this.winningNumbers = winningNumbers;
        this.timestamp = timestamp;
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Setter for username
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter for round
    public int getRound() {
        return round;
    }

    // Setter for round
    public void setRound(int round) {
        this.round = round;
    }

    // Getter for winning numbers
    public String getWinningNumbers() {
        return winningNumbers;
    }

    // Setter for winning numbers
    public void setWinningNumbers(String winningNumbers) {
        this.winningNumbers = winningNumbers;
    }

    // Getter for timestamp
    public long getTimestamp() {
        return timestamp;
    }

    // Setter for timestamp
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}