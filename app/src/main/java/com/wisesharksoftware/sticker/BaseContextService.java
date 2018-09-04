package com.wisesharksoftware.sticker;

import android.content.Context;

import com.aviary.android.feather.headless.utils.IDisposable;
import com.aviary.android.feather.library.log.LoggerFactory;

public abstract class BaseContextService implements IDisposable {
	private Context mContext;
	protected LoggerFactory.Logger logger;

	protected BaseContextService(Context context) {
		this.mContext = context;
		this.logger = LoggerFactory.getLogger(super.getClass().getSimpleName(),
				LoggerFactory.LoggerType.ConsoleLoggerType);
	}

	public Context getBaseContext() {
		return this.mContext;
	}

	public void internalDispose() {
		this.logger.info(new Object[] { "internalDispose" });
		dispose();
		this.mContext = null;
		this.logger = null;
	}

	public abstract void dispose();
}