package com.instantmedia.swerve;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class EditFriendsActivity extends ListActivity {
	
	public static final String TAG = EditFriendsActivity.class.getSimpleName();

	
	protected List<ParseUser> mUsers;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;
	
	protected ArrayList<String> userList = new ArrayList<String>();
    protected ArrayList<String> userIdList = new ArrayList<String>();
    protected ArrayList<String> recipientId = new ArrayList<String>();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_edit_friends);
		// Show the Up button in the action bar.
		setupActionBar();
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}
	
	@Override
	protected void onResume() {
			super.onResume();
			
			mCurrentUser = ParseUser.getCurrentUser();
			mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
			
			setProgressBarIndeterminateVisibility(true);
			
			ParseQuery<ParseUser> query = ParseUser.getQuery();
			query.orderByAscending(ParseConstants.KEY_USERNAME);
			query.setLimit(1000);
			query.findInBackground(new FindCallback<ParseUser>() {
				
				@Override
				public void done(List<ParseUser> users, ParseException e) {
					setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						// Success
						mUsers = users;
						String[] usernames = new String[mUsers.size()];
						int i = 0;
						for (ParseUser user : mUsers) {
							usernames[i] = user.getUsername();
							userList.add(user.getUsername());
	                        userIdList.add(user.getObjectId());
	                        i++;
							
						}
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(
								EditFriendsActivity.this, 
								android.R.layout.simple_list_item_checked, 
								usernames);
						setListAdapter(adapter);
					
						addFriendCheckmarks();
					}
					
					else {
						Log.e(TAG, e.getMessage());
						AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
						builder.setMessage(e.getMessage())
							.setTitle(R.string.error_title)
							.setPositiveButton(android.R.string.ok, null);
						AlertDialog dialog = builder.create();
						dialog.show();
					}
				}
			});
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        //Create a variable to store the position of the selected user is the list.
        final int friendPosition = position;
        //Create a variable to store the selected users name.
        final String friendName = userList.get(position);
        final String friendId = userIdList.get(position);
        if (getListView().isItemChecked(position)) {
        	
        	//If the selected user is not already a friend.
            //Create a dialog to ask if the User is sure they want to send a friend request.
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.dialog_add_friend_title);
            //Had a problem using a string resource on the next line. Just kept giving me the string Id instead of the text.
            //alert.setMessage(R.string.dialog_add_friend_message + friendName + ".");
            alert.setMessage("Enter message below to send with your friend request to " + friendName + ".");
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Send a request to the friend.
                String requestMessage = input.getText().toString();
                ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
                message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
                message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());  
                recipientId.add(friendId);
                message.put(ParseConstants.KEY_RECIPIENT_IDS, recipientId);
                message.put(ParseConstants.KEY_MESSAGE, requestMessage);
                message.put(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_FRIEND_REQUEST);
                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e ==null){
                            //success
                            Toast.makeText(EditFriendsActivity.this,  R.string.success_friend_request_sent, Toast.LENGTH_LONG).show();
                        }else{
                            String errorMessage = e.getMessage();
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                            builder.setMessage(R.string.dialog_error_sending_friend_request + errorMessage);
                            builder.setTitle(R.string.error_selecting_file_title);
                            builder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
              }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Friend request cancelled. 

              }
            });
            alert.show();   
            //Set friend List Item back to unchecked.
            getListView().setItemChecked(friendPosition, false);
        }else {//If the selected user is a friend already.
            //Create a dialog to ask if the User is sure they want to delete the friend.
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.dialog_delete_friend_title);
            //Had a problem using a string resource on the next line. Just kept giving me the string Id instead of the text.
            //alert.setMessage(R.string.dialog_delete_friend_message + friendName + ".");
            alert.setMessage(R.string.dialog_delete_friend_message + friendName + ".");
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Remove friend from list.
                mFriendsRelation.remove(mUsers.get(friendPosition));
                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e("EditFriendsActivity", e.getMessage());
                        }               
                    }   
                }); 
                // Send message to friends application requesting deletion from their list.
                ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
                message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
                message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());  
                recipientId.add(friendId);
                message.put(ParseConstants.KEY_RECIPIENT_IDS, recipientId);
                message.put(ParseConstants.KEY_MESSAGE, "");
                message.put(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_FRIEND_REMOVE);
                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e ==null){
                            //success
                            Toast.makeText(EditFriendsActivity.this, friendName + " removed.", Toast.LENGTH_LONG).show();
                        }else{
                            String errorMessage = e.getMessage();
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                            builder.setMessage("Error removing friend." + errorMessage);
                            builder.setTitle(R.string.error_selecting_file_title);
                            builder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
              }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Deletion cancelled. Set friend List Item back to checked.
                getListView().setItemChecked(friendPosition, true); 
              }
            });
            alert.show();
        }
        //Update the User
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                }               
            }   
        });
    }
	private void addFriendCheckmarks() {
		mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				if (e == null) {
					// list returned - look for a match
					for (int i = 0; i < mUsers.size(); i++) {
						ParseUser user = mUsers.get(i);
						
						for (ParseUser friend : friends) {
							if (friend.getObjectId().equals(user.getObjectId())){
								getListView().setItemChecked(i, true);
							}
						}
					}
				}
				else {
					Log.e(TAG, e.getMessage());
				}
			}
		});
		
	}

}
