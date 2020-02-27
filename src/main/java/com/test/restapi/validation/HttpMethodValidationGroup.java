package com.test.restapi.validation;

public interface HttpMethodValidationGroup {
	interface GET extends HttpMethodValidationGroup {
	};

	interface HEAD extends HttpMethodValidationGroup {
	};

	interface POST extends HttpMethodValidationGroup {
	};

	interface PUT extends HttpMethodValidationGroup {
	};

	interface PATCH extends HttpMethodValidationGroup {
	};

	interface DELETE extends HttpMethodValidationGroup {
	};

	interface OPTIONS extends HttpMethodValidationGroup {
	};

	interface TRACE extends HttpMethodValidationGroup {
	};

}
