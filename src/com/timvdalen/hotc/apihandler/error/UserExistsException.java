package com.timvdalen.hotc.apihandler.error;

/**
 * Thrown when someone wants to create a user with a name that already exist
 *
 */
public class UserExistsException extends APIException{
	private static final long serialVersionUID = -7411238377333865092L;

	public UserExistsException(String message){
		super(message);
	}
	
	@Override
	protected int getCode(){
		return 1;
	}
}
