package com.test.restapi.data.jpa.employee;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Simple enum used in an ObjectTypeConverter
 *
 * @see Employee#gender
 */

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, String> {

	@Override
	public String convertToDatabaseColumn(Gender gender) {
		switch (gender) {
		case Male:
			return "M";

		case Female:
			return "F";

		default:
			throw new IllegalArgumentException("Invalid gender: " + gender);
		}
	}

	@Override
	public Gender convertToEntityAttribute(String gender) {
		switch (gender) {
		case "M":
			return Gender.Male;

		case "F":
			return Gender.Female;

		default:
			throw new IllegalArgumentException("Invalid gender code: " + gender);
		}
	}
}