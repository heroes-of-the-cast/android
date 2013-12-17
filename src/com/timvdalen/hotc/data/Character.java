package com.timvdalen.hotc.data;

public class Character{
	private String name;
	private String username; //TODO: Import with reference to user
	private String race; //TODO: Import with reference to race
	private String cclass; //TODO: Import with reference to class
	
	public Character(String name, String username, String race, String cclass){
		this.name = name;
		this.username = username;
		this.race = race;
		this.cclass = cclass;
	}

	public String getName(){
		return name;
	}

	public String getUsername(){
		return username;
	}

	public String getRace(){
		return race;
	}

	public String getCclass(){
		return cclass;
	}
}
