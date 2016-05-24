package com.example.johanboqvist.myproject;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EndGameActivity extends AppCompatActivity {

    private TextView title;
    private TextView textPoints;
    private EditText name;
    private Button btn;
    private LinearLayout linearLayout;
    private HighScoreManager highScoreManager = new HighScoreManager();
    private int position;
    private int points;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        Intent mIntent = getIntent();
        points = mIntent.getIntExtra("points", 0);

        textPoints = (TextView) findViewById(R.id.textPoints);
        textPoints.setText("Points: "+points);


        new Thread(){
            public void run() {
                highScoreManager.getHighScore("http://52.49.26.113/highscore.txt");
                position = highScoreManager.isHighScore(points);
                if(position != -1) {
                    messageHandler.sendEmptyMessage(0);
                } else {
                    messageHandler.sendEmptyMessage(-1);
                }


            }
        }.start();

    }

    private final Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what) {
                case -1: noHighScore(); break;
                case 0: createForm(); break;
                case 1: {
                    finish();
                } break;
            }

        }
    };

    public void noHighScore(){
        title = (TextView) findViewById(R.id.textHi);
        title.setText("No highscore, try again!");
    }

    public void createForm(){
        linearLayout = (LinearLayout) findViewById(R.id.endgame);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        title = (TextView) findViewById(R.id.textHi);
        title.setText("YOU MADE IT TO THE TOP 5!");

        //Name
        name = new EditText(this);
        name.setHint("Enter your name:");
        params2.gravity = Gravity.CENTER_HORIZONTAL;
        name.setLayoutParams(params2);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(name.getText())){
                    btn.setEnabled(false);
                } else {
                    btn.setEnabled(true);
                }
            }
        });

        linearLayout.addView(name);

        btn = new Button(this);
        btn.setText("Submit highscore!");
        btn.setLayoutParams(params2);
        btn.setGravity(Gravity.CENTER_HORIZONTAL);
        btn.setEnabled(false);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    public void run() {

                        highScoreManager.addEntry(position, name.getText().toString(), points);
                        highScoreManager.writeHighScore("http://52.49.26.113/highscore.php");
                        messageHandler.sendEmptyMessage(1);
                    }
                }.start();

            }
        });

        linearLayout.addView(btn);



    }
}


