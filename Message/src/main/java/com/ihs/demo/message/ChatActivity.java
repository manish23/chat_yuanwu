package com.ihs.demo.message;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.message.R;
import com.ihs.message.managers.HSMessageChangeListener;
import com.ihs.message.managers.HSMessageManager;
import com.ihs.message.types.HSAudioMessage;
import com.ihs.message.types.HSBaseMessage;
import com.ihs.message.types.HSImageMessage;
import com.ihs.message.types.HSMessageType;
import com.ihs.message.types.HSOnlineMessage;
import com.ihs.message.types.HSTextMessage;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ihs.message.managers.HSMessageManager.*;

public class ChatActivity extends HSActionBarActivity implements HSMessageChangeListener{
    private String name;
    private String mid;
    private ListView listView;
    private Button button_send;
    private ImageButton multiButton;
    private ImageButton button_image;
    private ImageButton button_location;
    private ImageButton button_audio;
    private TextView audio;
    private EditText editText;
    private String status;
    private ChatEntity _chatEntity;
    private String myword;
    private String filePath;
    private MediaRecorder mediaRecorder;
    public MediaPlayer player;
    private static final String TAG = ChatActivity.class.getName();
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    List<HSBaseMessage> chatlist = new ArrayList<>();
    List<ChatEntity> Data_Entity = new ArrayList<>();
    private MsgAdapter msgAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        mid = intent.getStringExtra("mid");
        setTitle(name);
        init_view();

    }
    @Override
    protected void onDestroy(){
        HSMessageManager.getInstance().markRead(mid);
        HSMessageManager.getInstance().removeListener(this);
        super.onDestroy();
    }
    public void search(){
        List<HSBaseMessage> res = HSMessageManager.getInstance().queryMessages(mid, 0, -1).getMessages();
        for(int i = res.size()-1; i >= 0; i--){
            HSBaseMessage hsBaseMessage = res.get(i);
            status = hsBaseMessage.getStatus().valueOf(hsBaseMessage.getStatus().getValue()).toString();
            if(hsBaseMessage.getType() == HSMessageType.TEXT){
                HSTextMessage hsTextMessage = (HSTextMessage)hsBaseMessage;
                String word = hsTextMessage.getText();
                Date _date = hsBaseMessage.getTimestamp();
                String date = formatter.format(_date);
                ChatEntity chatEntity;
                if(hsBaseMessage.getFrom().equals(mid)){
                    chatEntity = new ChatEntity(word, date, "read", hsBaseMessage, false);
                }else{
                    chatEntity = new ChatEntity(word, date, status.toLowerCase(), hsBaseMessage, true);
                }
                Data_Entity.add(chatEntity);
            }else{
                String date = formatter.format(hsBaseMessage.getTimestamp());
                ChatEntity chatEntity;
                if(hsBaseMessage.getFrom().equals(mid))
                    chatEntity = new ChatEntity(hsBaseMessage.toString(), date, "read", hsBaseMessage, false);
                else
                    chatEntity = new ChatEntity(hsBaseMessage.toString(), date, status.toLowerCase(), hsBaseMessage, true);
                Data_Entity.add(chatEntity);
            }
        }
    }
    public void init_view(){
        button_send = (Button) findViewById(R.id.btn_send);
        multiButton = (ImageButton) findViewById(R.id.btn_pic);
        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.btn_multimedia);
        button_image = (ImageButton) findViewById(R.id.photo_btn);
        button_location = (ImageButton) findViewById(R.id.location);
        button_audio = (ImageButton) findViewById(R.id.iv_popup);
        audio = (TextView) findViewById(R.id.btn_rcd);
        listView = (ListView) findViewById(R.id.List_view);
        editText = (EditText) findViewById(R.id.editText_sendmessage);
        search();
        msgAdapter = new MsgAdapter(this, Data_Entity);
        listView.setAdapter(msgAdapter);
        msgAdapter.notifyDataSetChanged();
        listView.setSelection(Data_Entity.size() - 1);
        HSMessageManager.getInstance().addListener(this, new Handler());
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myword = null;
                myword = (editText.getText() + "").toString();
                if (myword.length() == 0)
                    return;
                editText.setText("");
                send();
            }
        });
        multiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(relativeLayout.getVisibility() == View.GONE)
                    relativeLayout.setVisibility(View.VISIBLE);
                else
                    relativeLayout.setVisibility(View.GONE);
            }
        });
        button_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 1);
                relativeLayout.setVisibility(View.GONE);

            }
        });
        button_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(audio.getVisibility() == View.VISIBLE){
                    audio.setVisibility(View.GONE);
                    multiButton.setVisibility(View.VISIBLE);
                    editText.hasFocus();
                }else {
                    audio.setVisibility(View.VISIBLE);
                    multiButton.setVisibility(View.GONE);
                    editText.clearFocus();
                }
            }
        });
        audio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    startRecord();
                    return true;
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    mediaRecorder.release();
                    HSAudioMessage hsAudioMessage = new HSAudioMessage(mid, filePath, 0);
                    HSMessageManager.getInstance().send(hsAudioMessage, new SendMessageCallback() {
                        @Override
                        public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {
                            HSLog.e(TAG, "success:"+success);
                        }
                    }, new Handler());
                    return false;
                }
                return false;
            }
        });

    }
    public void startRecord(){
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            String path = getSDpath();
            if(path != null){
                File dir = new File(path + "/MsgRecord");
                if(!dir.exists()){
                    dir.mkdir();
                }
                path = dir.getAbsolutePath() +"/" +formatter.format(System.currentTimeMillis()) +".3gp";
                mediaRecorder.setOutputFile(path);
                mediaRecorder.prepare();
                mediaRecorder.start();
                filePath = path;
            }
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
    public String getSDpath() {
        File sdir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);
        if(sdCardExist){
            sdir = Environment.getExternalStorageDirectory();
            return sdir.getAbsolutePath();
        }
        return null;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);


            int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();

            String path = cursor.getString(colunm_index);

            cursor.close();
            HSLog.e(TAG, "2222222"+path );
