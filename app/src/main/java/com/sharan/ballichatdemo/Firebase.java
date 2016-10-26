package com.sharan.ballichatdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by brst-pc93 on 10/21/16.
 */

public class Firebase
{

    public static Firebase instance = null;

    DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();


    public static Firebase getInstance()
    {
        if (instance == null)
        {
            instance = new Firebase();
        }

        return instance;
    }


    public String getUniqueID()
    {
        return String.valueOf(System.currentTimeMillis());
    }


    public void createUser(final Context context, final String username)
    {
        final String userID = getUniqueID();

        HashMap<String, Object> result = new HashMap<>();
        result.put(MyConstants.USER_NAME, username);
        result.put(MyConstants.FCM_TOKEN, MySharedPreference.getInstance().getFCM_TOKEN(context));

        root.child(MyConstants.USERS).child(userID).updateChildren(result).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    MySharedPreference.getInstance().saveUser(context, username, userID);

                    Toast.makeText(context, "User Created Successfull", Toast.LENGTH_SHORT).show();

                    context.startActivity(new Intent(context, MainActivity.class));

                    ((Activity) context).finish();
                }

            }
        });
    }


    public void createGroup(final Context context, final String groupName)
    {


        String userId = MySharedPreference.getInstance().getUSER_ID(context);
        String userName = MySharedPreference.getInstance().getUSER_NAME(context);
        String groupID = getUniqueID();

        HashMap<String, Object> result = new HashMap<>();
        result.put(MyConstants.GROUP_ID, groupID);
        result.put(MyConstants.GROUP_NAME, groupName);
        result.put(MyConstants.ADMIN_NAME, userName);
        result.put(MyConstants.ADMIN_ID, userId);


        root.child(MyConstants.GROUP_CHAT_NAMES).push().updateChildren(result);

        // After creating the group admin is subscribed to his own group
        subscribeUserToGroup(userId,userName,groupID);
    }


    public Query getMyGroups(Context context)
    {
        return root.child(MyConstants.GROUP_CHAT_NAMES).orderByChild(MyConstants.ADMIN_ID).equalTo(MySharedPreference.getInstance().getUSER_ID(context));
    }


    public Query getMySubscribeGroupsIDS(Context context)
    {
//       return root.child(SUBSCRIBERS).orderByChild(USER_ID).equalTo(MySharedPreference.getInstance().getUSER_ID(context));
//        return root.child(SUBSCRIBERS).orderByKey().endAt(USER_ID);
        Log.e("UserID", "" + MySharedPreference.getInstance().getUSER_ID(context));
        return root.child(MyConstants.SUBSCRIBERS);
    }

    public Query getMySubscribeGroupsDATA(String groupID)
    {
        return root.child(MyConstants.GROUP_CHAT_NAMES).orderByChild(MyConstants.GROUP_ID).equalTo(groupID);
    }

    public Query getUsers()
    {
        return root.child(MyConstants.USERS);
    }

    public Query getUserFcmToken(String userID)
    {
        return root.child(MyConstants.USERS).child(userID);
    }

    public void subscribeUserToGroup(String userID, String userName, String groupID)
    {
        HashMap<String, Object> result = new HashMap<>();
        result.put(userID, userName);

        root.child(MyConstants.SUBSCRIBERS).child(groupID).updateChildren(result);
    }


    public Query getGroupSubscribedUsers(String groupID)
    {
       return root.child(MyConstants.SUBSCRIBERS).child(groupID);
    }


/*    public void updateToken(Context context,String userID)
    {
        HashMap<String, Object> result = new HashMap<>();
        result.put( userID,MySharedPreference.getInstance().getFCM_TOKEN(context));

        root.child(MyConstants.FCM_TOKEN).updateChildren(result).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.e("Exception",""+e);
            }
        });

    }*/


    public void sendNotification(String fcmID, HashMap<String, String> map)
    {
        // String TO = "d87QHGv1CbU:APA91bGg1A0ntLpqcmQhlXU4RckCe9sSIqfmwdiHxLbkenSTVJc-gZMHdT6YVMQxDroUQXKNQOr04QbjUhqI3UtLfmp-svZqPPS-aRt7rewiGDDotcYlByOF6TUOzOfOLX6s99477KZb";

        String body123 = "{\n\t\"to\": \" " + fcmID + "\",\n\t\"notification\" :" + convert(map) + "\n}";

        Log.e("DataToSend", "" + body123);

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        // RequestBody body = RequestBody.create(mediaType, "{\n\t\"to\": \"d87QHGv1CbU:APA91bGg1A0ntLpqcmQhlXU4RckCe9sSIqfmwdiHxLbkenSTVJc-gZMHdT6YVMQxDroUQXKNQOr04QbjUhqI3UtLfmp-svZqPPS-aRt7rewiGDDotcYlByOF6TUOzOfOLX6s99477KZb\",\n\t\"notification\" :{\n\t\t\"body\":\"Testing Balli Daku Navjot\",\n\t\t\"title\":\"Title\"\n\t}\n}");

        RequestBody body = RequestBody.create(mediaType, body123);
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .addHeader("authorization", "key=" + MyConstants.SERVER_KEY)
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                //.addHeader("postman-token", "dfe29a6f-e40a-6b27-4065-6a25b103db56")
                .build();


        // Response response = client.newCall(request).execute();


        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e)
            {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException
            {
                if (!response.isSuccessful())
                {
                    throw new IOException("Unexpected code " + response);
                }
                else
                {
                    Log.e("ResponseBALLI", response.body().string());
                }
            }


        });


    }

    public String convert(HashMap<String, String> map)
    {
        JSONObject obj = null;
        try
        {
            obj = new JSONObject(map);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return obj.toString();
    }


}
