package com.example.banking.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail notFound(ResourceNotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler({ IllegalArgumentException.class, MethodArgumentNotValidException.class })
	ProblemDetail badRequest(Exception ex) {
		String detail = ex instanceof MethodArgumentNotValidException validation ? validation.getBindingResult()
				.getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; ")) : ex.getMessage();
		return problem(HttpStatus.BAD_REQUEST, detail);
	}

	@ExceptionHandler(ConflictException.class)
	ProblemDetail conflict(ConflictException ex) {
		return problem(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail integrityConflict(DataIntegrityViolationException ex) {
		return problem(HttpStatus.CONFLICT, "The request conflicts with existing data");
	}

	private ProblemDetail problem(HttpStatus status, String detail) {
		ProblemDetail result = ProblemDetail.forStatusAndDetail(status,
				detail == null ? status.getReasonPhrase() : detail);
		result.setType(URI.create("about:blank"));
		return result;
	}
}
