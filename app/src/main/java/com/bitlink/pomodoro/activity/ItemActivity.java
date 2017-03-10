package com.bitlink.pomodoro.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.bitlink.pomodoro.R;
import com.bitlink.pomodoro.database.DatabaseHelper;
import com.bitlink.pomodoro.database.model.Item;
import com.mikepenz.iconics.IconicsDrawable;
import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ItemActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    public static final String ARG_ITEM = "id";
    private DatabaseHelper databaseHelper = null;
    private Context mContext;
    Toolbar mToolBar;
    EditText editTitle;
    TextView textViewWorkDuration, textViewBreakDuration, textViewLongBreakDuration, textViewWorkSession;
    SeekBar seekBarWorkDuration, seekBarBreakDuration, seekBarLongBreakDuration, seekBarWorkSession;
    ImageButton imageButtonWorkColor, imageButtonBreakColor, imageButtonLongBreakColor;
    private Item mItem;
    private Bundle extras;
    ColorChooserDialog workSessionColorDialog, breakColorDialog, longBreakColorDialog;
    int[] mColorArray = {0};

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mContext = this;

        databaseHelper = new DatabaseHelper(this);

        editTitle = (EditText) findViewById(R.id.edt_title);

        textViewWorkDuration = (TextView) findViewById(R.id.tv_work_duration);
        textViewBreakDuration = (TextView) findViewById(R.id.tv_break_duration);
        textViewLongBreakDuration = (TextView) findViewById(R.id.tv_long_break_duration);
        textViewWorkSession = (TextView) findViewById(R.id.tv_total_work_session);

        seekBarWorkDuration = (SeekBar) findViewById(R.id.seek_bar_work_duration);
        seekBarBreakDuration = (SeekBar) findViewById(R.id.seek_bar_break_duration);
        seekBarLongBreakDuration = (SeekBar) findViewById(R.id.seek_bar_long_break_duration);
        seekBarWorkSession = (SeekBar) findViewById(R.id.seek_bar_total_work_session);

        imageButtonWorkColor = (ImageButton) findViewById(R.id.work_color);
        imageButtonBreakColor = (ImageButton) findViewById(R.id.break_color);
        imageButtonLongBreakColor = (ImageButton) findViewById(R.id.long_break_color);

        mColorArray = mContext.getResources().getIntArray(R.array.rainbow);

        extras = getIntent().getExtras();
        if (extras != null) {
            mItem = databaseHelper.getItemById(extras.getInt(ARG_ITEM));

            if (mItem != null) {

                editTitle.setText(mItem.getTaskName());

                seekBarWorkDuration.setProgress(mItem.getWorkSessionDuration());
                seekBarBreakDuration.setProgress(mItem.getBreakDuration());
                seekBarLongBreakDuration.setProgress(mItem.getLongBreakDuration());
                seekBarWorkSession.setProgress(mItem.getTotalWorkSession());

                String minuteText = getString(R.string.minutes);
                if (mItem.getWorkSessionDuration() == 1)
                    minuteText = getString(R.string.minute);
                textViewWorkDuration.setText(String.format("%d " + minuteText, mItem.getWorkSessionDuration()));

                minuteText = getString(R.string.minutes);
                if (mItem.getBreakDuration() == 1)
                    minuteText = getString(R.string.minute);
                textViewBreakDuration.setText(String.format("%d " + minuteText, mItem.getBreakDuration()));

                minuteText = getString(R.string.minutes);
                if (mItem.getLongBreakDuration() == 1)
                    minuteText = getString(R.string.minute);
                textViewLongBreakDuration.setText(String.format("%d " + minuteText, mItem.getLongBreakDuration()));

                String sessionText = getString(R.string.sessions);
                if (seekBarWorkSession.getProgress() == 1)
                    sessionText = getString(R.string.session);
                textViewWorkSession.setText(String.format("%d " + sessionText, mItem.getTotalWorkSession()));
            }
        } else {

            if (mItem == null)
                mItem = new Item();

            seekBarWorkDuration.setProgress(25);
            seekBarBreakDuration.setProgress(5);
            seekBarLongBreakDuration.setProgress(15);
            seekBarWorkSession.setProgress(4);

            String minuteText = getString(R.string.minutes);
            if (seekBarWorkDuration.getProgress() == 1)
                minuteText = getString(R.string.minute);
            textViewWorkDuration.setText(String.format("%d " + minuteText, seekBarWorkDuration.getProgress()));

            minuteText = getString(R.string.minutes);
            if (seekBarBreakDuration.getProgress() == 1)
                minuteText = getString(R.string.minute);
            textViewBreakDuration.setText(String.format("%d " + minuteText, seekBarBreakDuration.getProgress()));

            minuteText = getString(R.string.minutes);
            if (seekBarLongBreakDuration.getProgress() == 1)
                minuteText = getString(R.string.minute);
            textViewLongBreakDuration.setText(String.format("%d " + minuteText, seekBarLongBreakDuration.getProgress()));

            String sessionText = getString(R.string.sessions);
            if (seekBarWorkSession.getProgress() == 1)
                sessionText = getString(R.string.session);
            textViewWorkSession.setText(String.format("%d " + sessionText, seekBarWorkSession.getProgress()));

            mToolBar.setTitle(mContext.getString(R.string.new_record));

//            edtTime.setEnabled(false);
        }

        seekBarWorkDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                    seekBarWorkDuration.setProgress(1);
                }

                String minuteText = getString(R.string.minutes);
                if (progress == 1)
                    minuteText = getString(R.string.minute);
                textViewWorkDuration.setText(String.format("%d " + minuteText, progress));

                mItem.setWorkSessionDuration((short) progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarBreakDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                    seekBarBreakDuration.setProgress(1);
                }

                String minuteText = getString(R.string.minutes);
                if (progress == 1)
                    minuteText = getString(R.string.minute);
                textViewBreakDuration.setText(String.format("%d " + minuteText, progress));

                mItem.setBreakDuration((short) progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarLongBreakDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                    seekBarLongBreakDuration.setProgress(1);
                }

                String minuteText = getString(R.string.minutes);
                if (progress == 1)
                    minuteText = getString(R.string.minute);
                textViewLongBreakDuration.setText(String.format("%d " + minuteText, progress));

                mItem.setLongBreakDuration((short) progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarWorkSession.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                    seekBarWorkSession.setProgress(1);
                }

                String sessionText = getString(R.string.session);
                if (progress > 1)
                    sessionText = getString(R.string.sessions);
                textViewWorkSession.setText(String.format("%d " + sessionText, progress));

                mItem.setTotalWorkSession((short) progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (mItem.getWorkSessionColor() == 0)
            mItem.setWorkSessionColor(mColorArray[new Random().nextInt(mColorArray.length)]);
        if (mItem.getBreakColor() == 0)
            mItem.setBreakColor(mColorArray[new Random().nextInt(mColorArray.length)]);
        if (mItem.getLongBreakColor() == 0)
            mItem.setLongBreakColor(mColorArray[new Random().nextInt(mColorArray.length)]);

        imageButtonWorkColor.setBackgroundColor(mItem.getWorkSessionColor());
        imageButtonBreakColor.setBackgroundColor(mItem.getBreakColor());
        imageButtonLongBreakColor.setBackgroundColor(mItem.getLongBreakColor());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setNavigationBarColor(mContext.getResources().getColor(R.color.colorPrimary));
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_save);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTitle.getText().toString().length() == 0) {

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.enter_blanks), Toast.LENGTH_LONG).show();
                } else {

                    mItem.setTaskName(editTitle.getText().toString());
                    mItem.setWorkSessionDuration((short) seekBarWorkDuration.getProgress());
                    mItem.setBreakDuration((short) seekBarBreakDuration.getProgress());
                    mItem.setLongBreakDuration((short) seekBarLongBreakDuration.getProgress());
                    mItem.setTotalWorkSession((short) seekBarWorkSession.getProgress());

                    if (extras != null) {
                        databaseHelper.updateItem(mItem);
                    } else {
                        databaseHelper.insertItem(mItem);
                    }

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.saved), Toast.LENGTH_LONG).show();
//                    onBackPressed();
                    Intent mIntent = getIntent();
                    mIntent.putExtra(ItemActivity.ARG_ITEM, mItem.getId());
                    setResult(RESULT_OK, mIntent);
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            /*case R.id.action_add:
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Receives callback from color chooser dialog
    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int color) {

        if (dialog == workSessionColorDialog) {
            mItem.setWorkSessionColor(color);
            imageButtonWorkColor.setBackgroundColor(color);
        } else if (dialog == breakColorDialog) {
            mItem.setBreakColor(color);
            imageButtonBreakColor.setBackgroundColor(color);
        } else if (dialog == longBreakColorDialog) {
            mItem.setLongBreakColor(color);
            imageButtonLongBreakColor.setBackgroundColor(color);
        }
    }

    public void SetColorWorkSession(View view) {

        workSessionColorDialog = new ColorChooserDialog.Builder(this, R.string.select_color)
                .titleSub(R.string.select_color)
                .backButton(R.string.back)
                .cancelButton(R.string.cancel)
                .customButton(R.string.custom)
                .doneButton(R.string.done)
                .preselect(mItem.getWorkSessionColor())
                .presetsButton(R.string.presets)
                .show();
    }

    public void SetColorBreak(View view) {

        breakColorDialog = new ColorChooserDialog.Builder(this, R.string.select_color)
                .titleSub(R.string.select_color)
                .backButton(R.string.back)
                .cancelButton(R.string.cancel)
                .customButton(R.string.custom)
                .doneButton(R.string.done)
                .preselect(mItem.getBreakColor())
                .presetsButton(R.string.presets)
                .show();
    }

    public void SetColorLongBreak(View view) {

        longBreakColorDialog = new ColorChooserDialog.Builder(this, R.string.select_color)
                .titleSub(R.string.select_color)
                .backButton(R.string.back)
                .cancelButton(R.string.cancel)
                .customButton(R.string.custom)
                .doneButton(R.string.done)
                .preselect(mItem.getLongBreakColor())
                .presetsButton(R.string.presets)
                .show();
    }
}
