package com.ihs.demo.message;

import android.content.Context;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;
import com.ihs.message.R;
import com.ihs.message.types.HSAudioMessage;
import com.ihs.message.types.HSImageMessage;
import com.ihs.message.types.HSMessageType;

import java.io.File;
import java.util.List;

public class MsgAdapter extends BaseAdapter {

    //ListView视图的内容由IMsgViewType决定
    public static interface IMsgViewType {
        //对方发来的信息
        int IMVT_COM_MSG = 0;
        //自己发出的信息
        int IMVT_TO_MSG = 1;
    }

    private static final String TAG = MsgAdapter.class.getSimpleName();
    private List<ChatEntity> data;
    private Context context;
    public MediaPlayer Player;
    private LayoutInflater mInflater;

    public MsgAdapter(Context context, List<ChatEntity> data) {
        this.context = context;
        this.data = data;
        mInflater = LayoutInflater.from(context);
    }

    //获取ListView的项个数
    public int getCount() {
        return data.size();
    }

    //获取项
    public Object getItem(int position) {
        return data.get(position);
    }

    //获取项的ID
    public long getItemId(int position) {
        return position;
    }

    //获取项的类型
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        ChatEntity entity = data.get(position);

        if (entity.get_Issend()) {
            return IMsgViewType.IMVT_COM_MSG;
        } else {
            return IMsgViewType.IMVT_TO_MSG;
        }

    }

    //获取项的类型数
    public int getViewTypeCount() {
        // TODO Auto-generated method stub
        return 2;
    }

    //获取View
    public View getView(int position, View convertView, ViewGroup parent) {

        final ChatEntity entity = data.get(position);
        boolean isComMsg = entity.get_Issend();

        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (isComMsg) {
                convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, null);
                viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.send_time);
                viewHolder.tvContent = (TextView) convertView.findViewById(R.id.send_text);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.send_image);
            } else {
                HSLog.e(TAG, "11111111111");
                convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, null);
                viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.receive_time);
                viewHolder.tvContent = (TextView) convertView.findViewById(R.id.receive_text);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.receive_image);
            }

            viewHolder.isComMsg = isComMsg;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(entity.getHsBaseMessage().getType() == HSMessageType.IMAGE) {
            HSImageMessage hsImageMessage = (HSImageMessage)entity.getHsBaseMessage();
            HSLog.e(TAG, hsImageMessage.toString());
            try {

                hsImageMessage.download();
                File file = new File(hsImageMessage.getNormalImageFilePath());
                final Uri uri = Uri.fromFile(file);
                viewHolder.imageView.setImageURI(uri);

//                viewHolder.imageView.setImageBitmap(bitmap);
                viewHolder.tvContent.setVisibility(View.GONE);
                viewHolder.imageView.setVisibility(View.VISIBLE);
                viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(context, ImageActivity.class);
                        intent.putExtra("Image", uri.toString());
                        context.startActivity(intent);

                    }
                });
                HSLog.e(TAG, hsImageMessage.getThumbnailFilePath());
            }catch (OutOfMemoryError e){
                e.printStackTrace();
                HSLog.e(TAG, "137578325465346754126585642");
            }catch (Exception e){
                e.printStackTrace();
            }
//            viewHolder.tvContent.setText(hsImageMessage.toString());
            viewHolder.imageView.setVisibility(View.VISIBLE);
            viewHolder.tvContent.setVisibility(View.GONE);
        }
        else if(entity.getHsBaseMessage().getType() == HSMessageType.TEXT){
            viewHolder.tvContent.setText(entity.getText());
            viewHolder.tvContent.setVisibility(View.VISIBLE);
            viewHolder.imageView.setVisibility(View.GONE);
        }else if(entity.getHsBaseMessage().getType() == HSMessageType.AUDIO){
            viewHolder.imageView.setImageResource(R.drawable.chat_multimedia_audio_play_3);
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HSAudioMessage hsAudioMessage = (HSAudioMessage)entity.getHsBaseMessage();
                    if(Player != null && Player.isPlaying()){
                        Player.release();
                        Player = null;
                    }else{
                        hsAudioMessage.download();
                        Uri uri = Uri.parse(hsAudioMessage.getAudioFilePath());
                        Player = MediaPlayer.create(context, uri);
                        Player.start();
                        Player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Player.release();
                            Player = null;

                        }
                    });
                    }


                }
            });
            viewHolder.imageView.setVisibility(View.VISIBLE);
            viewHolder.tvContent.setVisibility(View.GONE);
        }
        viewHolder.tvSendTime.setText(entity.getDate() + " " + entity.getStatus());

        return convertView;
    }

    //通过ViewHolder显示项的内容
    static class ViewHolder {
        public TextView tvSendTime;
        public TextView tvContent;
        public boolean isComMsg = true;
        public ImageView imageView;
    }
}
