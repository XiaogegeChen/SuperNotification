package cn.cse.neu.edu.supernotification.lib;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 从顶部拉下来的notification.
 */
@SuppressWarnings("unused")
public class SuperNotification {
    // 单例
    private static volatile SuperNotification instance;

    static Context applicationContext;

    /**
     * 获得SuperNotification的单例。
     * @return SuperNotification的单例。
     */
    public static SuperNotification getInstance() {
        if (instance == null) {
            synchronized (SuperNotification.class){
                if (instance == null) {
                    instance = new SuperNotification();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化，传入applicationContext.
     * @param applicationContext applicationContext
     */
    public static void init(Context applicationContext){
        SuperNotification.applicationContext = applicationContext;
    }

    /** 记录SuperNotification注册所有的activity，列表的最后一个是栈顶的activity **/
    private ArrayList<Activity> registeredActivityList = new ArrayList<>();
    /** 记录SuperNotification注册所有的activity和与之对应的NotificationFrameLayout **/
    private HashMap<Activity, NotificationFrameLayout> registeredActivityMap = new HashMap<>();
    /** NotificationGetter，用来获取各个级别的notification **/
    private NotificationGetter notificationGetter;

    private SuperNotification(){
        notificationGetter = new DefaultNotificationGetter();
    }

    /**
     * 设置NotificationGetter， 如果传入的是null，则使用默认的NotificationGetter
     * @param notificationGetter notificationGetter
     */
    public void setNotificationGetter(@Nullable NotificationGetter notificationGetter) {
        if (notificationGetter != null) {
            this.notificationGetter = notificationGetter;
        }else{
            this.notificationGetter = new DefaultNotificationGetter();
        }
    }

    /**
     * 从顶部弹出一个通知。这个方法默认使用当前栈顶的activity
     * @param notificationLevel 通知的级别
     * @param message 通知的内容
     */
    public void show(Level notificationLevel, String message){
        // 找到栈顶activity的NotificationFrameLayout并操作
        Activity topActivity = registeredActivityList.get(registeredActivityList.size() - 1);
        show(topActivity, notificationLevel, message);
    }

    /**
     * 从顶部弹出一个通知。这个方法使用指定的activity，如果该activity未注册则抛出异常。
     * @param activity 指定的activity，notification将会从这个activity弹出。
     * @param notificationLevel 通知级别
     * @param message 通知的内容
     */
    public void show(@NonNull Activity activity, Level notificationLevel, String message){
        NotificationFrameLayout notificationFrameLayout = registeredActivityMap.get(activity);
        if (notificationFrameLayout == null) {
            throw new IllegalArgumentException("You should call SuperNotification.register(Activity) before calling show(Activity, Level, String)");
        }
        // 找到对应级别的View
        View notificationView = notificationGetter.getNotificationViewInternal(notificationLevel);
        notificationView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // 将message展示在view上
        notificationGetter.setMessageInternal(notificationView, notificationLevel, message);
        // java.lang.IllegalStateException: The specified child already has a parent. You must call
        // removeView() on the child's parent first. 因此addView()之前先将其从parent中移除，再将view添加到
        // NotificationFrameLayout中。
        notificationFrameLayout.removeAllViews();
        ViewParent parent = notificationView.getParent();
        if(parent != null){
            ((ViewGroup)(notificationView.getParent())).removeView(notificationView);
        }
        notificationFrameLayout.addView(notificationView);
        // 显示出来
        notificationFrameLayout.dismissImmediately();
        notificationFrameLayout.show();
    }

    /**
     * 隐藏当前正在显示的通知，如果当前没有通知，则不进行任何操作。这个方法只会隐藏当前栈顶的activity中的通知。
     */
    public void dismiss(){
        // 找到栈顶activity的NotificationFrameLayout并操作
        Activity topActivity = registeredActivityList.get(registeredActivityList.size() - 1);
        dismiss(topActivity);
    }

    /**
     * 隐藏当前正在显示的通知，如果当前没有通知，则不进行任何操作。这个方法隐藏指定activity中的通知。
     * @param activity 指定的activity
     */
    public void dismiss(Activity activity){
        NotificationFrameLayout notificationFrameLayout = registeredActivityMap.get(activity);
        if (notificationFrameLayout == null) {
            return;
        }

        if(notificationFrameLayout.getState() != NotificationFrameLayout.STATE_DISMISS){
            notificationFrameLayout.dismiss(null);
        }
    }

    /**
     * SuperNotification需要向activity中添加额外的view，因此每个activity都需要注册到SuperNotification中。这个
     * 方法通常在activity的onCreate()方法中被调用，同时要确保在onDestroy()中调用unregister()方法，避免持有activity
     * 引起内存泄漏。
     * @param activity 待添加的activity
     *
     * @see #unregister(Activity)
     */
    public void register(@NonNull Activity activity){
        registeredActivityList.add(activity);
        // DecorView是一个FrameLayout
        FrameLayout decorFrameLayout = (FrameLayout) activity.getWindow().getDecorView();
        // 向DecorView中添加NotificationFrameLayout,
        NotificationFrameLayout notificationFrameLayout = new NotificationFrameLayout(activity);
        notificationFrameLayout.setZ(Constants.Z); // 给一个高度，这样可以覆盖在最上层
        notificationFrameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        decorFrameLayout.addView(notificationFrameLayout);
        // 更新map
        registeredActivityMap.put(activity, notificationFrameLayout);
    }

    /**
     * 移除已经添加的activity，避免引起内存泄漏。这个方法通常在onDestroy()中调用。
     * @param activity 待移除的activity
     *
     * @see #register(Activity)
     */
    public void unregister(Activity activity){
        // 正在显示的notification要消失
        NotificationFrameLayout notificationFrameLayout = registeredActivityMap.get(activity);
        if (notificationFrameLayout == null) {
            // 已经解注册了
            return;
        }

        notificationFrameLayout.dismissImmediately();
        // 清除记录
        registeredActivityList.remove(activity);
        registeredActivityMap.remove(activity);
    }

}
