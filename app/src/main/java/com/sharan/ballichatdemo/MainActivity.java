package com.sharan.ballichatdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
{

    Button btn_add;
    EditText editText;
    ListView listView_room;

    SimpleAdapter arrayAdapter;
    //  ArrayList<String> list_of_rooms = new ArrayList<>();

    String name;

//    DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();

    List<HashMap<String, String>> list = new ArrayList<>();
    Set<HashMap<String,String>> set = new HashSet<HashMap<String,String>>();

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        context = this;

        setTitle(" UserName - " + MySharedPreference.getInstance().getUSER_NAME(context));


        btn_add = (Button) findViewById(R.id.btn_add);
        editText = (EditText) findViewById(R.id.editText);
        listView_room = (ListView) findViewById(R.id.listView_room);

        arrayAdapter = new SimpleAdapter(this, list, R.layout.custom_view, new String[]{MyConstants.GROUP_NAME}, new int[]{R.id.txtv_group_name});


        listView_room.setAdapter(arrayAdapter);

        btn_add.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                String groupName = editText.getText().toString().trim();

                if (!groupName.isEmpty())
                {
                    Firebase.getInstance().createGroup(context, editText.getText().toString());
                }
                else
                {
                    Toast.makeText(context, "Please enter group name", Toast.LENGTH_SHORT).show();
                }


                editText.setText("");

            }
        });


        Query queryRef = Firebase.getInstance().getMyGroups(context);


        queryRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                Log.e("ba", "" + dataSnapshot.getValue());


              //  list.clear();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext())
                {


                    DataSnapshot s = (DataSnapshot) i.next();

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(MyConstants.GROUP_NAME, (String) s.child(MyConstants.GROUP_NAME).getValue());
                    map.put(MyConstants.GROUP_ID, (String) s.child(MyConstants.GROUP_ID).getValue());

                    set.add(map);

                    //set.add((String) (s).getChildren().iterator().next().child("GroupName").getValue());
                    //Log.e("Re",""+((DataSnapshot) i.next()).getChildren().iterator().next().child("GroupName").getValue());
                }



                list.clear();
                list.addAll(set);

                // list_of_rooms.clear();
                // list_of_rooms.addAll(set);


                arrayAdapter.notifyDataSetChanged();


                getMoreGroups();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });




        listView_room.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent intent = new Intent(getApplicationContext(), Chat_Room.class);
                intent.putExtra(MyConstants.GROUP_NAME, list.get(i).get(MyConstants.GROUP_NAME));
                intent.putExtra(MyConstants.USER_NAME, name);
                intent.putExtra(MyConstants.GROUP_ID, list.get(i).get(MyConstants.GROUP_ID));
                startActivity(intent);
            }
        });
    }







    private void getMoreGroups()
    {
        Query queryRef2 = Firebase.getInstance().getMySubscribeGroupsIDS(context);

        queryRef2.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.e("Subscribers", "" + dataSnapshot.getValue());

                ArrayList<String> sublist = new ArrayList<String>();

                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext())
                {
                    DataSnapshot s = (DataSnapshot) i.next();

                   // Log.e("G ID", "" +  s.getKey());

                    Iterator child = s.getChildren().iterator();
                    while (child.hasNext())
                    {
                        DataSnapshot subChild = (DataSnapshot) child.next();

                       // Log.e("CCCCCCCCCCCCC", "" +  subChild.getKey());


                        if(subChild.getKey().equals(MySharedPreference.getInstance().getUSER_ID(context)))
                        {
                            sublist.add(s.getKey());
                            break;
                        }
                    }
                }

                Log.e("S List", "" + sublist);

                if( sublist.size()>0)
                {
                    for (int j = 0; j <sublist.size() ; j++)
                    {
                        getSubscribedGroups(sublist.get(j));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }


    public void getSubscribedGroups(final String groupID)
    {
        Query queryRef3 = Firebase.getInstance().getMySubscribeGroupsDATA(context, groupID);

        queryRef3.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                Log.e("Su 2"+""+groupID, "" + dataSnapshot.getValue());


                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext())
                {
                    DataSnapshot s = (DataSnapshot) i.next();

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(MyConstants.GROUP_NAME, (String) s.child(MyConstants.GROUP_NAME).getValue());
                    map.put(MyConstants.GROUP_ID, (String) s.child(MyConstants.GROUP_ID).getValue());

                    set.add(map);

                }

                list.clear();
                list.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

    }

}