//            File file = new File(path);
            HSImageMessage hsImageMessage = new HSImageMessage(mid, path);
            HSMessageManager.getInstance().send(hsImageMessage, new SendMessageCallback() {
                @Override
                public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {
                    HSLog.e(TAG, "success: "+success);
                }
            }, new Handler());

            // String picturePath contains the path of selected Image
        }
    }
    private void play_ringtone(){
        try {
            player = MediaPlayer.create(this, R.raw.message_ringtone_sent);
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.release();
                    player = null;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void send(){
        final HSTextMessage textMessage = new HSTextMessage(mid, myword);
        play_ringtone();
        HSMessageManager.getInstance().send(textMessage, new SendMessageCallback() {
            @Override
            public void onMessageSentFinished(HSBaseMessage message, boolean success, HSError error) {
            }
        }, new Handler());
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMessageChanged(HSMessageChangeType changeType, List<HSBaseMessage> messages) {
        if(changeType == HSMessageChangeType.ADDED && !messages.isEmpty()){
            for(int i = 0; i < messages.size(); i++){
                chatlist.add(messages.get(i));
                if(messages.get(i).getType() == HSMessageType.TEXT){
                    HSTextMessage textMessage = (HSTextMessage)messages.get(i);
                    if(textMessage.getFrom().equals(mid)) {
                        Date date = new Date(System.currentTimeMillis());
                        String str = formatter.format(date);
                        _chatEntity = new ChatEntity(textMessage.getText(), str, "read", messages.get(i), false);
                    }
                    else if(textMessage.getTo().equals(mid)){
                        Date date = new Date(System.currentTimeMillis());
                        String str = formatter.format(date);
                        status = textMessage.getStatus().valueOf(textMessage.getStatus().getValue()).toString();
                        _chatEntity = new ChatEntity(textMessage.getText(), str, status.toLowerCase(), messages.get(i), true);
                    }
                }else if(messages.get(i).getType() == HSMessageType.IMAGE){
                    if(messages.get(i).getFrom().equals(mid)){
                         Date date = new Date(System.currentTimeMillis());
                        String str = formatter.format(date);
                        _chatEntity = new ChatEntity("", str, "read", messages.get(i), false);

                    }else {
                        Date date = messages.get(i).getTimestamp();
                        String str = formatter.format(date);
                        status = messages.get(i).getStatus().valueOf(messages.get(i).getStatus().getValue()).toString();
                        _chatEntity = new ChatEntity("", str, status.toLowerCase(), messages.get(i), true);
                    }
                }else if(messages.get(i).getType() == HSMessageType.AUDIO){
                    if(messages.get(i).getFrom().equals(mid)){
                        Date date = messages.get(i).getTimestamp();
                        String str = formatter.format(date);
                        _chatEntity = new ChatEntity("", str, "read", messages.get(i), false);
                    }else{
                        Date date = messages.get(i).getTimestamp();
                        String str = formatter.format(date);
                        _chatEntity = new ChatEntity("", str, "sending", messages.get(i), true);
                    }
                }
                Data_Entity.add(_chatEntity);
                msgAdapter.notifyDataSetChanged();
                listView.setSelection(Data_Entity.size() - 1);
            }
        }
        if(changeType == HSMessageChangeType.UPDATED && !messages.isEmpty()){
            for(int i = 0; i < messages.size(); i++){
                chatlist.add(messages.get(i));
                if(messages.get(i).getType() == HSMessageType.TEXT){
                    HSTextMessage textMessage = (HSTextMessage)messages.get(i);
                    if(textMessage.getTo().equals(mid)){
                        Date date = new Date(System.currentTimeMillis());
                        String str = formatter.format(date);
                        status = textMessage.getStatus().valueOf(textMessage.getStatus().getValue()).toString();
                        for(int j = Data_Entity.size()-1; j >= 0; j--){
                            if(Data_Entity.get(j).getHsBaseMessage().getType() == HSMessageType.TEXT)
                                if(Data_Entity.get(j).getText().equals(textMessage.getText()))
                                    Data_Entity.get(j).setText(str, status.toLowerCase());
                        }
                        msgAdapter.notifyDataSetChanged();
                        listView.setSelection(Data_Entity.size() - 1);

                    }
                }else if(messages.get(i).getType() == HSMessageType.IMAGE){
                    HSImageMessage hsImageMessage = (HSImageMessage)messages.get(i);
                    Date date = hsImageMessage.getTimestamp();
                    String str = formatter.format(date);
                    status = hsImageMessage.getStatus().valueOf(hsImageMessage.getStatus().getValue()).toString();
                    for(int j = Data_Entity.size()-1; j >= 0; j--){
                        if(Data_Entity.get(j).getHsBaseMessage().getType() == HSMessageType.IMAGE)
                            if(Data_Entity.get(j).getHsBaseMessage().getMsgID().equals(messages.get(i).getMsgID())){
                                Data_Entity.get(j).setText(str, status.toLowerCase());
                            }
                    }
                }
            }
        }
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