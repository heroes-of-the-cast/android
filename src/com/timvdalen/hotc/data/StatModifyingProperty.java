package com.timvdalen.hotc.data;

public class StatModifyingProperty{
	private String alias;
	private String name;
	private String description;
	
	public StatModifyingProperty(String alias, String name, String description){
		this.alias = alias;
		this.name = name;
		this.description = description;
	}

	public String getAlias(){
		return alias;
	}

	public String getName(){
		return name;
	}

	public String getDescription(){
		return description;
	}
	
	@Override
	public String toString(){
		return getAlias();
	}
}
