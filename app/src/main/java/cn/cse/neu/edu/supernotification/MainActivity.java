package cn.cse.neu.edu.supernotification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import cn.cse.neu.edu.supernotification.lib.BaseActivity;
import cn.cse.neu.edu.supernotification.lib.Level;
import cn.cse.neu.edu.supernotification.lib.SuperNotification;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.success).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuperNotification.getInstance().show(Level.SUCCESS, "Well done");
            }
        });

        findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuperNotification.getInstance().show(Level.INFO, "Hello, it is me");
            }
        });

        findViewById(R.id.warning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuperNotification.getInstance().show(Level.WARNING, "Go away, do not touch me");
            }
        });

        findViewById(R.id.error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuperNotification.getInstance().show(Level.ERROR, "Oh, shit");
            }
        });

        findViewById(R.id.defaultStyle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuperNotification.getInstance().setNotificationGetter(null);
            }
        });

        findViewById(R.id.custom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuperNotification.getInstance().setNotificationGetter(new CustomNotificationGetter(getApplicationContext()));
            }
        });

        findViewById(R.id.secondActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, SecondActivity.class), REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            // NOTE: 不要使用show(Level, String)方法，因为此时栈顶的activity还是SecondActivity,因此notification还是会在
            // SecondActivity中弹出并且立即销毁，因此需要使用show(Activity, Level, String)方法指定notification在哪个activity
            // 中弹出。
            SuperNotification.getInstance().show(MainActivity.this, Level.SUCCESS, data.getStringExtra("msg"));
        }
    }
}