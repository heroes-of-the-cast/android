package com.timvdalen.hotc.apihandler.error;

/**
 * Thrown when someone wants to create a character with a name that already exists for that user
 *
 */
public class CharExistsException extends APIException{
	private static final long serialVersionUID = 8643287201912054081L;

	public CharExistsException(String message){
		super(message);
	}
	
	@Override
	protected int getCode(){
		return 2;
	}

}
