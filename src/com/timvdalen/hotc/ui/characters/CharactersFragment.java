package com.timvdalen.hotc.ui.characters;

import java.util.ArrayList;

import com.timvdalen.hotc.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class CharactersFragment extends Fragment{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		// Inflate the layout for this fragment
		View fragment = inflater.inflate(R.layout.fragment_characters, container, false);
		
		ArrayList<com.timvdalen.hotc.data.Character> chars = new ArrayList<com.timvdalen.hotc.data.Character>();
		
		ListView listCharacters = (ListView) fragment.findViewById(R.id.listCharacters);
		CharacterAdapter adapter = new CharacterAdapter(getActivity(), R.layout.character_item, chars);
		listCharacters.setAdapter(adapter);
		
		return fragment;
	}
}
