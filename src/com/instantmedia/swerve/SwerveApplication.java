package com.instantmedia.swerve;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseTwitterUtils;
import com.parse.PushService;
import com.parse.ParseFacebookUtils;

public class SwerveApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "yKRswsrDimrUE3pr7c1JSIbn31DHnTK6t30r0jFc", "wijuLwvwZTdn4BoxyGMW7VDWJca7b5KSOXVhl4Eu");
		PushService.setDefaultPushCallback(this, MainActivity.class);
		//ParseTwitterUtils.initialize("xsgAmEzu8KTHRPrc33MZfw", "QNBKQgYCAZEVQasMnLBcIIF2iuqG6ONTYgW1533Q");
       // ParseFacebookUtils.initialize("1489853804571687");
		
		  
		  }
	
	
}

