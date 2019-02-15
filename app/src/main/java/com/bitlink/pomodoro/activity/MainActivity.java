package com.bitlink.pomodoro.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitlink.pomodoro.R;
import com.bitlink.pomodoro.adapter.ItemsAdapter;
import com.bitlink.pomodoro.database.DatabaseHelper;
import com.bitlink.pomodoro.database.model.Item;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ItemsAdapter.OnItemSelectedListener {

    private Context mContext;
    private DatabaseHelper mDbHelper = null;
    private RecyclerView mRecyclerView;
    private ItemsAdapter mAdapter;
    private List<Item> itemList = new ArrayList<>();
    private ImageView imageViewEmpty;
    private TextView textViewEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);

        imageViewEmpty = (ImageView) findViewById(R.id.iv_no_item);
        textViewEmpty = (TextView) findViewById(R.id.tv_no_items);

        mContext = this;
        mDbHelper = new DatabaseHelper(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(new Intent(mContext, ItemActivity.class));
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setNavigationBarColor(mContext.getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        itemList = mDbHelper.getItemList();

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter = new ItemsAdapter(this, itemList);
        mRecyclerView.setAdapter(mAdapter);

        if (itemList.size() == 0) {
            imageViewEmpty.setVisibility(View.VISIBLE);
            textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            imageViewEmpty.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemSelected(MenuItem menuItem, int position) {
        Item item = itemList.get(position);

        switch (menuItem.getItemId()) {
            case R.id.action_edit:
                Intent mIntent = new Intent(mContext.getApplicationContext(), ItemActivity.class);
                mIntent.putExtra(ItemActivity.ARG_ITEM, item.getId());
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(mIntent);
                return;
            case R.id.action_delete:
                int result = mDbHelper.deleteItem(item.getId());
                if (result == 1) {
                    itemList.remove(position);
                    mAdapter.notifyDataSetChanged();
                    onResume();
                    Toast.makeText(mContext, mContext.getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                }
                return;
            default:
                return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_other_apps) {
            Uri uri = Uri.parse("market://details?id=com.bitlink.countdown");
            Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(myAppLinkToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.bitlink.countdown")));
            }
            return true;
        } else if (id == R.id.action_recommend) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            //shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.recommend_text));
            startActivity(Intent.createChooser(shareIntent, getText(R.string.action_recommend)));
            return true;
        } /*else if (id == R.id.action_rate) {
            Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
            Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(myAppLinkToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + mContext.getPackageName())));
            }
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
