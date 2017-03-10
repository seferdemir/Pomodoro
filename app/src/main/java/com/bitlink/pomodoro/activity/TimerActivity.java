package com.bitlink.pomodoro.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.CircleView;
import com.bitlink.pomodoro.R;
import com.bitlink.pomodoro.database.DatabaseHelper;
import com.bitlink.pomodoro.database.model.Item;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TimerActivity extends AppCompatActivity {

    public static final String ARG_ITEM = "id";
    private static final int REQUEST_EDIT = 1;
    private DatabaseHelper databaseHelper = null;
    private Context mContext;
    Toolbar mToolBar;
    private FrameLayout mLayout = null;
    private Button startButton, stopButton = null;
    EditText edit_task_name;
    TextView minute_tv, second_tv;
    private Item mItem;
    private Bundle extras;
    int mColor = 0;
    int[] mColorArray = {0};
    Calendar newCalendar, mReminderCalendar;
    CountDownTimer mTimer;
    short duration;
    long remainingMin, remainingSec;
    float progress;
    boolean isRunning = false;

    private SharedPreferences mSPreferences;
    private SharedPreferences.Editor mEditor;

    private HoloCircularProgressBar mCircularProgressBar;
    private ObjectAnimator mProgressBarAnimator;

    Intent notificationIntent;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mContext = this;

        mColorArray = mContext.getResources().getIntArray(R.array.rainbow);

        mSPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor = mSPreferences.edit();
        mEditor.putInt("step", 1);
        mEditor.putInt("break", 0);
        mEditor.commit();

        databaseHelper = new DatabaseHelper(this);

        mLayout = (FrameLayout) findViewById(R.id.layout);
        edit_task_name = (EditText) findViewById(R.id.edt_task_name);
        minute_tv = (TextView) findViewById(R.id.minute);
        second_tv = (TextView) findViewById(R.id.second);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        mCircularProgressBar = (HoloCircularProgressBar) findViewById(R.id.progress);
        mCircularProgressBar.setProgress(0f);
        mCircularProgressBar.setWheelSize((int) getResources().getDimension(R.dimen.progressBarWidth));

        notificationIntent = new Intent(mContext, TimerActivity.class);
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        extras = getIntent().getExtras();
        if (extras != null) {
            mItem = databaseHelper.getItemById(extras.getInt(ARG_ITEM));

            if (mItem != null) {

                duration = mItem.getWorkSessionDuration();

                mColor = mItem.getWorkSessionColor();

                setColorToolbar(mItem.getWorkSessionColor());

                edit_task_name.setText(mItem.getTaskName());
                minute_tv.setText(String.valueOf(duration));
                second_tv.setText("00");
            }
        }

        if (mItem == null) {
            mItem = new Item();
            mItem.setWorkSessionDuration((short) 25);
            mItem.setBreakDuration((short) 5);
            mItem.setLongBreakDuration((short) 15);
            mItem.setTotalWorkSession((short) 4);
        }

        final String editTaskNameStr = edit_task_name.getText().toString();
        edit_task_name.setSelection(editTaskNameStr.length());
        edit_task_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_task_name.setFocusable(true);
                edit_task_name.setFocusableInTouchMode(true);
            }
        });
        edit_task_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 3 && !editTaskNameStr.equals(editable.toString()) && !editable.toString().equals(getString(R.string.break_time))) {
                    mItem.setTaskName(editable.toString());

                    databaseHelper.updateItem(mItem);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        notificationManager.cancelAll();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putLong("Minute", remainingMin);
        savedInstanceState.putLong("Second", remainingSec);
        savedInstanceState.putFloat("Progress", mCircularProgressBar.getProgress());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        remainingMin = savedInstanceState.getLong("Minute");
        remainingSec = savedInstanceState.getLong("Second");
        progress = savedInstanceState.getFloat("Progress");

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timer, menu);

        return true;
    }

    @Override
    public void onBackPressed() {

        if (extras == null) {

            new MaterialDialog.Builder(this)
                    .title(R.string.save_timer)
                    .content(R.string.want_to_save_changes)
                    .positiveText(android.R.string.yes)
                    .negativeText(android.R.string.no)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            mItem.setTaskName(edit_task_name.getText().toString());

                            databaseHelper.insertItem(mItem);
                        }
                    })
                    .show();
        }

        if (!isRunning) {
            super.onBackPressed();
        } else {
            moveTaskToBack(true); // it acts like home button
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_edit:
                Intent mIntent = new Intent(this, ItemActivity.class);
                mIntent.putExtra(ItemActivity.ARG_ITEM, mItem.getId());
//                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(mIntent, REQUEST_EDIT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT) {
            if (resultCode == RESULT_OK) {
                mItem = databaseHelper.getItemById(extras.getInt(ARG_ITEM));

                duration = mItem.getWorkSessionDuration();

                mColor = mItem.getWorkSessionColor();

                setColorToolbar(mItem.getWorkSessionColor());

                edit_task_name.setText(mItem.getTaskName());
                minute_tv.setText(String.valueOf(duration));
                second_tv.setText("00");
            }
        }
    }

    private void setColorToolbar(int color) {

        if (color == 0)
            color = mColorArray[new Random().nextInt(mColorArray.length)];

        int shiftColor = CircleView.shiftColorDown(color);
        int darkShiftColor = CircleView.shiftColorDown(shiftColor);

        mLayout.setBackgroundColor(color);
        mToolBar.setBackgroundColor(shiftColor);

        mCircularProgressBar.setProgressColor(shiftColor);
        mCircularProgressBar.setProgressBackgroundColor(ContextCompat.getColor(this, R.color.circle_progress_color));

        startButton.setBackgroundDrawable(new IconicsDrawable(mContext, FontAwesome.Icon.faw_play_circle).color(Color.parseColor("#CCFFFFFF")).sizeDp(48));
        stopButton.setBackgroundDrawable(new IconicsDrawable(mContext, FontAwesome.Icon.faw_stop_circle).color(Color.parseColor("#CCFFFFFF")).sizeDp(48));

//        minute_tv.setTextColor(darkShiftColor);
//        second_tv.setTextColor(darkShiftColor);

        if (getSupportActionBar() != null)
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(shiftColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(darkShiftColor);
            getWindow().setNavigationBarColor(shiftColor);
        }

        setSupportActionBar(mToolBar);
    }

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    public void StartTimer(View view) {

        newCalendar = Calendar.getInstance();

        mReminderCalendar = (Calendar) newCalendar.clone();
        mReminderCalendar.add(Calendar.MINUTE, duration);
//        mReminderCalendar.add(Calendar.SECOND, duration * 10);

        long start_millis = newCalendar.getTimeInMillis(); //get the start time in milliseconds
        long end_millis = mReminderCalendar.getTimeInMillis(); //get the end time in milliseconds
        final long total_millis = (end_millis - start_millis); //total time in milliseconds

        if (total_millis <= 0) {
//            holder.remainigTime.setText(String.format(mContext.getResources().getString(R.string.timesUp), itemReminder));
        }

//        //1000 = 1 second interval
        mTimer = new CountDownTimer(total_millis, 1000) {

            String sec, min;
            int i = 0;

            @Override
            public void onTick(long millisUntilFinished) {

                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes);

                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                min = String.valueOf(minutes);
                sec = String.valueOf(seconds);

                if (seconds < 10)
                    sec = "0" + sec;

                minute_tv.setText(min);
                second_tv.setText(sec);

                remainingMin = minutes;
                remainingSec = seconds;
            }

            @Override
            public void onFinish() {
                int step = mSPreferences.getInt("step", 1);
                int _break = mSPreferences.getInt("break", 0);

                if (_break == step) { // Have break was made?
                    mEditor.putInt("step", step + 1);
                }
                mEditor.putInt("break", step);
                mEditor.commit();

                step = mSPreferences.getInt("step", 1);
                _break = mSPreferences.getInt("break", 0);

                if (step == mItem.getTotalWorkSession() + 1 && step == _break) {
                    duration = mItem.getLongBreakDuration();

                    edit_task_name.setText(getString(R.string.break_time));
                    setColorToolbar(mItem.getLongBreakColor());

                    mEditor.putInt("step", 0);
                    mEditor.putInt("break", 0);
                    mEditor.commit();
                } else {
                    if (step <= mItem.getTotalWorkSession()) {
                        duration = mItem.getWorkSessionDuration();

                        edit_task_name.setText(mItem.getTaskName());
                        setColorToolbar(mItem.getWorkSessionColor());

                        notifyAlarm(String.format(mContext.getResources().getString(R.string.work_time), mItem.getTaskName()));
                    }

                    step = mSPreferences.getInt("step", 1);
                    _break = mSPreferences.getInt("break", 0);
                    if (step == _break) {
                        duration = mItem.getBreakDuration();

                        edit_task_name.setText(getString(R.string.break_time));
                        setColorToolbar(mItem.getBreakColor());

                        notifyAlarm(String.format(mContext.getResources().getString(R.string.break_time_for_notify), mItem.getTaskName()));
                    }
                    if (((mItem.getTotalWorkSession() == 1 && step == 1) || (step == mItem.getTotalWorkSession())) && step == _break) {
                        duration = mItem.getLongBreakDuration();

                        edit_task_name.setText(getString(R.string.break_time));
                        setColorToolbar(mItem.getLongBreakColor());

                        mEditor.putInt("step", 0);
                        mEditor.putInt("break", 0);
                        mEditor.commit();
                    }
                }

                minute_tv.setText(String.valueOf(duration));
                second_tv.setText("00");

                isRunning = false;

                notificationManager.notify(0, builder.build());
            }
        };
        mTimer.start();

        mCircularProgressBar.setProgress(0f);
        mProgressBarAnimator = ObjectAnimator.ofFloat(mCircularProgressBar, "progress", 1f);
        mProgressBarAnimator.setDuration(1000 * 60 * duration);
