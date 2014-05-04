package com.instantmedia.swerve;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

public class ViewImageActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_view_image);
	    // show the up button
	    setupActionBar();
	
	    ImageView imageView = (ImageView)findViewById(R.id.imageView);
	    
	    Uri imageUri = getIntent().getData();
	    
	    Picasso.with(this).load(imageUri.toString()).into(imageView);
	    
	    Timer timer = new Timer();
	    timer.schedule(new TimerTask() {
			
		@Override
			public void run() {
				finish();
			}
		}, 10*1000);


	    
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			
			NavUtils.navigateUpFromSameTask(this);
			return true;
			}
		return super.onOptionsItemSelected(item);
	}
}


 