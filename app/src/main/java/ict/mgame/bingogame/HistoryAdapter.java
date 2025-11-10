package ict.mgame.bingogame;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * HistoryAdapter class is a RecyclerView adapter for displaying GameInfo items.
 * It binds data to views and handles timestamp formatting.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // List of GameInfo objects to display
    private List<GameInfo> gameInfoList;

    /**
     * Constructor for HistoryAdapter.
     * @param gameInfoList List of GameInfo to adapt
     */
    public HistoryAdapter(List<GameInfo> gameInfoList) {
        this.gameInfoList = gameInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        // Create and return ViewHolder
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get GameInfo at position
        GameInfo info = gameInfoList.get(position);
        // Bind data to TextViews
        holder.tvUsername.setText("Username: " + info.getUsername());
        holder.tvRound.setText("Round: " + info.getRound());
        holder.tvWinningNumbers.setText("Winning Numbers: " + info.getWinningNumbers());

        // Format timestamp to readable date/time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(new Date(info.getTimestamp()));
        holder.tvTimestamp.setText("Time: " + formattedTime);
    }

    @Override
    public int getItemCount() {
        // Return the size of the list
        return gameInfoList.size();
    }

    /**
     * ViewHolder class to hold references to item views.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvRound, tvWinningNumbers, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize TextViews
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvRound = itemView.findViewById(R.id.tv_round);
            tvWinningNumbers = itemView.findViewById(R.id.tv_winning_numbers);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}