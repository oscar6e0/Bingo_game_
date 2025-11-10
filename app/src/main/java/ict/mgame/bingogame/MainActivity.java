package ict.mgame.bingogame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * MainActivity class handles the core functionality of the Bingo game.
 * It manages the UI elements, game logic for drawing numbers, checking for Bingo,
 * auto-draw feature, generating new cards, and integrating with database for history.
 */
public class MainActivity extends Activity {

    // 2D array for TextViews representing the Bingo card cells
    private TextView[][] cardViews = new TextView[5][5];
    // 2D array storing the numbers on the Bingo card
    private int[][] cardNumbers = new int[5][5];
    // 2D boolean array tracking which cells are marked (hit by drawn numbers)
    private boolean[][] marked = new boolean[5][5];
    // List of available numbers to draw (1-75, shuffled)
    private List<Integer> availableNumbers = new ArrayList<>();
    // TextView to display the last drawn number
    private TextView tvDrawn;
    // Buttons for user interactions: manual draw, auto draw, new card, config, history, back
    private Button btnDraw, btnAutoDraw, btnGenerateNew, btnConfig, btnBack, btnHistory;
    // Container LinearLayout for dynamically adding Bingo card rows
    private LinearLayout bingoCardContainer;
    // Handler for scheduling auto-draw tasks
    private Handler autoDrawHandler = new Handler();
    // Flag to track if auto-draw is active
    private boolean isAutoDrawing = false;
    // Database helper for storing game info
    private DBHelper dbHelper;
    // SharedPreferences for accessing user login data (e.g., username)
    private SharedPreferences sharedPreferences;
    // Current round number, starts at 0 and increments per new game
    private int currentRound = 0;  // Starts at 0, increments on new game
    // Flag to ensure game info is stored only once per Bingo win
    private boolean hasStoredBingo = false;  // Prevent multiple inserts per game

