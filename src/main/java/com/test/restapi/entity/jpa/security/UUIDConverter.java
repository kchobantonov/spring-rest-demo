package com.test.restapi.entity.jpa.security;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

public class UUIDConverter implements Converter {
	private boolean isUUIDasByteArray = true;

	@Override
	public Object convertObjectValueToDataValue(Object objectValue, Session session) {
		if (isUUIDasByteArray) {
			UUID uuid = (UUID) objectValue;
			if (uuid == null)
				return null;
			byte[] buffer = new byte[16];
			ByteBuffer bb = ByteBuffer.wrap(buffer);
			bb.putLong(uuid.getMostSignificantBits());
			bb.putLong(uuid.getLeastSignificantBits());
			return buffer;
		}

		return objectValue;
	}

	@Override
	public Object convertDataValueToObjectValue(Object dataValue, Session session) {
		if (dataValue instanceof String) {
			// comes from the generator
			return UUID.fromString((String) dataValue);
		}
		if (isUUIDasByteArray) {
			byte[] bytes = (byte[]) dataValue;
			if (bytes == null)
				return null;
			ByteBuffer bb = ByteBuffer.wrap(bytes);
			long high = bb.getLong();
			long low = bb.getLong();
			return new UUID(high, low);
		}

		return (UUID) dataValue;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {
		final DatabaseField field;
		if (mapping instanceof DirectCollectionMapping) {
			// handle @ElementCollection...
			field = ((DirectCollectionMapping) mapping).getDirectField();
		} else {
			field = mapping.getField();
		}

		if (session != null && session.getLogin() != null && session.getLogin().getPlatform() != null) {
			String platform = session.getLogin().getPlatform().getClass().getSimpleName();

			if (platform.equals("PostgreSQLPlatform")) {
				field.setSqlType(java.sql.Types.OTHER);
				field.setTypeName("java.util.UUID");
				field.setColumnDefinition("UUID");
				isUUIDasByteArray = false;
			} else if (platform.equals("H2Platform")) {
				field.setColumnDefinition("UUID");
				isUUIDasByteArray = false;
			} else if (platform.equals("OraclePlatform")) {
				field.setColumnDefinition("RAW(16)");
			} else if (platform.equals("MySQLPlatform")) {
				field.setColumnDefinition("BINARY(16)");
			} else if (platform.equals("SQLServerPlatform")) {
				field.setColumnDefinition("UNIQUEIDENTIFIER");
			}
		}

	}
}
