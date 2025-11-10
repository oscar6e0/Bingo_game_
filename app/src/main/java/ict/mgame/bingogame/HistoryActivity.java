package ict.mgame.bingogame;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * HistoryActivity class displays the history of Bingo wins.
 * It loads data from the database, populates a RecyclerView, and provides a back button.
 */
public class HistoryActivity extends Activity {

    // RecyclerView for displaying history items
    private RecyclerView recyclerHistory;
    // Database helper instance
    private DBHelper dbHelper;
    // List to hold GameInfo objects
    private List<GameInfo> gameInfoList = new ArrayList<>();
    // Adapter for the RecyclerView
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the history layout
        setContentView(R.layout.activity_history);

        // Initialize UI elements
        recyclerHistory = findViewById(R.id.recycler_history);
        Button btnBack = findViewById(R.id.btn_back_history);

        // Initialize database helper
        dbHelper = new DBHelper(this);

        // Set up RecyclerView with linear layout manager
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        // Create and set adapter
        adapter = new HistoryAdapter(gameInfoList);
        recyclerHistory.setAdapter(adapter);

        // Load history data from database
        loadHistory();

        // Set click listener for back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish activity and return
                finish();
            }
        });
    }

    /**
     * Loads all game history from the database into the list and notifies adapter.
     */
    private void loadHistory() {
        // Clear existing list
        gameInfoList.clear();
        // Get cursor with all records
        Cursor cursor = dbHelper.getAllGameInfo();
        if (cursor.moveToFirst()) {
            do {
                // Extract data from cursor
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME));
                int round = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ROUND));
                String winningNumbers = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_WINNING_NUMBERS));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
                // Add to list
                gameInfoList.add(new GameInfo(username, round, winningNumbers, timestamp));
            } while (cursor.moveToNext());
        }
        // Close cursor
        cursor.close();
        // Notify adapter of data change
        adapter.notifyDataSetChanged();
    }
}