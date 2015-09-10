package com.ihs.demo.message;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.style.TtsSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.message.R;
import com.ihs.message.managers.HSMessageChangeListener;
import com.ihs.message.managers.HSMessageManager;
import com.ihs.message.types.HSBaseMessage;
import com.ihs.message.types.HSOnlineMessage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessagesFragment extends Fragment implements INotificationObserver, HSMessageChangeListener{

    private ListView listView;
    private MessageAdapter messageAdapter = null;
    private List<HSBaseMessage> list_hsBaseMessage = new ArrayList<>();
    private HSMessageChangeType changeType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        listView = (ListView) view.findViewById(R.id.message_list);
        final List<Contact> contacts = new ArrayList<>();

        HSMessageManager.getInstance().addListener(this, new Handler());
        messageAdapter = new MessageAdapter(this.getActivity(), R.layout.cell_item_message, contacts);
        listView.setAdapter(messageAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mid = contacts.get(position).getMid();
                String name = contacts.get(position).getName();
                //Toast.makeText(getActivity(), "你点击了名字为：" + name + " mid为：" + mid + "的联系人，需要在这里跳转到同此人的聊天界面（一个Activity）", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setClass(getActivity(), ChatActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("mid", mid);
                startActivity(intent);

            }

        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String mid = contacts.get(position).getMid();
                new AlertDialog.Builder(getActivity()).setTitle("HINT")
                        .setMessage("Are you sure to delete?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                HSMessageManager.getInstance().deleteMessages(mid);
                            }
                        }).setNegativeButton("No", null).show();
                return true;
            }
        });
        refresh();
        return view;
    }
    public void refresh(){

        List<Contact> list_contact = new ArrayList<>();
        messageAdapter.getContacts().clear();
        List<Contact> contacts = new ArrayList<>();
        contacts = FriendManager.getInstance().getAllFriends();
        try {

            for (Contact contact: contacts){
                List<HSBaseMessage> res = HSMessageManager.getInstance().queryMessages(contact.getMid(), 1, -1).getMessages();
                if(!res.isEmpty()){
                    list_contact.add(contact);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Collections.sort(list_contact, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                List<HSBaseMessage> lans = HSMessageManager.getInstance().queryMessages(lhs.getMid(), 1, -1).getMessages();
                List<HSBaseMessage> rans = HSMessageManager.getInstance().queryMessages(rhs.getMid(), 1, -1).getMessages();
                return (rans.get(0).getTimestamp().getTime() > lans.get(0).getTimestamp().getTime()) ? 1 : -1;
            }
        });
        messageAdapter.getContacts().addAll(list_contact);
        messageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy(){
        HSMessageManager.getInstance().removeListener(this);
        super.onDestroy();
    }
    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        refresh();
        changeType =(HSMessageChangeType)hsBundle.getObject("changeType");
        List<?> list = hsBundle.getObjectList("message");
        for(int i = 0; i < list.size(); i++){
            HSBaseMessage hs_Message = (HSBaseMessage)list.get(i);
            this.list_hsBaseMessage.add(hs_Message);
        }
    }

    @Override
    public void onMessageChanged(HSMessageChangeType changeType, List<HSBaseMessage> messages) {
        refresh();
    }

    @Override
    public void onTypingMessageReceived(String fromMid) {

    }

    @Override
    public void onOnlineMessageReceived(HSOnlineMessage message) {

    }

    @Override
    public void onUnreadMessageCountChanged(String mid, int newCount) {

    }

    @Override
    public void onReceivingRemoteNotification(JSONObject pushInfo) {

    }
}
