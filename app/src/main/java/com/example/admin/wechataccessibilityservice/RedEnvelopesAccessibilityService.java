package com.example.admin.wechataccessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2016/12/20   .
 */

public class RedEnvelopesAccessibilityService extends AccessibilityService {

    private List<AccessibilityNodeInfo> parents;
    private int mHaveReceivedRedNumber = 0;
    private String mNotification_Target_Contains = "[微信红包]";
    private String mRedEnvelopes = "com.tencent.mm.ui.LauncherUI";
    private String mRedEnvelopes_ReceiveUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    private String mRedEnvelopes_DetailUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    private String OpenID = "com.tencent.mm:id/bdh";
    private String CloseID = "com.tencent.mm:id/gp";
    private static final String TAG = "michael";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG,"RedEnvelopesAccessibilityService.is connected");
        parents = new ArrayList<>();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        int eventType = accessibilityEvent.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Log.d(TAG,"onAccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED CHANGED ");
                List<CharSequence> mNotification_text = accessibilityEvent.getText();
                if (!mNotification_text.isEmpty()) {
                    for (CharSequence text : mNotification_text) {
                        String content = text.toString();
                        if (content.contains(mNotification_Target_Contains)) {
                            Log.d(TAG,"Open WeChat intent..... ");
                            if (accessibilityEvent.getParcelableData() != null &&
                                    accessibilityEvent.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) accessibilityEvent.getParcelableData();//通过statu_bar或者是notification传入的一个当前notification对象
                                PendingIntent pendingIntent = notification.contentIntent;//得到pendingIntent对象
                                try {
                                    pendingIntent.send();//执行这个intent
                                    Log.d(TAG,"Into WeChat.....");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.d(TAG,"onAccessibilityEvent.TYPE_WINDOW_STATE_CHANGED CHANGED ");
                String className = accessibilityEvent.getClassName().toString();
                if (className.equals(mRedEnvelopes)) {
                    //点击最后一个红包
                    Log.d(TAG,"Get Last RedEnvelopes.....");
                    getLastPacket();
                } else if (className.equals(mRedEnvelopes_ReceiveUI)) {
                    //开红包
                    Log.d(TAG,"Open RedEnvelopes.....");
                    inputClick(OpenID);
                } else if (className.equals(mRedEnvelopes_DetailUI)) {
                    //退出红包
                    Log.d(TAG,"Exit RedEnvelopes.....");
//                    inputClick(CloseID);  //如果用户手动进入红包详情界面它会自动返回，所以暂时注销
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG,"RedEnvelopesAccessibilityService was Interrupt ");
    }

    private void inputClick(String clickId) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(clickId);
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    private void getLastPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();//获取当前窗口的根节点
        recycle(rootNode);
        Log.d(TAG,"parents.size() == "+ parents.size()+" "+" mHaveReceivedRedNumber == "+mHaveReceivedRedNumber);
        if(parents.size()>0 && mHaveReceivedRedNumber < parents.size()){
            parents.get(parents.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        parents.clear();//清楚parents 个数避免一直累加
        mHaveReceivedRedNumber = 0;
    }

    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            Log.d(TAG," if ChildCount == "+info.getChildCount());
            if (info.getText() != null) {
                if ("领取红包".equals(info.getText().toString())) {
                    Log.d(TAG," 检测到红包，判断是否可点击 == "+info.isClickable());
                    if (info.isClickable()) {//判断是否可点击
                        Log.d(TAG," performAction.ACTION_CLICK "+info.getChildCount());
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {//如果其父view 可点击则保存起来
                            parents.add(parent);
                            Log.d(TAG," recycle.parents size == "+parents.size());
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
                if(info.getText().toString().contains("你领取了")){
                    mHaveReceivedRedNumber++;
                }
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                Log.d(TAG," else ChildCount == "+info.getChildCount());
                if (info.getChild(i) != null) {
                    Log.d(TAG," for if  the child sum  == "+ i);
                    recycle(info.getChild(i));
                }
            }
        }
    }
}
