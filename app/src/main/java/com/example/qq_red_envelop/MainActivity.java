package com.example.qq_red_envelop;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qq_red_envelop.service.AccessibilityUtils;
import com.example.qq_red_envelop.service.MyAccessibilityService;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    CheckBox checkBox;
    EditText editText;
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        editText=new EditText(getApplicationContext());
        textView = findViewById(R.id.textview);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        checkBox.setChecked(getSharedPreferences(SP.AUTO_CLICK, MODE_PRIVATE).getBoolean(SP.AUTO_CLICK, false));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mContext.getSharedPreferences(SP.AUTO_CLICK, MODE_PRIVATE)
                            .edit()
                            .putBoolean(SP.AUTO_CLICK, isChecked)
                            .apply();
//                if (!isChecked) {
//                    mContext.getSharedPreferences(SP.AUTO_CLICK, MODE_PRIVATE)
//                            .edit()
//                            .putBoolean(SP.AUTO_CLICK, false)
//                            .apply();
//                    return;
//                }
//                new AlertDialog.Builder(mContext)
//                        .setTitle("点击延迟")
//                        .setView(editText)
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                mContext.getSharedPreferences(SP.DELAY_TIME, MODE_PRIVATE)
//                                        .edit()
//                                        .putInt(SP.DELAY_TIME, Integer.parseInt(editText.getText().toString()))
//                                        .apply();
//                                mContext.getSharedPreferences(SP.AUTO_CLICK, MODE_PRIVATE)
//                                        .edit()
//                                        .putBoolean(SP.AUTO_CLICK, isChecked)
//                                        .apply();
//                            }
//                        }).create().show();
            }
        });
        if (AccessibilityUtils.isAccessibilitySettingsOn(this, MyAccessibilityService.class)) {
            Intent intent = new Intent(this, MyAccessibilityService.class);
            stopService(intent);
            startService(intent);
            textView.setText("已经开启红包监听");
            Toast.makeText(this, "开始监听", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "需要无障碍权限", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                }
            }).start();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (AccessibilityUtils.isAccessibilitySettingsOn(this, MyAccessibilityService.class)) {
            startService(new Intent(this, MyAccessibilityService.class));
            textView.setText("已经开启红包监听");
            Toast.makeText(this, "开始监听", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "没有无障碍权限，无法启动", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "停止监听", Toast.LENGTH_SHORT).show();
        stopService(new Intent(this, MyAccessibilityService.class));
        super.onDestroy();
    }
}