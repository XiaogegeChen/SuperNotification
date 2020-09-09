[![](https://jitpack.io/v/XiaogegeChen/SuperNotification.svg)](https://jitpack.io/#XiaogegeChen/SuperNotification)<br>
# SuperNotification
A convenient notification component for Android.<br>

## 效果图(真机上很流畅，这里做了放慢处理)
<img src="https://github.com/XiaogegeChen/SuperNotification/blob/master/pic/v1.gif" width="75%" align="middle"><br>

---

<table>
    <tr>
        <td><center><img src="https://github.com/XiaogegeChen/SuperNotification/blob/master/pic/success.jpg">success</center></td>
        <td><center><img src="https://github.com/XiaogegeChen/SuperNotification/blob/master/pic/information.jpg">information</center></td>
      <td><center><img src="https://github.com/XiaogegeChen/SuperNotification/blob/master/pic/warning.jpg">warning</center></td>
        <td><center><img src="https://github.com/XiaogegeChen/SuperNotification/blob/master/pic/error.jpg">error</center></td>
    </tr>

    
</table>

---

## 快速使用
1. 在工程根目录的build.gradle中添加依赖
```
allprojects {
    repositories {
        google()
        jcenter()
        
        maven { url 'https://jitpack.io' } 
    }
}
```
---
2. 在工程目录的build.gradle中添加依赖(查看最上面的版本号进行替换)
```
implementation 'com.github.XiaogegeChen:SuperNotification:last-version'
```
3. 项目中的```Activity```继承[BaseActivity](https://github.com/XiaogegeChen/SuperNotification/blob/master/lib/src/main/java/cn/cse/neu/edu/supernotification/lib/BaseActivity.java)或将其中的逻辑复制到自己的```BaseActivity```中。
4. 一行代码显示通知（像不像```Toast```？）
```
SuperNotification.getInstance().show(Level.WARNING, "Go away, do not touch me");
```
5. 不想用自带的样式怎么办？继承[NotificationGetter](https://github.com/XiaogegeChen/SuperNotification/blob/master/lib/src/main/java/cn/cse/neu/edu/supernotification/lib/NotificationGetter.java)，覆盖两个方法并设置给```SuperNotification```即可(参考[CustomNotificationGetter](https://github.com/XiaogegeChen/SuperNotification/blob/master/app/src/main/java/cn/cse/neu/edu/supernotification/CustomNotificationGetter.java)和[DefaultNotificationGetter](https://github.com/XiaogegeChen/SuperNotification/blob/master/lib/src/main/java/cn/cse/neu/edu/supernotification/lib/DefaultNotificationGetter.java))。
```java
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
```
----
更多用法参考[MainActivity](https://github.com/XiaogegeChen/SuperNotification/blob/master/app/src/main/java/cn/cse/neu/edu/supernotification/MainActivity.java)

