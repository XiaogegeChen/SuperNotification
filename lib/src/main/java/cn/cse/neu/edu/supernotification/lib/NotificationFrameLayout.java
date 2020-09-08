package cn.cse.neu.edu.supernotification.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 一个从顶部弹下来的ViewGroup
 */
@SuppressWarnings("unused")
public class NotificationFrameLayout extends FrameLayout {

    // ------------- 状态枚举 -----------
    // 飞行中
    public static final int STATE_FLING = 1000;
    // 拖动中
    public static final int STATE_DRAG = 1001;
    // 已消失
    public static final int STATE_DISMISS = 1002;
    // 完全展开
    public static final int STATE_EXPAND = 1003;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            STATE_FLING,
            STATE_DRAG,
            STATE_DISMISS,
            STATE_EXPAND
    })
    private @interface State{}

    // 默认的触发速度
    private static final int VELOCITY_THRESHOLD = 500;
    // 默认动画时长
    private static final int ANIMATION_DURATION = Constants.ANIMATION_DURATION;
    // 当手指松开时的位置超过高度的0.25时，就让他飞出屏幕
    private static final float FLY_FACTOR = 0.25f;
    // 出现后2s后自动消失
    private static final int DISMISS_DELAY = Constants.DISMISS_DELAY;

    // 标志位，true表示处于触摸滑动状态，false表示未处于触摸滑动状态，也就是触摸事件没有被该ViewGroup拦截
    private boolean isScrolling;
    // 用来检测是否拦截事件的x和y
    private float lastXUsedToDetectInterception;
    private float lastYUsedToDetectInterception;
    // 判定为滑动的阈值
    private int touchSlop;
    // 跟手滑动用来记录上一个点的y
    private float lastRawY;

    // 向下能移动的最大距离
    private int maxMoveDownDistance = Integer.MIN_VALUE;
    // 向上能移动的最大距离
    private int maxMoveUpDistance = Integer.MIN_VALUE;
    // 滑动速度追踪器
    private VelocityTracker velocityTracker;

    // 动画执行器
    private ObjectAnimator flyUpAnimator;
    private ObjectAnimator flyDownAnimator;

    // 当前状态
    private @State int state = STATE_EXPAND;
    // 消失定时器，当出现后开始定时，几秒后view消失，一旦view滑动则取消定时任务，滑动结束后如果view又回到下面了，则重新启用
    // 定时任务
    private Timer timer;

    public NotificationFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public NotificationFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        touchSlop = viewConfiguration.getScaledTouchSlop();

        velocityTracker = VelocityTracker.obtain();

        initAnimator();

        timer = new Timer();
    }

    /** 初始化Animator **/
    private void initAnimator(){
        flyUpAnimator = ObjectAnimator.ofFloat(this, TRANSLATION_Y, 0, 0);
        flyUpAnimator.setDuration(ANIMATION_DURATION);
        flyUpAnimator.setInterpolator(new OvershootInterpolator());
        flyUpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                state = STATE_DISMISS;
            }
        });

        flyDownAnimator = ObjectAnimator.ofFloat(this, TRANSLATION_Y, 0, 0);
        flyDownAnimator.setDuration(ANIMATION_DURATION);
        flyDownAnimator.setInterpolator(new OvershootInterpolator());
        flyDownAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                state = STATE_EXPAND;
            }
        });
    }

    /** 初始化上下边界 **/
    private void initMaxDistance(){
        maxMoveUpDistance = getHeight();
        maxMoveDownDistance = 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            isScrolling = false;
            return false;
        }

        switch (action){
            case MotionEvent.ACTION_MOVE:
                // 正在移动拦截
                if(isScrolling){
                    return true;
                }

                final float yDiff = Math.abs(lastYUsedToDetectInterception - ev.getY());
                final float xDiff = Math.abs(lastXUsedToDetectInterception - ev.getX());
                if(yDiff > xDiff){
                    if(yDiff > touchSlop){
                        isScrolling = true;
                        return true;
                    }
                }

                lastXUsedToDetectInterception = ev.getX();
                lastYUsedToDetectInterception = ev.getY();
                break;

            case MotionEvent.ACTION_DOWN:
                // ACTION_DOWN 不拦截
                lastXUsedToDetectInterception = ev.getX();
                lastYUsedToDetectInterception = ev.getY();
                return false;

            default:
                break;
        }

        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 先初始化上下边界的范围
        initMaxDistance();

        final int action = event.getActionMasked();
        final float currRawY = event.getRawY();

        // 手松开要释放滑动状态
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            isScrolling = false;
        }

        switch (action){
            case MotionEvent.ACTION_DOWN:
                state = STATE_DRAG;
                cancelTimer();

                lastRawY = currRawY;
                break;

            case MotionEvent.ACTION_MOVE:
                // 跟手上下滑动
                final float yDiff = currRawY - lastRawY;
                // 检查边界
                final float preTranslationY = getTranslationY() + yDiff;
                final float translationY;
                if(preTranslationY < -maxMoveUpDistance){
                    // 超越上界
                    translationY = -maxMoveUpDistance;
                }else if(preTranslationY > maxMoveDownDistance){
                    // 超越下界
                    translationY = maxMoveDownDistance;
                }else{
                    translationY = preTranslationY;
                }
                setTranslationY(translationY);

                lastRawY = currRawY;
                break;

            case MotionEvent.ACTION_UP:
                state = STATE_FLING;

                velocityTracker.computeCurrentVelocity(1000);
                final int yVelocity = (int) velocityTracker.getYVelocity();
                if(Math.abs(yVelocity) > VELOCITY_THRESHOLD){
                    // 沿着速度方向动画出去
                    if(yVelocity > 0){
                        flyDown(false);
                        refreshTimer();
                    }else{
                        flyUp(null, false);
                    }
                }else{
                    // 根据当前滑动的距离判断
                    final float currTranslationY = getTranslationY();
                    if (Math.abs(currTranslationY) > getHeight() * FLY_FACTOR){
                        flyUp(null, false);
                    }else{
                        flyDown(false);
                        refreshTimer();
                    }
                }
                break;

            default:
                break;
        }

        velocityTracker.addMovement(event);
        return true;
    }

    /** 重新定时 **/
    private void refreshTimer(){
        timer = new Timer();
        timer.schedule(new DismissTask(), DISMISS_DELAY);
    }

    /** 取消定时 **/
    private void cancelTimer(){
        timer.cancel();
    }

    /** 飞出屏幕，完全消失的时候通知监听器 **/
    private void flyUp(@Nullable final OnDismissListener onDismissListener, boolean immediately){
        initMaxDistance();
        if(immediately){
            setTranslationY(-maxMoveUpDistance);
            state = STATE_DISMISS;
        }else{
            final float currTranslationY = getTranslationY();
            flyUpAnimator.setFloatValues(currTranslationY, -maxMoveUpDistance);
            if (onDismissListener != null) {
                flyUpAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        flyUpAnimator.removeListener(this);
                        onDismissListener.onDismiss();
                    }
                });
            }
            flyUpAnimator.start();
        }
    }

    /** 向下飞到最底部，immediately用来控制是动画还是立即执行 **/
    private void flyDown(boolean immediately){
        initMaxDistance();
        if(immediately){
            setTranslationY(maxMoveDownDistance);
            state = STATE_EXPAND;
        }else{
            final float currTranslationY = getTranslationY();
            flyDownAnimator.setFloatValues(currTranslationY, maxMoveDownDistance);
            flyDownAnimator.start();
        }
    }

    /**
     * 使该view消失，也就是飞出屏幕，这个方法是通过动画的方式实现的
     * @param onDismissListener 监听器，当完全消失的时候会通知该监听器
     */
    public void dismiss(@Nullable OnDismissListener onDismissListener){
        flyUp(onDismissListener, false);
        cancelTimer();
    }

    /**
     * 使该view立即消失
     */
    public void dismissImmediately(){
        flyUp(null, true);
        cancelTimer();
    }

    /**
     * 使该view出现，这个方法是通过动画的方式实现的
     */
    public void show(){
        flyDown(false);
        // 显示一段时间后自动消失
        refreshTimer();
    }

    /**
     * 使该view立即出现
     */
    public void showImmediately(){
        flyDown(true);
    }

    /**
     * 获取当前的状态
     * @return 当前的状态
     *
     * @see State
     */
    @State
    public int getState(){
        return state;
    }

    /**
     * 监听该view消失的监听器
     */
    public interface OnDismissListener {
        /**
         * 当view消失时会回调该方法
         */
        void onDismiss();
    }

    /**
     * 控制view消失的定时任务
     */
    class DismissTask extends TimerTask {

        @Override
        public void run() {
            post(new Runnable() {
                @Override
                public void run() {
                    dismiss(null);
                }
            });
        }
    }
}
