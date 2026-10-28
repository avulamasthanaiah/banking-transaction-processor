package com.example.banking.exception;

public class ConflictException extends RuntimeException {

	private static final long serialVersionUID = 2920463803251317767L;

	public ConflictException(String message) {
		super(message);
	}
}
