package cn.cse.neu.edu.supernotification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import cn.cse.neu.edu.supernotification.lib.Level;
import cn.cse.neu.edu.supernotification.lib.NotificationGetter;

public class CustomNotificationGetter extends NotificationGetter {
    private Context applicationContext;

    public CustomNotificationGetter(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Nullable
    @Override
    public View getNotificationView(Level level) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(applicationContext);
        switch (level){
            case SUCCESS:
                view = inflater.inflate(R.layout.my_success, null, false);
                break;
            case INFO:
                view = inflater.inflate(R.layout.my_info, null, false);
                break;
            case WARNING:
                view = inflater.inflate(R.layout.my_warning, null, false);
                break;
            case ERROR:
                view = inflater.inflate(R.layout.my_error, null, false);
                break;
            default:
                return null;
        }
        return view;
    }

    @Override
    public boolean setMessage(View view, Level level, String message) {
        ((TextView)(view.findViewById(R.id.msg))).setText(message);
        return true;
    }
}
