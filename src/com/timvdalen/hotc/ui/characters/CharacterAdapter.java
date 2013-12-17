package com.timvdalen.hotc.ui.characters;

import java.util.ArrayList;

import com.timvdalen.hotc.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CharacterAdapter extends ArrayAdapter<com.timvdalen.hotc.data.Character>{
	private Context context;
	private int layoutResource;
	private ArrayList<com.timvdalen.hotc.data.Character> characters;

	public CharacterAdapter(Context context, int resource, ArrayList<com.timvdalen.hotc.data.Character> characters){
		super(context, resource, characters);

		this.context = context;
		this.layoutResource = resource;
		this.characters = characters;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View row = convertView;
		final CharacterHolder holder;

		if(row == null){
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResource, parent, false);

			holder = new CharacterHolder();
			holder.imgChar = (ImageView) row.findViewById(R.id.imgChar);
			holder.txtName = (TextView) row.findViewById(R.id.txtName);
			holder.txtDescription = (TextView) row.findViewById(R.id.txtDescription);
			row.setTag(holder);
		}else{
			holder = (CharacterHolder) row.getTag();
		}

		com.timvdalen.hotc.data.Character charac = characters.get(position);
		holder.imgChar.setOnClickListener(new RowClickListener());
		holder.txtName.setText(charac.getName());
		holder.txtDescription.setText(charac.getRace() + " " + charac.getCclass());

		return row;
	}

	private static class CharacterHolder{
		ImageView imgChar;
		TextView txtName;
		TextView txtDescription;
	}
	
	private class RowClickListener implements View.OnClickListener{
		@Override
		public void onClick(View v){
			((View)v.getParent()).setSelected(true);
		}
		
	}
}
