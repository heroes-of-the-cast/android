package com.timvdalen.hotc.ui.characters;

import java.util.ArrayList;

import com.timvdalen.hotc.R;
import com.timvdalen.hotc.data.StatModifyingProperty;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StatModifyingPropertyAdapter extends ArrayAdapter<StatModifyingProperty>{
	private Context context;
	private int layoutResource;
	private ArrayList<StatModifyingProperty> properties;

	public StatModifyingPropertyAdapter(Context context, int resource, ArrayList<StatModifyingProperty> properties){
		super(context, resource, properties);

		this.context = context;
		this.layoutResource = resource;
		this.properties = properties;
	}
	
	@Override
	public View getDropDownView(int position, View convertView,	ViewGroup parent){
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View v = getCustomView(position, convertView, parent);
		v.setPadding(0, 0, 0, 0);
		return v;
	}
	
	private View getCustomView(int position, View convertView, ViewGroup parent){
		View row = convertView;
		final PropertyHolder holder;

		if(row == null){
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResource, parent, false);

			holder = new PropertyHolder();
			holder.txtName = (TextView) row.findViewById(R.id.txtName);
			holder.txtDescription = (TextView) row.findViewById(R.id.txtDescription);
			row.setTag(holder);
		}else{
			holder = (PropertyHolder) row.getTag();
		}

		StatModifyingProperty prop = properties.get(position);
		holder.txtName.setText(prop.getName());
		holder.txtDescription.setText(prop.getDescription());

		return row;	
	}

	private static class PropertyHolder{
		TextView txtName;
		TextView txtDescription;
	}
}
