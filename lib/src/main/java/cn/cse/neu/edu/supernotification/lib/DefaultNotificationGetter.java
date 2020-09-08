package cn.cse.neu.edu.supernotification.lib;

import android.view.View;

import androidx.annotation.Nullable;

/**
 * 默认的{@link NotificationGetter}
 */
final class DefaultNotificationGetter extends NotificationGetter{

    @Nullable
    @Override
    public View getNotificationView(Level level) {
        // 使用默认的
        return null;
    }

    @Override
    public boolean setMessage(View view, Level level, String message) {
        // 使用默认的
        return false;
    }
}
