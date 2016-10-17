package com.main.colorlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {
	private final static String TAG = AboutActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		getActionBar().setTitle("ColorLight");

		Intent intent = getIntent();
		String value = intent.getStringExtra("testIntent");

		String help = "ColorLight, powered by PKUMakerspace 2016";

		TextView tv = (TextView) findViewById(R.id.about_text_help);
		tv.setText(help);
	}

	public void onClick(View v) {
		// TODO
	}

}