package com.hst.beautifulwall.app;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hst.beautifulwall.utility.Preference;

public class MyApplication extends Application {

    private static Preference preference = null;
    private static MyApplication application = null;
    public static Preference getInstance() {
        if (preference == null) {
            preference = Preference.buildInstance(application);
        }
        preference.isOpenFirst();
        return preference;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        FirebaseMessaging.getInstance().subscribeToTopic("firewallappnotification");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        application = this;

    }
}
