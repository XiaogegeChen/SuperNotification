package cn.cse.neu.edu.supernotification.lib;

/**
 * 通知等级
 */
public enum Level {
    /**
     * 最严重的级别，发生错误需要通知用户时使用
     */
    ERROR,

    /**
     * 警告级别的通知，用于提示用户操作不当
     */
    WARNING,

    /**
     * 普通级别，用于给用户操作的反馈信息
     */
    INFO,

    /**
     * 成功级别，用于在用户很有成就感的时候使用
     */
    SUCCESS

}
