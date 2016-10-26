package com.sharan.ballichatdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Chat_Room extends AppCompatActivity implements View.OnClickListener
{
    private Button btn_send_msg;
    private EditText input_msg;
    private TextView chat_conversation;

    private String userName, groupName, groupID;
    private DatabaseReference root;
    private String temp_key;

    private String chat_msg, chat_user_name;

    String myUserID;


    Context context;

    ArrayList<HashMap<String, String>> listUsersWithFcmToken = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat__room);


        context = this;


        myUserID = MySharedPreference.getInstance().getUSER_ID(context);

        btn_send_msg = (Button) findViewById(R.id.btn_send);
        input_msg = (EditText) findViewById(R.id.msg_input);
        chat_conversation = (TextView) findViewById(R.id.textView);


        findViewById(R.id.btn_add_user).setOnClickListener(this);

        userName = MySharedPreference.getInstance().getUSER_NAME(context);

        groupName = getIntent().getExtras().get(MyConstants.GROUP_NAME).toString();
        groupID = getIntent().getExtras().get(MyConstants.GROUP_ID).toString();


        Log.e("groupID", "" + groupID);

        setTitle(" Room - " + groupName);

        root = FirebaseDatabase.getInstance().getReference().child(MyConstants.GROUP_CHATS).child(groupID);


        getSubscribedUsers();


        btn_send_msg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                String messageText = input_msg.getText().toString().trim();

                Map<String, Object> map = new HashMap<String, Object>();
                temp_key = root.push().getKey();
                root.updateChildren(map);

                DatabaseReference message_root = root.child(temp_key);
                Map<String, Object> map2 = new HashMap<String, Object>();
                map2.put(MyConstants.USER_NAME, userName);
                map2.put(MyConstants.MESSAGE, messageText);
                map2.put(MyConstants.GROUP_NAME, groupName);

                message_root.updateChildren(map2);


                //Notification send to other user

                String message = "{\n" +
                        "  \"GroupID\": \"" + groupID + "\",\n" +
                        "  \"GroupName\": \"" + groupName + "\",\n" +
                        "  \"Message\": \"" + groupName + " : " + messageText + "\",\n" +
                        "  \"SenderName\": \"" + userName + "\"\n" +
                        "}";


                for (int i = 0; i < listUsersWithFcmToken.size(); i++)
                {
                    HashMap<String, String> message_map = new HashMap<String, String>();
                    message_map.put("title", "BalliChatDemo");
                    message_map.put("body", message);

                    Firebase.getInstance().sendNotification(listUsersWithFcmToken.get(i).get(MyConstants.FCM_TOKEN), message_map);
                }


                input_msg.setText("");
            }
        });

        root.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {

                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {

                append_chat_conversation(dataSnapshot);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

    }


    private void append_chat_conversation(DataSnapshot dataSnapshot)
    {

        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext())
        {
            groupName = (String) ((DataSnapshot) i.next()).getValue();
            chat_msg = (String) ((DataSnapshot) i.next()).getValue();
            chat_user_name = (String) ((DataSnapshot) i.next()).getValue();


            chat_conversation.append(chat_user_name + " : " + chat_msg + " \n");
        }


    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_add_user:

                getAllUsers();

                break;
        }
    }

    private void getAllUsers()
    {
        Query queryRef = Firebase.getInstance().getUsers();

        queryRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                Log.e("ba", "" + dataSnapshot.getValue());

                ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext())
                {
                    DataSnapshot s = (DataSnapshot) i.next();

                    if (!(s.getKey().equals(myUserID)))
                    {

                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put(MyConstants.FCM_TOKEN, (String) s.child(MyConstants.FCM_TOKEN).getValue());
                        map.put(MyConstants.USER_NAME, (String) s.child(MyConstants.USER_NAME).getValue());
                        map.put(MyConstants.USER_ID, s.getKey());

                        list.add(map);
                    }

                }

                if (MySharedPreference.getInstance().getActivityState(context))
                {
                    showUsers(list);
                }


                Log.e("List", "" + list);

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void showUsers(final ArrayList<HashMap<String, String>> list)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Users in this group");

        SimpleAdapter arrayAdapter = new SimpleAdapter(this, list, R.layout.custom_view, new String[]{MyConstants.USER_NAME}, new int[]{R.id.txtv_group_name});


        final ListView listView = new ListView(this);
        listView.setAdapter(arrayAdapter);


        builder.setView(listView);

        final AlertDialog ad = builder.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                subscribeToThisGroup(list.get(i).get(MyConstants.USER_ID), list.get(i).get(MyConstants.USER_NAME));
                String msg = "You are added in the group " + groupName + " by " + MySharedPreference.getInstance().getUSER_NAME(context);

                String message = "{\n" +
                        "  \"GroupID\": \"" + groupID + "\",\n" +
                        "  \"GroupName\": \"" + groupName + "\",\n" +
                        "  \"Message\": \"" + msg + "\",\n" +
                        "  \"SenderName\": \"" + userName + "\"\n" +
                        "}";


                HashMap<String, String> map = new HashMap<String, String>();
                map.put("title", "Subscription");
                map.put("body", message);

                Firebase.getInstance().sendNotification(list.get(i).get(MyConstants.FCM_TOKEN), map);

                ad.dismiss();
            }
        });


    }

    private void subscribeToThisGroup(String userID, String userName)
    {
        Firebase.getInstance().subscribeUserToGroup(userID, userName, groupID);
    }


    // First to get userIds which arer subcribed to group using groupID and then get users FcmToken from users group


    public void getSubscribedUsers()
    {
        listUsersWithFcmToken.clear();

        Firebase.getInstance().getGroupSubscribedUsers(groupID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //   Log.e("Subscribers", "" + dataSnapshot.getValue());


                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext())
                {
                    DataSnapshot s = (DataSnapshot) i.next();

                    String userId = s.getKey();
                    if (!userId.equals(myUserID))
                    {
                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put(MyConstants.USER_NAME, (String) s.getValue());
                        map.put(MyConstants.USER_ID, userId);
                        map.put(MyConstants.FCM_TOKEN, "");

                        listUsersWithFcmToken.add(map);
                    }

                }
                //  Log.e("List", "" + listUsersWithFcmToken);


                for (int j = 0; j < listUsersWithFcmToken.size(); j++)
                {
                    final int position = j;
                    Firebase.getInstance().getUserFcmToken(listUsersWithFcmToken.get(position).get(MyConstants.USER_ID)).addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {

                            //  Log.e("Response", "" + dataSnapshot.getValue());

                            String token = (String) dataSnapshot.child(MyConstants.FCM_TOKEN).getValue();


                            //  Log.e("FCM", "" + token);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(MyConstants.USER_NAME, listUsersWithFcmToken.get(position).get(MyConstants.USER_NAME));
                            map.put(MyConstants.USER_ID, listUsersWithFcmToken.get(position).get(MyConstants.USER_ID));
                            map.put(MyConstants.FCM_TOKEN, token);


                            listUsersWithFcmToken.set(position, map);


                            Log.e("listUsersWithFcmToken", "" + listUsersWithFcmToken);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }


    @Override
    protected void onResume()
    {

        MySharedPreference.getInstance().saveActivityState(context, true);
        MySharedPreference.getInstance().saveCurrentGroupID(context, groupID);
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        MySharedPreference.getInstance().saveActivityState(context, false);
        MySharedPreference.getInstance().saveCurrentGroupID(context, "");
        super.onPause();
    }
}