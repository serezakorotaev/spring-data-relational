/*
 * Copyright 2019-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.relational.core.dialect;

import static java.util.Arrays.asList;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.relational.core.sql.SqlIdentifier;

/**
 * An SQL dialect for Oracle.
 *
 * @author Jens Schauder
 * @author Mikahil Polivakha
 * @since 2.1
 */
public class OracleDialect extends AnsiDialect {

	/**
	 * Singleton instance.
	 *
	 * @deprecated use either the {@code org.springframework.data.r2dbc.dialect.OracleDialect} or
	 * 						 {@code org.springframework.data.jdbc.core.dialect.JdbcOracleDialect}.
	 */
	@Deprecated(forRemoval = true)
	public static final OracleDialect INSTANCE = new OracleDialect();

	private static final IdGeneration ID_GENERATION = new IdGeneration() {

		@Override
		public boolean driverRequiresKeyColumnNames() {
			return true;
		}

		@Override
		public String getKeyColumnName(SqlIdentifier id) {
			return id.toSql(INSTANCE.getIdentifierProcessing());
		}

		@Override
		public String createSequenceQuery(@NotNull SqlIdentifier sequenceName) {
			return "SELECT %s.nextval FROM DUAL".formatted(sequenceName.toSql(INSTANCE.getIdentifierProcessing()));
		}
	};

	protected OracleDialect() {}

	@Override
	public IdGeneration getIdGeneration() {
		return ID_GENERATION;
	}

	@Override
	public Collection<Object> getConverters() {
		return asList(TimestampAtUtcToOffsetDateTimeConverter.INSTANCE, NumberToBooleanConverter.INSTANCE,
				BooleanToIntegerConverter.INSTANCE);
	}

	@WritingConverter
	enum BooleanToIntegerConverter implements Converter<Boolean, Integer> {
		INSTANCE;

		@Override
		public Integer convert(Boolean bool) {
			return bool ? 1 : 0;
		}
	}
}
