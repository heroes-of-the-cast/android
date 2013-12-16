package com.timvdalen.hotc.apihandler.error;

/**
 * Error returned by the API
 *
 */
public abstract class APIException extends Exception{
	private static final long serialVersionUID = -40042524863901415L;
	
	public APIException(String message){
		super(message);
	}

	/**
	 * Error code
	 * @return error code associated with this error
	 */
	protected abstract int getCode();
}
