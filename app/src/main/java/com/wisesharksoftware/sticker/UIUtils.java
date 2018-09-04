package com.wisesharksoftware.sticker;

public class UIUtils {
	
	public static final int HIGHLIGHT_MODE_PRESSED = 2;
	public static final int HIGHLIGHT_MODE_CHECKED = 4;
	public static final int HIGHLIGHT_MODE_SELECTED = 8;
	
	public static final int GLOW_MODE_PRESSED = 2;
	public static final int GLOW_MODE_CHECKED = 4;
	public static final int GLOW_MODE_SELECTED = 8;	

	public static boolean checkBits( int status, int checkBit ) {
		return ( status & checkBit ) == checkBit;
	}
}