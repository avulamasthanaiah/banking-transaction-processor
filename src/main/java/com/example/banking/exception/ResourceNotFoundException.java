package com.example.banking.exception;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1091716106445153726L;

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
