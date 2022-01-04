package com.example.qq_red_envelop.service;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AccessibilityUtils {

    AccessibilityService service;

    public interface Filtration{
        public boolean filtrate(AccessibilityNodeInfo nodeInfo);
    }

    AccessibilityUtils(AccessibilityService service1) {
        service = service1;
    }




    /**
     * 判断无障碍服务是否Enable
     * @param mContext 上下文
     * @param serviceClass 服务类
     * @return  boolean
     */
    public static boolean isAccessibilitySettingsOn(Context mContext,Class serviceClass) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + serviceClass.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 查找对应ID的View
     *
     * @param id id
     * @return View
     */
    public AccessibilityNodeInfo findViewByID(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = service.getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    /**
     * 通过筛选器筛选符合条件的控件集合
     * @param filtration 筛选器
     * @return List<AccessibilityNodeInfo>
     */
    public List<AccessibilityNodeInfo> findViewListByFiltration(Filtration filtration) {
        AccessibilityNodeInfo accessibilityNodeInfo = service.getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }

        List<AccessibilityNodeInfo> list = new LinkedList<>();
        search(accessibilityNodeInfo,filtration,list);
        return list;
    }

    /**
     * 查找对应Text的控件，将返回一个集合
     * @param text text
     * @return  List<AccessibilityNodeInfo>
     */
    public List<AccessibilityNodeInfo> findViewListByText(String text) {
        AccessibilityNodeInfo accessibilityNodeInfo = service.getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        List<AccessibilityNodeInfo> list = new LinkedList<>();
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    list.add(nodeInfo);
                }
            }
        }
        return list;
    }


    /**
     * 粘贴文本到控件中
     * @param nodeInfo 目标控件
     * @param context 上下文
     * @param key 要粘贴的文字
     */
    public void pastaText(AccessibilityNodeInfo nodeInfo,Context context, String key) {
        //使用剪切板
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        //获取焦点
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        //需要替换的key
        clipboard.setPrimaryClip(ClipData.newPlainText("text", key));
        //粘贴进入内容
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }

    /**
     * 模拟输入
     *
     * @param nodeInfo nodeInfo
     * @param text     text
     */
    public void inputText(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null) {
            return;
        }
        Bundle arguments = new Bundle();
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    /**
     *模拟点击事件
     *如果控件不可点击则遍历父控件直到找到一个能点击的，进行点击
     * @param nodeInfo nodeInfo
     */
    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 模拟返回操作
     * @param time 延时时间
     */
    public void performBackClick(int time) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            }
        }).start();
    }

    /**
     * 模拟下滑操作
     * @param time 延时时间
     */
    public void performScrollBackward(int time) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                service.performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }
        }).start();
    }

    /**
     * 模拟上滑操作
     * @param time 延时时间
     */
    public void performScrollForward(int time) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                service.performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
        }).start();
    }

    private void search(AccessibilityNodeInfo accessibilityNodeInfo, Filtration filtration, List<AccessibilityNodeInfo> list) {
        if (accessibilityNodeInfo == null) {
            return;
        }
        if (filtration.filtrate(accessibilityNodeInfo)) {
            list.add(accessibilityNodeInfo);
        }
        for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo info = accessibilityNodeInfo.getChild(i);
            search(info, filtration, list);
        }
    }
    
}
