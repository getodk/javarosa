package org.javarosa.communication.http;

public class UnexpectedResponseCodeException extends Exception{
	int code;
	public UnexpectedResponseCodeException(int code){
		this.code = code;
	}
	public int getCode() {
		return code;
	}
}