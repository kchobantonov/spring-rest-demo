package com.test.restapi.error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.test.restapi.error.ApiErrorResponse.ValidationError;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private ErrorProperties errorProperties;
	private final MessageSourceAccessor messageSourceAccessor;

	private Map<Class<? extends Throwable>, HttpStatus> exceptionToHttpStatus = new HashMap<>();

	@Autowired
	public ApiExceptionHandler(ServerProperties serverProperties, MessageSource messageSource) {
		Assert.notNull(messageSource, "MessageSource must not be null!");
		messageSourceAccessor = new MessageSourceAccessor(messageSource);

		errorProperties = serverProperties.getError();

		exceptionToHttpStatus.put(DataIntegrityViolationException.class, HttpStatus.CONFLICT);
		exceptionToHttpStatus.put(MethodArgumentNotValidException.class, HttpStatus.BAD_REQUEST);
		exceptionToHttpStatus.put(ConstraintViolationException.class, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<ApiErrorResponse> doResolveException(HttpServletRequest request, Exception e) {

		List<ValidationError> errors = extractErrors(e);

		/*
		 * if (ex.getCause() instanceof Exception) { return doResolveException(request,
		 * response, handler, (Exception) ex.getCause()); }
		 */

		ApiErrorResponse.ApiErrorResponseBuilder errorBuilder = ApiErrorResponse.builder();
		errorBuilder.timestamp(new Date());
		MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations
				.from(e.getClass(), SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
		HttpStatus errorStatus = determineHttpStatus(e, responseStatusAnnotation);

		errorBuilder.status(errorStatus.value());
		errorBuilder.exception(errorProperties.isIncludeException() ? e.getClass().getName() : null);
		errorBuilder.message(determineMessage(e, responseStatusAnnotation));
		errorBuilder.errors(errors);
		errorBuilder.trace(isIncludeStackTrace(request, MediaType.ALL) ? toStackTrace(e) : null);
		errorBuilder.path(request.getRequestURI());

		if (errorStatus.is1xxInformational() || errorStatus.is2xxSuccessful() || errorStatus.is3xxRedirection()) {
			log.debug("Request raised an Exception", e);
		} else if (errorStatus.is4xxClientError()) {
			log.info("Request raised an Exception", e);
		} else {
			log.error("Request raised an Exception", e);
		}

		return new ResponseEntity<>(errorBuilder.build(), errorStatus);
	}

	private HttpStatus determineHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
		if (error instanceof ResponseStatusException) {
			return ((ResponseStatusException) error).getStatus();
		}
		return responseStatusAnnotation.getValue("code", HttpStatus.class)
				.orElse(exceptionToHttpStatus.containsKey(error.getClass())
						? exceptionToHttpStatus.get(error.getClass())
						: HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private String determineMessage(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
		if (error instanceof WebExchangeBindException) {
			return error.getMessage();
		}
		if (error instanceof ResponseStatusException) {
			return ((ResponseStatusException) error).getReason();
		}
		return responseStatusAnnotation.getValue("reason", String.class).orElseGet(error::getLocalizedMessage);
	}

	private String toStackTrace(Throwable error) {
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		stackTrace.flush();
		return stackTrace.toString();
	}

	/**
	 * Determine if the stacktrace attribute should be included.
	 * 
	 * @param request  the source request
	 * @param produces the media type produced (or {@code MediaType.ALL})
	 * @return if the stacktrace attribute should be included
	 */
	protected boolean isIncludeStackTrace(HttpServletRequest request, MediaType produces) {
		IncludeStacktrace include = errorProperties.getIncludeStacktrace();
		if (include == IncludeStacktrace.ALWAYS) {
			return true;
		}
		if (include == IncludeStacktrace.ON_TRACE_PARAM) {
			return getTraceParameter(request);
		}
		return false;
	}

	private List<ValidationError> extractErrors(Throwable error) {
		Errors errors = null;

		if (error instanceof Errors) {
			errors = (Errors) error;
		} else if (error instanceof MethodArgumentNotValidException) {
			errors = ((MethodArgumentNotValidException) error).getBindingResult();
		} else if (error instanceof RepositoryConstraintViolationException) {
			errors = ((RepositoryConstraintViolationException) error).getErrors();
		}

		if (errors != null) {
			return toValidationErrors(errors);
		}

		if (error instanceof ConstraintViolationException) {
			return toValidationErrors(((ConstraintViolationException) error).getConstraintViolations());
		}

		return null;
	}

	private List<ValidationError> toValidationErrors(Set<ConstraintViolation<?>> violations) {
		if (violations.isEmpty()) {
			return null;
		}

		List<ValidationError> result = new ArrayList<>();

		for (ConstraintViolation<?> violation : violations) {
			result.add(ValidationError.of(violation.getRootBeanClass().getSimpleName(),
					violation.getPropertyPath().toString(), violation.getInvalidValue(),
					toValidationMessage(violation)));
		}

		return result;
	}

	private String toValidationMessage(ConstraintViolation<?> violation) {
		if (StringUtils.isEmpty(violation.getMessageTemplate()) || violation.getLeafBean() == null) {
			return violation.getMessage();
		}

		return messageSourceAccessor.getMessage(violation.getMessageTemplate(),
				new Object[] { violation.getLeafBean().getClass().getSimpleName(),
						violation.getPropertyPath().toString(), violation.getInvalidValue() },
				violation.getMessage(), LocaleContextHolder.getLocale());
	}

	private List<ValidationError> toValidationErrors(Errors errors) {
		List<ValidationError> result = new ArrayList<>();

		for (FieldError fieldError : errors.getFieldErrors()) {
			result.add(ValidationError.of(fieldError.getObjectName(), fieldError.getField(),
					fieldError.getRejectedValue(), messageSourceAccessor.getMessage(fieldError)));
		}

		return result;
	}

	protected boolean getTraceParameter(HttpServletRequest request) {
		String parameter = request.getParameter("trace");
		if (parameter == null) {
			return false;
		}
		return !"false".equals(parameter.toLowerCase());
	}
}
