package com.timvdalen.hotc.ui.adventures;

import com.timvdalen.hotc.R;
import com.timvdalen.hotc.ui.MainFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AdventuresFragment extends MainFragment{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_adventures, container, false);
	}
}
