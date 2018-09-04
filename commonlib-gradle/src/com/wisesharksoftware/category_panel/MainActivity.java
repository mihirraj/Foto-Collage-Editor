package com.wisesharksoftware.category_panel;

import com.smsbackupandroid.lib.R;
import com.wisesharksoftware.category_panel.CategoryPanel;
import com.wisesharksoftware.category_panel.CategoryPanel.OnItemListener;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	TextView tv1; 
	CategoryPanel panel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv1 = (TextView)findViewById(R.id.tv1);
		panel = (CategoryPanel)findViewById(R.id.panel1);
		panel.setOnItemListener(new OnItemListener() {
			@Override
			public boolean onItemSelected(String id) {
				tv1.setText(id);
				//true - btn state should be changed, false otherwise
				return true;
			}
		});
	}

}
