package com.timvdalen.hotc.apihandler.error;

/**
 * Thrown when someone tries to query a user that doesn't exist
 *
 */
public class UserNotFoundException extends APIException{
	private static final long serialVersionUID = -3999461028411546258L;

	public UserNotFoundException(String message){
		super(message);
	}
	
	@Override
	protected int getCode(){
		return 12;
	}
}
