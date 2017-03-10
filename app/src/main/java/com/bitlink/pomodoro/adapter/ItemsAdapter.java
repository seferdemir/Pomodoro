package com.bitlink.pomodoro.adapter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bitlink.pomodoro.R;
import com.bitlink.pomodoro.activity.MainActivity;
import com.bitlink.pomodoro.activity.TimerActivity;
import com.bitlink.pomodoro.database.DatabaseHelper;
import com.bitlink.pomodoro.database.model.Item;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Item> itemList;
    private Item item;
    private OnItemSelectedListener listener;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, time_tv;
        ImageView overflow;
        RelativeLayout cardLayout;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            time_tv = (TextView) view.findViewById(R.id.time);
            overflow = (ImageView) view.findViewById(R.id.overflow);
            cardLayout = (RelativeLayout) itemView.findViewById(R.id.card_layout);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item = itemList.get(getPosition());
                    Intent mIntent = new Intent(mContext.getApplicationContext(), TimerActivity.class);
                    mIntent.putExtra(TimerActivity.ARG_ITEM, item.getId());
//                    mIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // it acts like main class
//                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(mIntent);
//                    ActivityCompat.finishAffinity((Activity) mContext);
                }
            });
        }
    }


    public ItemsAdapter(Context mContext, List<Item> itemList) {
        this.mContext = mContext;
        this.listener = (OnItemSelectedListener) mContext;
        this.itemList = itemList;
        setHasStableIds(true);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        item = itemList.get(position);
        holder.title.setText(item.getTaskName());
        holder.cardLayout.setBackgroundColor(item.getWorkSessionColor());

        if (holder.title.getText().length() == 0)
            holder.title.setVisibility(View.GONE);

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder.overflow, position);
            }
        });

}

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view, final int position) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_card, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                listener.onItemSelected(item, position);
                return false;
            }
        });
        popup.show();
    }

public interface OnItemSelectedListener {
    public void onItemSelected(MenuItem item, int position);

}

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
