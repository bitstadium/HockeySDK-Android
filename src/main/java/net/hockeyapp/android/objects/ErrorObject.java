package net.hockeyapp.android.objects;

import java.io.Serializable;

/**
 * Error helper object
 * @author Bogdan Nistor
 *
 */
public class ErrorObject implements Serializable {
	private int code;
	private String message;
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
