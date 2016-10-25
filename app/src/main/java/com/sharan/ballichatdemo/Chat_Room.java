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

    private String user_name, groupName, groupID;
    private DatabaseReference root;
    private String temp_key;

    private String chat_msg, chat_user_name;


    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat__room);


        context =this;

        btn_send_msg = (Button) findViewById(R.id.btn_send);
        input_msg = (EditText) findViewById(R.id.msg_input);
        chat_conversation = (TextView) findViewById(R.id.textView);


        findViewById(R.id.btn_add_user).setOnClickListener(this);

        user_name = MySharedPreference.getInstance().getUSER_NAME(context);

        groupName = getIntent().getExtras().get(MyConstants.GROUP_NAME).toString();
        groupID = getIntent().getExtras().get(MyConstants.GROUP_ID).toString();


        Log.e("groupID",""+groupID);

        setTitle(" Room - " + groupName);

        // root = FirebaseDatabase.getInstance().getReference().child(room_name);
        root = FirebaseDatabase.getInstance().getReference().child(MyConstants.GROUP_CHATS).child(groupID);

        btn_send_msg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Map<String, Object> map = new HashMap<String, Object>();
                temp_key = root.push().getKey();
                root.updateChildren(map);

                DatabaseReference message_root = root.child(temp_key);
                Map<String, Object> map2 = new HashMap<String, Object>();
                map2.put(MyConstants.USER_NAME, user_name);
                map2.put(MyConstants.MESSAGE, input_msg.getText().toString());
                map2.put(MyConstants.GROUP_NAME, groupName);

                message_root.updateChildren(map2);


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



           // groupName = (String) ((DataSnapshot) i.next()).getValue();
//            chat_msg = (String) ((DataSnapshot) i.next()).getValue();


//            Log.e("1",(String) ((DataSnapshot)i.next()).getValue());
//            Log.e("2",(String) ((DataSnapshot)i.next()).getValue());
//            Log.e("3",(String) ((DataSnapshot)i.next()).getValue());

            chat_conversation.append(chat_user_name + " : " + chat_msg + " \n");
        }


    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_add_user:

                getUsers();

                break;
        }
    }

    private void getUsers()
    {
        Query queryRef =Firebase.getInstance().getUsers();

        queryRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                Log.e("ba",""+dataSnapshot.getValue());

                ArrayList<HashMap<String,String>> list=new ArrayList<HashMap<String, String>>();

                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext())
                {
                    DataSnapshot s = (DataSnapshot) i.next();

                    if(!(s.getKey().equals(MySharedPreference.getInstance().getUSER_ID(context))))
                    {

                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put(MyConstants.FCM_TOKEN, (String) s.child(MyConstants.FCM_TOKEN).getValue());
                        map.put(MyConstants.USER_NAME, (String) s.child(MyConstants.USER_NAME).getValue());
                        map.put(MyConstants.USER_ID, (String) s.getKey());

                        list.add(map);
                    }

                }

                showUsers(list);

                Log.e("List",""+list);

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

        SimpleAdapter arrayAdapter = new SimpleAdapter(this, list, R.layout.custom_view, new String[] { MyConstants.USER_NAME },new int[]{R.id.txtv_group_name});


        final ListView listView = new ListView(this);
        listView.setAdapter(arrayAdapter);



        builder.setView(listView);

       final AlertDialog ad = builder.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                subscribeToThisGroup(list.get(i).get(MyConstants.USER_ID),list.get(i).get(MyConstants.USER_NAME));

               HashMap<String,String> map=new HashMap<String, String>();
                map.put("title","Subscription");
                map.put("body","You are added in the group "+groupName+" by "+MySharedPreference.getInstance().getUSER_NAME(context));

                Firebase.getInstance().sendNotification(list.get(i).get(MyConstants.FCM_TOKEN),map);

                ad.dismiss();
            }
        });


    }

    private void subscribeToThisGroup(String userID, String userName)
    {
        Firebase.getInstance().subscribeUserToGroup(userID,userName,groupID);
    }


}