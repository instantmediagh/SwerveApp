package com.instantmedia.swerve;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class InboxFragment extends ListFragment {

    public String senderId;
    public String senderName;
    protected List < ParseObject > mMessages;
    protected List < ParseUser > mUsers;
    protected List < ParseConstants > messageType;
    protected ParseRelation < ParseUser > mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected Uri mMediaUri;
    protected ArrayList < String > recipientId = new ArrayList < String > ();

    @
    Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox,
            container, false);

        return rootView;
    }

    @
    Override
    public void onResume() {
        super.onResume();

        getActivity()
            .setProgressBarIndeterminateVisibility(true);

        ParseQuery < ParseObject > query = new ParseQuery < ParseObject > (ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser()
            .getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback < ParseObject > () {

            @
            Override
            public void done(List < ParseObject > messages, ParseException e) {
                getActivity()
                    .setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    //we found messages!
                    mMessages = messages;

                    String[] usernames = new String[mMessages.size()];
                    int i = 0;
                    for (ParseObject message: mMessages) {
                        usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;

                    }
                    if (getListView()
                        .getAdapter() == null) {

                        MessageAdapter adapter = new MessageAdapter(
                            getListView()
                            .getContext(),
                            mMessages);
                        setListAdapter(adapter);
                    } else {
                        //refill the adapter
                        ((MessageAdapter) getListView()
                            .getAdapter())
                            .refill(mMessages);

                    }
                }

            }
        });


    }@
    Override
         public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);
        senderName = message.getString(ParseConstants.KEY_SENDER_NAME);
        senderId = message.getString(ParseConstants.KEY_SENDER_ID);
        String displayMessage = message.getString(ParseConstants.KEY_MESSAGE);
        ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
        if (messageType.equals(ParseConstants.TYPE_IMAGE)) {
            //view the image
            Uri fileUri = Uri.parse(file.getUrl());
            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
            intent.setData(fileUri);
            startActivity(intent);
        }else if (messageType.equals(ParseConstants.TYPE_VIDEO)){
            Uri fileUri = Uri.parse(file.getUrl());
            //view the video
            Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
            intent.setDataAndType(fileUri, "video/*");
            startActivity(intent);
        }else if (messageType.equals(ParseConstants.TYPE_TEXT)){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Message from: " + senderName + ".");
            builder.setMessage(displayMessage);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }else if (messageType.equals(ParseConstants.TYPE_FRIEND_REQUEST)){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Friend Request from " + senderName + ".");
            builder.setMessage(displayMessage);
            builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //Accept request
                    //Add senders id to friends list
                    mCurrentUser = ParseUser.getCurrentUser();
                    mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
                    getActivity().setProgressBarIndeterminate(true);
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("objectId", senderId);
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> users, ParseException e) {
                            getActivity().setProgressBarIndeterminate(false);
                            if (e == null){
                                mFriendsRelation.add(users.get(0));
                                mCurrentUser.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.e("Inbox Fragment", e.getMessage());
                                        }
                                    }
                                });
                            }else {
                                Log.e("Inbox Fragment", e.getMessage());
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage(e.getMessage())
                                        .setTitle(R.string.error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                    //send accepted message
                    String acceptedMessage = mCurrentUser.getUsername() + " has accepted your friend request.";
                    ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
                    message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
                    message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
                    recipientId.add(senderId);
                    message.put(ParseConstants.KEY_RECIPIENT_IDS, recipientId);
                    message.put(ParseConstants.KEY_MESSAGE, acceptedMessage);
                    message.put(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_FRIEND_ACCEPT);
                    message.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e ==null){
                                //success
                                //Tell the user that the new friend was added.
                                Toast.makeText(getActivity(), senderName + " is now your friend.", Toast.LENGTH_LONG).show();
                            }else{
                                String errorMessage = e.getMessage();
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage("Error accepting friend request." + errorMessage);
                                builder.setTitle(R.string.error_selecting_file_title);
                                builder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            });
            builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //Refuse request
                    Toast.makeText(getActivity(),  "Request Denied.", Toast.LENGTH_LONG).show();
                    //Send denied message.
                    mCurrentUser = ParseUser.getCurrentUser();
                    String acceptedMessage = mCurrentUser.getUsername() + " has denied your friend request.";
                    ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
                    message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
                    message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
                    recipientId.add(senderId);
                    message.put(ParseConstants.KEY_RECIPIENT_IDS, recipientId);
                    message.put(ParseConstants.KEY_MESSAGE, acceptedMessage);
                    message.put(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_FRIEND_DENY);
                    message.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e ==null){
                                //success
                                //Tell the user that the new friend was added.
                                Toast.makeText(getActivity(), "Request denied.", Toast.LENGTH_LONG).show();
                            }else{
                                String errorMessage = e.getMessage();
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage("Error denying friend request." + errorMessage);
                                builder.setTitle(R.string.error_selecting_file_title);
                                builder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            });
            builder.show();
        }else  if (messageType.equals(ParseConstants.TYPE_FRIEND_ACCEPT)){
            //Display dialog showing that friend has accepted.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(senderName + " is now your friend.");
            builder.setTitle("Friend Request.");
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
            //Add new friends name to relation list
            mCurrentUser = ParseUser.getCurrentUser();
            mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
            getActivity().setProgressBarIndeterminate(true);
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", senderId);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> users, ParseException e) {
                    getActivity().setProgressBarIndeterminate(false);
                    if (e == null){
                        mFriendsRelation.add(users.get(0));
                        mCurrentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e("Inbox Fragment", e.getMessage());
                                }
                            }
                        });
                    }else {
                        Log.e("Inbox Fragment", e.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(e.getMessage())
                                .setTitle(R.string.error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        } else if (messageType.equals(ParseConstants.TYPE_FRIEND_DENY)){
            //Display dialog showing that friend has denied.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(senderName + " has denied the friend request.");
            builder.setTitle("Friend Request.");
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else if (messageType.equals(ParseConstants.TYPE_FRIEND_REMOVE)){
            //Display dialog showing that a friend has requested removal.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(senderName + " has been removed from your friends list.");
            builder.setTitle("Friend Update.");
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
            //Remove friends name from relation list
            mCurrentUser = ParseUser.getCurrentUser();
            mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
            getActivity().setProgressBarIndeterminate(true);
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", senderId);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> users, ParseException e) {
                    getActivity().setProgressBarIndeterminate(false);
                    if (e == null){
                        mFriendsRelation.remove(users.get(0));
                        mCurrentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e("Inbox Fragment", e.getMessage());
                                }
                            }
                        });
                    }else {
                        Log.e("Inbox Fragment", e.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(e.getMessage())
                                .setTitle(R.string.error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        }
        //delete the message
        List<String> ids = message.getList(ParseConstants.KEY_RECIPIENT_IDS);
        if(ids.size() == 1) {
            //last recipient, delete the message
            message.deleteInBackground();
        }else {
            //remove recipients name
            ids.remove(ParseUser.getCurrentUser().getObjectId());
            ArrayList<String> idsToRemove = new ArrayList<String>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());
            message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
            message.saveInBackground();
        }
    }}