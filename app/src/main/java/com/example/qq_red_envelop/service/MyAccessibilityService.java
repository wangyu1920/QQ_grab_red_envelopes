package com.example.qq_red_envelop.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.qq_red_envelop.SP;

import java.util.List;

public class MyAccessibilityService extends AccessibilityService {
    String TAG = "MyAccessibilityService";
    String ID_HEAD = "com.tencent.mobileqq:id/";
    AccessibilityUtils utils;
    boolean firstFind=false;
    @Override
    public void onCreate() {
        super.onCreate();
        utils = new AccessibilityUtils(this);
        Log.d(TAG, "onCreate: ");
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent: ");
        if (firstFind&&getSharedPreferences(SP.AUTO_CLICK, MODE_PRIVATE).getBoolean(SP.AUTO_CLICK, false)) {
            Log.d(TAG, "onAccessibilityEvent: click");

            AccessibilityNodeInfo info = utils.findViewByID(ID_HEAD + "h3v");
            if (info != null) {
                firstFind = false;
                utils.performViewClick(info.getChild(0));
            }
        }
        //事件类型
        int eventType = event.getEventType();

        //获取包名
        CharSequence packageName = event.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            return;
        }

        switch (eventType) {

            //状态栏变化
            case GLOBAL_ACTION_NOTIFICATIONS:
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Log.d(TAG, "onAccessibilityEvent: Notification");
                if (QQConstant.QQ_PACKAGE_NAME.contentEquals(packageName)) {
                    //处理状态栏上QQ的消息，如果是红包就跳转过去
                    progressQQStatusBar(event);

                }
                break;

            //窗口切换的时候回调
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.d(TAG, "onAccessibilityEvent: Window");
                if (QQConstant.QQ_PACKAGE_NAME.contentEquals(packageName)) {
                    //处理正在QQ聊天窗口页面，有其他群或者人有新的红包提醒，跳转过去。
                    progressNewMessage(event);

                }

                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                break;
            case AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT:
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                break;
            case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                break;
        }
    }

    private void progressNewMessage(AccessibilityEvent event) {

        if (event == null) {
            return;
        }
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }
        //根据event的source里的text，来判断这个消息是否包含[QQ红包]的字眼，有的话就跳转过去
        CharSequence text = source.getText();
        Log.w(TAG, "progressNewMessage: "+text );
        if (!TextUtils.isEmpty(text) && text.toString().contains(QQConstant.QQ_ENVELOPE_KEYWORD)) {
            utils.performViewClick(source);
            vibrate();
            firstFind = true;
        }
    }

    public void progressQQStatusBar(AccessibilityEvent event) {
        List<CharSequence> text = event.getText();
        //开始检索界面上是否有QQ红包的文本，并且他是通知栏的信息
        if (text != null && text.size() > 0) {
            for (CharSequence charSequence : text) {
                if (charSequence.toString().contains(QQConstant.QQ_ENVELOPE_KEYWORD)) {
                    //说明存在红包弹窗，马上进去
                    if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) event.getParcelableData();
                        if (notification == null) {
                            return;
                        }
                        PendingIntent pendingIntent = notification.contentIntent;
                        if (pendingIntent == null) {
                            return;
                        }
                        try {
                            //跳转
                            pendingIntent.send();
                            vibrate();
                            firstFind = true;
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    private void vibrate() {
        Vibrator v=(Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
    }
}