//        mProgressBarAnimator.setDuration(1000 * 10 * duration);

        mProgressBarAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(final Animator animation) {
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                mProgressBarAnimator.cancel();

                startButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

            @Override
            public void onAnimationStart(final Animator animation) {
            }
        });

        /*mProgressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                mCircularProgressBar.setProgress((Float) animation.getAnimatedValue());
            }
        });*/

//        mCircularProgressBar.setMarkerProgress(1f);
//        mProgressBarAnimator.start();

        startButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.VISIBLE);

        mCircularProgressBar.setMarkerProgress(1f);
        mProgressBarAnimator.start();

        isRunning = true;
    }

    public void StopTimer(View view) {

        if (mTimer != null)
            mTimer.cancel();

        duration = mItem.getWorkSessionDuration();

        edit_task_name.setText(mItem.getTaskName());
        setColorToolbar(mItem.getWorkSessionColor());

        mEditor.putInt("step", 1);
        mEditor.putInt("break", 0);
        mEditor.commit();

        if (mProgressBarAnimator != null)
            mProgressBarAnimator.cancel();
        if (mCircularProgressBar != null)
            mCircularProgressBar.setProgress(0f);

        startButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.GONE);

        minute_tv.setText(String.valueOf(mItem.getWorkSessionDuration()));
        second_tv.setText("00");

        isRunning = false;
    }

    private void notifyAlarm(String title) {

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(ARG_ITEM, mItem.getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(mContext);

        Notification notification = builder
                .setAutoCancel(true)
                .setContentTitle(mContext.getResources().getString(R.string.app_name))
                .setContentText(title)
                .setTicker(title)
                .setLights(Color.RED, 3000, 3000)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{1000, 1000, 1000})
                .setSmallIcon(R.drawable.ic_pomodoro)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_pomodoro))
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .build();
    }
}
