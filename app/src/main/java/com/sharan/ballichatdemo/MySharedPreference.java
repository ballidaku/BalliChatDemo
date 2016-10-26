package com.sharan.ballichatdemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by brst-pc93 on 10/21/16.
 */

public class MySharedPreference
{


    public final String PreferenceName = "MyPreference";


    public MySharedPreference()
    {
    }

    public static MySharedPreference instance = null;

    public static MySharedPreference getInstance()
    {
        if (instance == null)
        {
            instance = new MySharedPreference();
        }

        return instance;
    }


    public SharedPreferences getPreference(Context context)
    {
        return context.getSharedPreferences(PreferenceName, Activity.MODE_PRIVATE);
    }

    public void saveUser(Context context, String username, String userid)
    {
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.putString(MyConstants.USER_NAME, username);
        editor.putString(MyConstants.USER_ID, userid);
        editor.apply();
    }


    public void saveToken(Context context, String fcmToken)
    {
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.putString(MyConstants.FCM_TOKEN, fcmToken);
        editor.apply();
    }


    public String getUSER_ID(Context context)
    {
        return getPreference(context).getString(MyConstants.USER_ID,"");
    }


    public String getUSER_NAME(Context context)
    {
        return getPreference(context).getString(MyConstants.USER_NAME,"");
    }

    public String getFCM_TOKEN(Context context)
    {
        return getPreference(context).getString(MyConstants.FCM_TOKEN,"");
    }



    //To check is Chat_Room opened or not

    public void saveActivityState(Context context,boolean isActive)
    {
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.putBoolean(MyConstants.IS_CHAT_ROOM_ACTIVE, isActive);
        editor.apply();
    }

    public boolean getActivityState(Context context)
    {
        return getPreference(context).getBoolean(MyConstants.IS_CHAT_ROOM_ACTIVE,false);
    }


    //To check current chat Group ID
    public void saveCurrentGroupID(Context context,String groupID)
    {
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.putString(MyConstants.GROUP_ID, groupID);
        editor.apply();
    }

    public String getCurrentGroupID(Context context)
    {
        return getPreference(context).getString(MyConstants.GROUP_ID,"");
    }
}
