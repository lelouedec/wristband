package justin_lelouedec.project;


        import android.annotation.SuppressLint;
        import android.annotation.TargetApi;
        import android.app.Notification;
        import android.content.Context;
        import android.content.Intent;
        import android.graphics.drawable.Icon;
        import android.os.Build;
        import android.os.Bundle;
        import android.service.notification.NotificationListenerService;
        import android.service.notification.StatusBarNotification;
        import android.util.Log;
        import android.support.v4.content.LocalBroadcastManager;
        import android.widget.Toast;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService {

    Context context;

    @Override

    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();

    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override

    public void onNotificationPosted(StatusBarNotification sbn) {

        Notification notif = sbn.getNotification();

        Bundle extras = notif.extras;
        String title = extras.getString("android.title");
        String text = extras.getCharSequence("android.text").toString();


        Log.i("Title",title);
        Log.i("Text",text);



        Intent msgrcv = new Intent("Msg");
        msgrcv.putExtra("package", notif.category);
        msgrcv.putExtra("title", title);
        msgrcv.putExtra("text", text);



        LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);


    }

    @Override

    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg", "Notification Removed");

    }

}