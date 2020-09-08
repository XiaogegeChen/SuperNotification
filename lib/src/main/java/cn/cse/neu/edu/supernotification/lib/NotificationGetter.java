package cn.cse.neu.edu.supernotification.lib;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

/**
 * 用来获取各种Notification的类，如果需要使用自定义的Notification样式，则需要扩展该类并覆盖指定的方法。如果没有指定样式
 * 则使用默认的样式。
 */
public abstract class NotificationGetter {

    /** 默认的view的map **/
    private HashMap<Level, View> defaultViewMap = new HashMap<>();

    /**
     * 获得相应级别的notification的view，如果返回null则会使用默认的样式。
     * @param level notification 级别
     * @return 对应的notification的view，返回null则会使用默认的样式。
     */
    @Nullable
    public abstract View getNotificationView(Level level);

    /**
     * 为指定级别的view设置消息。如果是使用默认的则返回false，如果是自定义的则返回true。
     * @param view 该级别下的view
     * @param level 该notification 级别
     * @param message 待显示的信息
     * @return 如果是使用默认的则返回false，如果是自定义的则返回true。
     */
    public abstract boolean setMessage(View view, Level level, String message);

    /**
     * 获得相应级别的notification的view，内部调用
     * @param level notification 级别
     * @return 对应的notification的view
     */
    @NonNull
    View getNotificationViewInternal(Level level){
        // 如果用户提供的view是null，则使用默认的view
        View view = getNotificationView(level);
        if (view == null) {
            view = getDefaultView(level);
        }
        return view;
    }

    /**
     * 为指定级别的view设置消息。内部使用
     * @param view 该级别下的view
     * @param level 该notification 级别
     * @param message 待显示的信息
     */
    void setMessageInternal(View view, Level level, String message){
        boolean success = setMessage(view, level, message);
        if(!success){
            // 默认的view
            // 通过tag判断是不是默认的view
            Object tag = view.getTag();
            if (tag instanceof String && tag.equals(Constants.DEFAULT_VIEW_TAG)) {
                // 默认的view
                setMessageToDefaultView(view, message);
            }else{
                // 不是默认的，有错误
                throw new IllegalArgumentException("You must return true from the setMessage(View, Level, String) method");
            }
        }
    }

    /** 获取指定级别的默认的view **/
    @NonNull
    private View getDefaultView(Level level){
        LayoutInflater inflater = LayoutInflater.from(SuperNotification.applicationContext);
        View view = defaultViewMap.get(level);
        if (view != null) {
            // 返回已经缓存的view
            return view;
        }
        // 从资源文件中加载
        switch (level){
            case ERROR:
                view = inflater.inflate(R.layout.error, null, false);
                break;
            case WARNING:
                view = inflater.inflate(R.layout.warning, null, false);
                break;
            case INFO:
                view = inflater.inflate(R.layout.info, null, false);
                break;
            case SUCCESS:
                view = inflater.inflate(R.layout.success, null, false);
                break;
        }
        view.setTag(Constants.DEFAULT_VIEW_TAG);
        defaultViewMap.put(level, view);
        return view;
    }

    /** 为默认的view设置message **/
    private void setMessageToDefaultView(View view, String message){
        ((TextView)(view.findViewById(R.id.msg))).setText(message);
    }
}