    // Runnable for auto-drawing numbers every 3 seconds
    private final Runnable autoDrawRunnable = new Runnable() {
        @Override
        public void run() {
            // Check if auto-draw is still active and numbers are available
            if (isAutoDrawing && !availableNumbers.isEmpty()) {
                // Draw a number
                drawNumber();
                // If no Bingo yet, schedule the next draw after 3 seconds
                if (!hasBingo()) {
                    autoDrawHandler.postDelayed(this, 3000);  // 3-second delay
                } else {
                    // Stop auto-draw on Bingo
                    stopAutoDraw();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the main activity layout
        setContentView(R.layout.activity_main);

        // Initialize UI elements by finding them in the layout
        tvDrawn = findViewById(R.id.tv_drawn);
        btnDraw = findViewById(R.id.btn_draw);
        btnAutoDraw = findViewById(R.id.btn_auto_draw);
        btnGenerateNew = findViewById(R.id.btn_generate_new);
        btnConfig = findViewById(R.id.btn_config);
        btnHistory = findViewById(R.id.btn_history);
        btnBack = findViewById(R.id.btn_back);
        bingoCardContainer = findViewById(R.id.bingo_card);

        // Initialize database helper
        dbHelper = new DBHelper(this);
        // Get shared preferences for login data
        sharedPreferences = getSharedPreferences("login.xml", MODE_PRIVATE);

        // Generate the initial Bingo card (increments round to 1)
        generateNewCard();  // Initial card generation, increments to round 1

        // Set click listener for manual draw button
        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Draw a number if available
                if (!availableNumbers.isEmpty()) {
                    drawNumber();
                } else {
                    // Show dialog if no more numbers
                    showNoMoreNumbersDialog();
                }
            }
        });

        // Set click listener for auto-draw toggle button
        btnAutoDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle auto-draw on/off
                if (isAutoDrawing) {
                    stopAutoDraw();
                } else {
                    startAutoDraw();
                }
            }
        });

        // Set click listener for generating a new card
        btnGenerateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop auto-draw if active, then generate new card
                if (isAutoDrawing) {
                    stopAutoDraw();
                }
                generateNewCard();
            }
        });

        // Set click listener for config button
        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast and start ConfigActivity
                Toast.makeText(MainActivity.this, "Opening Config", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent);
            }
        });

        // Set click listener for history button
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start HistoryActivity
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // Set click listener for back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop auto-draw if active, then finish activity
                if (isAutoDrawing) {
                    stopAutoDraw();
                }
                finish();  // Returns to previous activity (Login)
            }
        });
    }

    /**
     * Draws a random number from availableNumbers, updates UI, marks card if match,
     * and checks for Bingo. If Bingo, shows dialog and stores game info.
     */
    private void drawNumber() {
        // Select a random index from available numbers
        int drawnIndex = new Random().nextInt(availableNumbers.size());
        // Remove and get the drawn number
        int drawn = availableNumbers.remove(drawnIndex);
        // Update drawn text view
        tvDrawn.setText("Drawn: " + drawn);

        // Check and mark if the drawn number is on the card
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (cardNumbers[i][j] == drawn) {
                    marked[i][j] = true;
                    cardViews[i][j].setText("X");
                    cardViews[i][j].setBackgroundColor(Color.RED);
                }
            }
        }

        // Check for Bingo and store info if not already done
        if (hasBingo() && !hasStoredBingo) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Bingo! You win!")
                    .setPositiveButton("OK", null)
                    .show();

            // Store game info only once
            storeGameInfo();
            hasStoredBingo = true;
        }
    }

    /**
     * Stores the game information in the database upon Bingo win.
     * Includes username, round, winning numbers, and timestamp.
     */
    private void storeGameInfo() {
        // Get username from shared preferences
        String username = sharedPreferences.getString("username", "Unknown");
        // Get the winning line numbers as string
        String winningNumbers = getWinningNumbers();  // Get the first winning line's numbers
        // Current system time
        long timestamp = System.currentTimeMillis();

        // Create and insert GameInfo object
        GameInfo gameInfo = new GameInfo(username, currentRound, winningNumbers, timestamp);
        dbHelper.insertGameInfo(gameInfo);
    }

    /**
     * Retrieves the numbers from the first detected winning line (row, column, or diagonal)
     * as a comma-separated string, handling "FREE" for the center.
     * @return String of winning numbers
     */
    private String getWinningNumbers() {
        StringBuilder sb = new StringBuilder();

        // Check rows
        for (int i = 0; i < 5; i++) {
            if (isLineMarked(marked[i])) {
                for (int j = 0; j < 5; j++) {
                    if (i == 2 && j == 2) {
                        sb.append("FREE");
                    } else {
                        sb.append(cardNumbers[i][j]);
                    }
                    if (j < 4) sb.append(",");
                }
                return sb.toString();
            }
        }

        // Check columns
        for (int j = 0; j < 5; j++) {
            boolean colMarked = true;
            for (int i = 0; i < 5; i++) {
                if (!marked[i][j]) {
                    colMarked = false;
                    break;
                }
            }
            if (colMarked) {
                for (int i = 0; i < 5; i++) {
                    if (i == 2 && j == 2) {
                        sb.append("FREE");
                    } else {
                        sb.append(cardNumbers[i][j]);
                    }
                    if (i < 4) sb.append(",");
                }
                return sb.toString();
            }
        }

        // Check main diagonal
        boolean diag1 = true;
        for (int i = 0; i < 5; i++) {
            if (!marked[i][i]) diag1 = false;
        }
        if (diag1) {
            for (int i = 0; i < 5; i++) {
                if (i == 2) {
                    sb.append("FREE");
                } else {
                    sb.append(cardNumbers[i][i]);
                }
                if (i < 4) sb.append(",");
            }
            return sb.toString();
        }

        // Check anti-diagonal
        boolean diag2 = true;
        for (int i = 0; i < 5; i++) {
            if (!marked[i][4 - i]) diag2 = false;
        }
        if (diag2) {
            for (int i = 0; i < 5; i++) {
                if (i == 2 && (4 - i) == 2) {
                    sb.append("FREE");
                } else {
                    sb.append(cardNumbers[i][4 - i]);
                }
                if (i < 4) sb.append(",");
            }
            return sb.toString();
        }

        return "";  // Fallback, though shouldn't happen if hasBingo() is true
    }

    /**
     * Starts the auto-draw feature, updating button UI and scheduling draws.
     */
    private void startAutoDraw() {
        if (availableNumbers.isEmpty()) {
            showNoMoreNumbersDialog();
            return;
        }
        isAutoDrawing = true;
        btnAutoDraw.setText("Stop Auto Draw");
        btnAutoDraw.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
        autoDrawHandler.postDelayed(autoDrawRunnable, 3000);  // Start after 3 seconds
    }

    /**
     * Stops the auto-draw feature, updating button UI and removing scheduled tasks.
     */
    private void stopAutoDraw() {
        isAutoDrawing = false;
        btnAutoDraw.setText("Start Auto Draw");
        btnAutoDraw.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));
        autoDrawHandler.removeCallbacks(autoDrawRunnable);
    }

    /**
     * Shows a dialog when no more numbers are available to draw.
     */
    private void showNoMoreNumbersDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("No more numbers to draw!")
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Generates a new Bingo card with random numbers in standard ranges,
     * increments round, resets game state.
     */
    private void generateNewCard() {
        currentRound++;  // Increment round for new game
        hasStoredBingo = false;  // Reset for new game

        bingoCardContainer.removeAllViews();  // Clear existing card

        // Generate unique numbers for each column (B:1-15, I:16-30, etc.)
        List<Integer> bNums = generateUniqueNums(1, 15, 5);
        List<Integer> iNums = generateUniqueNums(16, 30, 5);
        List<Integer> nNums = generateUniqueNums(31, 45, 4);  // 4 because center is FREE
        List<Integer> gNums = generateUniqueNums(46, 60, 5);
        List<Integer> oNums = generateUniqueNums(61, 75, 5);

        // Build the 5x5 card row by row
        for (int rowIdx = 0; rowIdx < 5; rowIdx++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            // B column
            cardNumbers[rowIdx][0] = bNums.get(rowIdx);
            addCell(row, rowIdx, 0);

            // I column
            cardNumbers[rowIdx][1] = iNums.get(rowIdx);
            addCell(row, rowIdx, 1);

            // N column (center is FREE)
            if (rowIdx == 2) {
                cardNumbers[rowIdx][2] = 0;  // FREE
                TextView tv = createTextView("FREE", Color.RED);
                row.addView(tv);
                marked[rowIdx][2] = true;  // FREE is auto-marked
                cardViews[rowIdx][2] = tv;
            } else {
                cardNumbers[rowIdx][2] = nNums.get(rowIdx < 2 ? rowIdx : rowIdx - 1);
                addCell(row, rowIdx, 2);
            }

            // G column
            cardNumbers[rowIdx][3] = gNums.get(rowIdx);
            addCell(row, rowIdx, 3);

            // O column
            cardNumbers[rowIdx][4] = oNums.get(rowIdx);
            addCell(row, rowIdx, 4);

            bingoCardContainer.addView(row);
        }

        // Reset marks and available numbers
        resetMarks();
        resetAvailableNumbers();
        tvDrawn.setText("Drawn: ");
    }

    /**
     * Generates a list of unique shuffled numbers in a given range.
     * @param min Minimum number (inclusive)
     * @param max Maximum number (inclusive)
     * @param count Number of unique numbers to generate
     * @return List of unique numbers
     */
    private List<Integer> generateUniqueNums(int min, int max, int count) {
        List<Integer> nums = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            nums.add(i);
        }
        Collections.shuffle(nums);
        return nums.subList(0, count);
    }

    /**
     * Adds a cell (TextView) to a row with the number from cardNumbers.
     * @param row The LinearLayout row to add to
     * @param rowIdx Row index
     * @param colIdx Column index
     */
    private void addCell(LinearLayout row, int rowIdx, int colIdx) {
        TextView tv = createTextView(String.valueOf(cardNumbers[rowIdx][colIdx]), Color.BLACK);
        cardViews[rowIdx][colIdx] = tv;
        row.addView(tv);
    }

    /**
     * Creates a styled TextView for a Bingo cell.
     * @param text Text to display (number or "FREE")
     * @param textColor Color of the text
     * @return Configured TextView
     */
    private TextView createTextView(String text, int textColor) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(textColor);
        tv.setPadding(16, 16, 16, 16);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.WHITE);
        tv.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);  // Add border
        return tv;
    }

    /**
     * Resets all marks on the card except the FREE center, updates UI.
     */
    private void resetMarks() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                marked[i][j] = (i == 2 && j == 2);  // Only FREE is marked
                if (cardViews[i][j] != null) {
                    cardViews[i][j].setText(i == 2 && j == 2 ? "FREE" : String.valueOf(cardNumbers[i][j]));
                    cardViews[i][j].setBackgroundColor(Color.WHITE);
                }
            }
        }
    }

    /**
     * Resets the list of available numbers to 1-75, shuffled.
     */
    private void resetAvailableNumbers() {
        availableNumbers.clear();
        for (int i = 1; i <= 75; i++) {
            availableNumbers.add(i);
        }
        Collections.shuffle(availableNumbers);
    }

    /**
     * Checks if there is a Bingo (full row, column, or diagonal).
     * @return true if Bingo detected, false otherwise
     */
    private boolean hasBingo() {
        // Check rows
        for (int i = 0; i < 5; i++) {
            if (isLineMarked(marked[i])) return true;
        }

        // Check columns
        for (int j = 0; j < 5; j++) {
            boolean colMarked = true;
            for (int i = 0; i < 5; i++) {
                if (!marked[i][j]) {
                    colMarked = false;
                    break;
                }
            }
            if (colMarked) return true;
        }

        // Check main diagonal
        boolean diag1 = true;
        for (int i = 0; i < 5; i++) {
            if (!marked[i][i]) diag1 = false;
        }
        if (diag1) return true;

        // Check anti-diagonal
        boolean diag2 = true;
        for (int i = 0; i < 5; i++) {
            if (!marked[i][4 - i]) diag2 = false;
        }
        if (diag2) return true;

        return false;
    }

    /**
     * Checks if a single line (row or passed array) is fully marked.
     * @param line Boolean array representing the line
     * @return true if all marked, false otherwise
     */
    private boolean isLineMarked(boolean[] line) {
        for (boolean b : line) {
            if (!b) return false;
        }
        return true;
    }
}