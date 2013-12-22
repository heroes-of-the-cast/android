package com.timvdalen.hotc.ui.characters;

import com.timvdalen.hotc.R;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

public class AddCharActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addchar);

		ActionBarListener listener = new ActionBarListener();

		View actionBarButtons = getLayoutInflater().inflate(R.layout.actionbar_addchar, new LinearLayout(this), false);
		View cancelActionView = actionBarButtons.findViewById(R.id.action_cancel);
		cancelActionView.setOnClickListener(listener);
		View doneActionView = actionBarButtons.findViewById(R.id.action_done);
		doneActionView.setOnClickListener(listener);
		
		ActionBar actionbar = getActionBar();
		actionbar.setCustomView(actionBarButtons);
		actionbar.setDisplayShowHomeEnabled(false);
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setDisplayShowCustomEnabled(true);
	}

	class ActionBarListener implements View.OnClickListener{
		@Override
		public void onClick(View v){
			// TODO: Cancel and done buttons
		}
	}
}
