package com.timvdalen.hotc.ui.characters;

import java.io.IOException;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.timvdalen.hotc.R;
import com.timvdalen.hotc.apihandler.APIHandler;
import com.timvdalen.hotc.apihandler.NoNetworkException;
import com.timvdalen.hotc.apihandler.error.APIException;
import com.timvdalen.hotc.ui.MainFragment;

public class CharactersFragment extends MainFragment{
	View fragment;
	private CharacterAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		// Inflate the layout for this fragment
		 fragment = inflater.inflate(R.layout.fragment_characters, container, false);
		
		ArrayList<com.timvdalen.hotc.data.Character> chars = new ArrayList<com.timvdalen.hotc.data.Character>();
		
		ListView listCharacters = (ListView) fragment.findViewById(R.id.listCharacters);
		adapter = new CharacterAdapter(getActivity(), R.layout.character_item, chars);
		listCharacters.setAdapter(adapter);
		
		listCharacters.setEmptyView(fragment.findViewById(R.id.empty));
		
		refresh();
		
		return fragment;
	}
	
	@Override
	public void refresh(){
		hideProgress(false);
		adapter.clear();
		adapter.notifyDataSetChanged();
		(new GetCharsTask()).execute((Void) null);
	}
	
	private void hideProgress(boolean hide){
		if(hide){
			fragment.findViewById(R.id.prgsCharload).setVisibility(View.GONE);
			fragment.findViewById(R.id.txtNoChars).setVisibility(View.VISIBLE);
		}else{
			fragment.findViewById(R.id.prgsCharload).setVisibility(View.VISIBLE);
			fragment.findViewById(R.id.txtNoChars).setVisibility(View.GONE);
		}
	}
	
	public class GetCharsTask extends AsyncTask<Void, Void, Boolean>{
		private Exception e;
		private ArrayList<com.timvdalen.hotc.data.Character> chars;
		
		@Override
		protected Boolean doInBackground(Void... params){
			//Get session key
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
			String session_key = preferences.getString("session_key", null);
			if(session_key == null){
				Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
			}else{
				try{
					//Download chars
					chars = APIHandler.usercharacters(session_key, getActivity());
					return true;
				}catch(NoNetworkException e){
					//Notify user that there is no connection
					this.e = e;
				}catch(APIException e){
					// /user/:name/character/list can't throw errors according to the doc
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
				adapter.addAll(chars);
				adapter.notifyDataSetChanged();
			}else{
				if(this.e != null){
					//Figure out what went wrong
					if(e instanceof NoNetworkException){
						Toast.makeText(getActivity(), getResources().getString(R.string.error_no_network), Toast.LENGTH_SHORT).show();
					}else{
						//No idea
						Toast.makeText(getActivity(), getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
					}
				}
			}
			hideProgress(true);
		}

		@Override
		protected void onCancelled(){
			hideProgress(true);
		}
	}
}
