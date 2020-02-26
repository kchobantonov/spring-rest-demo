package com.test.restapi.error;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

/**
 * Defines the REST error response.
 * 
 * @author kchobantonov
 */
@Data
@Builder
public class ApiErrorResponse {
	/**
	 * The time that the errors were extracted
	 */
	private Date timestamp;

	/**
	 * The status code
	 */
	private Integer status;

	/**
	 * The class name of the root exception
	 */
	private String exception;

	/**
	 * The exception message
	 */
	private String message;

	/**
	 * Any validation errors
	 */
	private List<ValidationError> errors;

	/**
	 * The exception stack trace
	 */
	private String trace;

	/**
	 * The URL path when the exception was raised
	 */
	private String path;

	/**
	 * Defines validation error.
	 * 
	 * @author kchobantonov
	 */
	@Value(staticConstructor = "of")
	public static class ValidationError {
		/**
		 * The target entity for the value.
		 */
		String entity;

		/**
		 * The target property for the value.
		 */
		String property;

		/**
		 * The value that is invalid.
		 */
		Object invalidValue;

		/**
		 * The validation error message.
		 */
		String message;
	}
}
