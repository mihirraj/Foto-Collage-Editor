package com.smsbackupandroid.lib;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.flurry.android.FlurryAgent;

public class ExceptionHandler {
	
	public ExceptionHandler(Throwable e, String errorId) {
	    //ErrorReporter.getInstance().handleException(e);

	    try {
			if (e == null) {
				FlurryAgent.onEvent(errorId);
			} else {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
	    		FlurryAgent.onError(errorId, "Message: " + e.getMessage() + ". Stacktrace: " + sw.toString(), e);
			}
	    } catch (Exception exc) {
	    	exc.printStackTrace();
		    //ErrorReporter.getInstance().handleException(exc);
	    }
	}

}
