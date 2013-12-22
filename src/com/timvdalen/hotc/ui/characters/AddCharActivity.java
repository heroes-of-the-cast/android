package com.timvdalen.hotc.ui.characters;

import java.io.IOException;
import java.util.ArrayList;

import com.timvdalen.hotc.R;
import com.timvdalen.hotc.apihandler.APIHandler;
import com.timvdalen.hotc.apihandler.NoNetworkException;
import com.timvdalen.hotc.apihandler.error.APIException;
import com.timvdalen.hotc.apihandler.error.CharExistsException;
import com.timvdalen.hotc.apihandler.error.LoginException;
import com.timvdalen.hotc.apihandler.error.ReservedNameException;
import com.timvdalen.hotc.data.StatModifyingProperty;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
		
		StatModifyingPropertyAdapter raceAdapter = new StatModifyingPropertyAdapter(this, R.layout.statmodifyingproperty_item, new ArrayList<StatModifyingProperty>());
		StatModifyingPropertyAdapter classAdapter = new StatModifyingPropertyAdapter(this, R.layout.statmodifyingproperty_item, new ArrayList<StatModifyingProperty>());
		
		Spinner raceSpinner = (Spinner) findViewById(R.id.spnRace);
		Spinner classSpinner = (Spinner) findViewById(R.id.spnClass);
		
		raceSpinner.setAdapter(raceAdapter);
		classSpinner.setAdapter(classAdapter);
		
		(new GetStatModifyingPropertyList(raceAdapter, true)).execute((Void) null);
		(new GetStatModifyingPropertyList(classAdapter, false)).execute((Void) null);
	}
	
	private void showProgress(boolean show){
		if(show){
			findViewById(R.id.charcreate_form).setVisibility(View.GONE);
			findViewById(R.id.charcreate_status).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.charcreate_form).setVisibility(View.VISIBLE);
			findViewById(R.id.charcreate_status).setVisibility(View.GONE);
		}
	}

	class ActionBarListener implements View.OnClickListener{
		@Override
		public void onClick(View v){
			switch(v.getId()){
			case R.id.action_done:
				showProgress(true);
				(new CreateCharTask()).execute((Void) null);
				break;
			case R.id.action_cancel:
				finish();
				break;
			}
		}
	}
	
	public class GetStatModifyingPropertyList extends AsyncTask<Void, Void, Boolean>{
		private Exception e;
		private StatModifyingPropertyAdapter adapter;
		private StatModifyingProperty[] list;
		private boolean type;
		
		public GetStatModifyingPropertyList(StatModifyingPropertyAdapter adapter, boolean type){
			this.adapter = adapter;
			this.type = type;
		}

		@Override
		protected Boolean doInBackground(Void... params){
			try{
				list = APIHandler.getpropertylist(type, AddCharActivity.this);
				return true;
			}catch(NoNetworkException e){
				//Notify user that there is no connection
				this.e = e;
			}catch(APIException e){
				// No APIExceptions possible after this point
				this.e = e;
			}catch(IOException e){
				//TODO: Figure out if there's something that can fix this
				this.e = e;
			}
			
			return false;
		}
		
		
		@Override
		protected void onPostExecute(final Boolean success){
			if(success){
				//Set adapter
				adapter.addAll(list);
				adapter.notifyDataSetChanged();
			}else{
				if(this.e != null){
					//Figure out what went wrong
					if(e instanceof NoNetworkException){
						Toast.makeText(AddCharActivity.this, getResources().getString(R.string.error_no_network), Toast.LENGTH_SHORT).show();
					}else{
						//No idea
						Toast.makeText(AddCharActivity.this, getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}
	
	public class CreateCharTask extends AsyncTask<Void, Void, Boolean>{
		private Exception e;
		
		@Override
		protected Boolean doInBackground(Void... params){
			//Get session key
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AddCharActivity.this);
			String session_key = preferences.getString("session_key", null);
			if(session_key == null){
				Toast.makeText(AddCharActivity.this, getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
			}else{
				try{
					String name = ((EditText) findViewById(R.id.edtName)).getText().toString();
					String race_alias = ((Spinner) findViewById(R.id.spnRace)).getSelectedItem().toString();
					String class_alias = ((Spinner) findViewById(R.id.spnClass)).getSelectedItem().toString();
					//Download chars
					APIHandler.charactercreate(name, race_alias, class_alias, session_key, AddCharActivity.this);
					return true;
				}catch(NoNetworkException e){
					//Notify user that there is no connection
					this.e = e;
				}catch(LoginException e){
					//Login credentials failed
					this.e = e;
				}catch(ReservedNameException e){
					//Name not valid
					this.e = e;
				}catch(CharExistsException e){
					//Name already in use
					this.e = e;
				}catch(APIException e){
					// All possible API exceptions have been handled
					this.e = e;
				}catch(IOException e){
					//TODO: Figure out if there's something that can fix this
					this.e = e;
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success){
			if(success){
				//TODO: Trigger reload of character fragment
				finish();
			}else{
				showProgress(false);
				if(this.e != null){
					//Figure out what went wrong
					if(e instanceof NoNetworkException){
						Toast.makeText(AddCharActivity.this, getResources().getString(R.string.error_no_network), Toast.LENGTH_SHORT).show();
					}else if(e instanceof LoginException){
						//Shouldn't be happening, TODO: handle wrong sessions
					}else if(e instanceof ReservedNameException){
						//Name is not valid
						((EditText) findViewById(R.id.edtName)).setError(getString(R.string.addchar_error_reserved));
						((EditText) findViewById(R.id.edtName)).requestFocus();
					}else if(e instanceof CharExistsException){
						//Name in use
						((EditText) findViewById(R.id.edtName)).setError(getString(R.string.addchar_error_exists));
						((EditText) findViewById(R.id.edtName)).requestFocus();
					}else{
						//No idea
						Toast.makeText(AddCharActivity.this, getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
					}
				}
			}
		}

		@Override
		protected void onCancelled(){
			showProgress(false);
		}
	}
}
