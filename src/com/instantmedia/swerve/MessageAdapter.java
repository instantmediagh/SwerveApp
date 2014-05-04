package com.instantmedia.swerve;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<ParseObject>{

	protected Context mContext;
	protected List<ParseObject> mMessages;
	
	
	public MessageAdapter(Context context, List<ParseObject> messages) {
		super(context, R.layout.message_item, messages);
		mContext = context;
		mMessages = messages;

	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView)convertView.findViewById(R.id.messageIcon);
            holder.nameLabel = (TextView)convertView.findViewById(R.id.senderLabel);
            holder.sentTime = (TextView)convertView.findViewById(R.id.sentTime);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        ParseObject message =mMessages.get(position);
        if (message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
            holder.iconImageView.setImageResource(R.drawable.ic_action_picture);
            holder.sentTime.setText(message.getString(ParseConstants.KEY_CREATED_AT));
        }else if (message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_VIDEO)){
            holder.iconImageView.setImageResource(R.drawable.ic_action_play_over_video);
            holder.sentTime.setText(message.getString(ParseConstants.KEY_CREATED_AT));
        }else {
            holder.iconImageView.setImageResource(R.drawable.ic_action_chat);
            holder.sentTime.setText(message.getString(ParseConstants.KEY_CREATED_AT));
        }if (message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_FRIEND_REQUEST)) {
            holder.iconImageView.setImageResource(R.drawable.ic_action_add_person);
            holder.sentTime.setText(message.getString(ParseConstants.KEY_CREATED_AT));
        }else if (message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_FRIEND_ACCEPT)) {
            holder.iconImageView.setImageResource(R.drawable.ic_action_add_person);
            holder.sentTime.setText(message.getString(ParseConstants.KEY_CREATED_AT));
        }else if (message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_FRIEND_DENY)) {
            holder.iconImageView.setImageResource(R.drawable.ic_action_add_person);
            holder.sentTime.setText(message.getString(ParseConstants.KEY_CREATED_AT));
        }else if (message.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_FRIEND_REMOVE)) {
            holder.iconImageView.setImageResource(R.drawable.ic_action_add_person);
            holder.sentTime.setText(message.getString(ParseConstants.KEY_CREATED_AT));
        }




        holder.nameLabel.setText(message.getString(ParseConstants.KEY_SENDER_NAME));
        holder.sentTime.setText(message.getString(ParseConstants.KEY_CREATED_AT));
        

        return convertView;
    }
	
	public void refill(List<ParseObject> messages) {
		mMessages.clear();
		mMessages.addAll(messages);
		notifyDataSetChanged();
	}
	
	private static class ViewHolder {
		TextView sentTime;
		ImageView iconImageView;
		TextView nameLabel;
	}
}