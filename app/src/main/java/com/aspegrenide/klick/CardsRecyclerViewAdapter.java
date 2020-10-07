package com.aspegrenide.klick;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardsRecyclerViewAdapter extends RecyclerView.Adapter<CardsRecyclerViewAdapter.ViewHolder> {

    private static final String LOG_TAG = "KLICK Recycler";
    private List<CardDetails> cardDetails;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    // data is passed into the constructor
    CardsRecyclerViewAdapter(Context context, List<CardDetails> data) {
        this.mInflater = LayoutInflater.from(context);
        this.cardDetails = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.card_details, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = cardDetails.get(position).getName();
        String uri = cardDetails.get(position).getUri();
        String action = cardDetails.get(position).getAction();
        String imgUrl = cardDetails.get(position).getImgUrl();
        if (imgUrl != null) {
            int drawableId = context.getResources().getIdentifier(imgUrl, "drawable",
                    context.getPackageName());
            holder.imageView.setImageResource(drawableId);
        }
        holder.tvCardName.setText(name);
        holder.tvCardUri.setText(uri);
        holder.tvCardAction.setText(action);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return cardDetails.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvCardName;
        TextView tvCardUri;
        TextView tvCardAction;
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            tvCardName = itemView.findViewById(R.id.tvName);
            tvCardUri = itemView.findViewById(R.id.tvUri);
            tvCardAction = itemView.findViewById(R.id.tvAction);
            imageView = itemView.findViewById(R.id.ivLogo);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            String carduid = cardDetails.get(getAdapterPosition()).getCardId();
            String cardName = cardDetails.get(getAdapterPosition()).getName();
            //goHome();
            Toast.makeText(context, "clicked carduid = " + carduid + " name " + cardName, Toast.LENGTH_SHORT).show();
            callAppstarter(carduid);
        }
    }

    public void goHome() {
        Log.d(LOG_TAG, "Go home ");

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MainActivity.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, i, 0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        4 * 1000, alarmIntent);
    }

    private void callAppstarter(String cardUid) {
        Log.d("USB Listener", "cardUid" + cardUid);
        Intent startAppIntent = new Intent(context, AppstarterService.class);
        startAppIntent.putExtra("CARDUID", cardUid);
        context.startService(startAppIntent);
    }

    // convenience method for getting data at click position
    CardDetails getItem(int id) {
        return cardDetails.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
