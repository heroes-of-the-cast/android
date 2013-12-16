package com.timvdalen.hotc.apihandler.returntypes;

public class APIReturnError{
	public APIReturnErrorBody error;
	
	public class APIReturnErrorBody{
		public int code;
		public String message;
	}
}
