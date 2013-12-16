package com.timvdalen.hotc.apihandler.error;

/**
 * Thrown when login credentials aren't correct
 *
 */
public class LoginException extends APIException{
	private static final long serialVersionUID = -1043064633898754570L;

	public LoginException(String message){
		super(message);
	}
	
	@Override
	protected int getCode(){
		return 13;
	}

}
