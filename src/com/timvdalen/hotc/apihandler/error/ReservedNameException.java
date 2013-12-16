package com.timvdalen.hotc.apihandler.error;

/**
 * Thrown when someone wants to create something with a reserved name
 *
 */
public class ReservedNameException extends APIException{
	private static final long serialVersionUID = -4281948366713787146L;

	public ReservedNameException(String message){
		super(message);
	}
	
	@Override
	protected int getCode(){
		return 11;
	}
}
