/*
 * Copyright 2017-2025 the original author or authors.
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
package org.springframework.data.jdbc.repository.support;

import java.io.Serializable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.DataAccessStrategyFactory;
import org.springframework.data.jdbc.core.convert.InsertStrategyFactory;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.QueryMappingConfiguration;
import org.springframework.data.jdbc.core.convert.SqlGeneratorSource;
import org.springframework.data.jdbc.core.convert.SqlParametersFactory;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.util.Assert;

/**
 * Special adapter for Springs {@link org.springframework.beans.factory.FactoryBean} interface to allow easy setup of
 * repository factories via Spring configuration.
 *
 * @author Jens Schauder
 * @author Greg Turnquist
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 * @author Hebert Coelho
 * @author Chirag Tailor
 * @author Mikhail Polivakha
 * @author Sergey Korotaev
 */
public class JdbcRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
		extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> implements ApplicationEventPublisherAware {

	private ApplicationEventPublisher publisher;
	private BeanFactory beanFactory;
	private RelationalMappingContext mappingContext;
	private JdbcConverter converter;
	private DataAccessStrategy dataAccessStrategy;
	private QueryMappingConfiguration queryMappingConfiguration;
	private NamedParameterJdbcOperations operations;
	private EntityCallbacks entityCallbacks;
	private Dialect dialect;

	/**
	 * Creates a new {@link JdbcRepositoryFactoryBean} for the given repository interface.
	 *
	 * @param repositoryInterface must not be {@literal null}.
	 */
	public JdbcRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {

		super.setApplicationEventPublisher(publisher);

		this.publisher = publisher;
	}

	/**
	 * Creates the actual {@link RepositoryFactorySupport} instance.
	 */
	@Override
	protected RepositoryFactorySupport doCreateRepositoryFactory() {

		JdbcRepositoryFactory jdbcRepositoryFactory = new JdbcRepositoryFactory(dataAccessStrategy, mappingContext,
				converter, dialect, publisher, operations);
		jdbcRepositoryFactory.setQueryMappingConfiguration(queryMappingConfiguration);
		jdbcRepositoryFactory.setEntityCallbacks(entityCallbacks);
		jdbcRepositoryFactory.setBeanFactory(beanFactory);

		return jdbcRepositoryFactory;
	}

	public void setMappingContext(RelationalMappingContext mappingContext) {

		Assert.notNull(mappingContext, "MappingContext must not be null");

		super.setMappingContext(mappingContext);
		this.mappingContext = mappingContext;
	}

	public void setDialect(Dialect dialect) {

		Assert.notNull(dialect, "Dialect must not be null");

		this.dialect = dialect;
	}

	/**
	 * @param dataAccessStrategy can be {@literal null}.
	 */
	public void setDataAccessStrategy(DataAccessStrategy dataAccessStrategy) {

		Assert.notNull(dataAccessStrategy, "DataAccessStrategy must not be null");

		this.dataAccessStrategy = dataAccessStrategy;
	}

	/**
	 * @param queryMappingConfiguration can be {@literal null}. {@link #afterPropertiesSet()} defaults to
	 *          {@link QueryMappingConfiguration#EMPTY} if {@literal null}.
	 */
	public void setQueryMappingConfiguration(QueryMappingConfiguration queryMappingConfiguration) {

		Assert.notNull(queryMappingConfiguration, "QueryMappingConfiguration must not be null");

		this.queryMappingConfiguration = queryMappingConfiguration;
	}

	public void setJdbcOperations(NamedParameterJdbcOperations operations) {

		Assert.notNull(operations, "NamedParameterJdbcOperations must not be null");

		this.operations = operations;
	}

	public void setConverter(JdbcConverter converter) {

		Assert.notNull(converter, "JdbcConverter must not be null");

		this.converter = converter;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {

		super.setBeanFactory(beanFactory);

		this.beanFactory = beanFactory;
	}

	@Override
	public void afterPropertiesSet() {

		Assert.state(this.converter != null, "RelationalConverter is required and must not be null");

		if (mappingContext == null) {
			Assert.state(beanFactory != null, "If no MappingContext are set a BeanFactory must be available");

			this.mappingContext = beanFactory.getBean(RelationalMappingContext.class);
		}

		if (this.operations == null) {

			Assert.state(beanFactory != null, "If no JdbcOperations are set a BeanFactory must be available");

			this.operations = beanFactory.getBean(NamedParameterJdbcOperations.class);
		}

		if (this.queryMappingConfiguration == null) {
			Assert.state(beanFactory != null, "If no QueryMappingConfiguration are set a BeanFactory must be available");

			this.queryMappingConfiguration = beanFactory.getBeanProvider(QueryMappingConfiguration.class)
					.getIfAvailable(() -> QueryMappingConfiguration.EMPTY);
		}

		if (this.dataAccessStrategy == null) {

			Assert.state(beanFactory != null, "If no DataAccessStrategy is set a BeanFactory must be available");

			this.dataAccessStrategy = this.beanFactory.getBeanProvider(DataAccessStrategy.class) //
					.getIfAvailable(() -> {

						Assert.state(this.dialect != null, "Dialect is required and must not be null");

						SqlGeneratorSource sqlGeneratorSource = new SqlGeneratorSource(this.mappingContext, this.converter,
								this.dialect);
						SqlParametersFactory sqlParametersFactory = new SqlParametersFactory(this.mappingContext, this.converter);
						InsertStrategyFactory insertStrategyFactory = new InsertStrategyFactory(this.operations, this.dialect);

						DataAccessStrategyFactory factory = new DataAccessStrategyFactory(sqlGeneratorSource, this.converter,
								this.operations, sqlParametersFactory, insertStrategyFactory, queryMappingConfiguration);

						return factory.create();
					});
		}

		if (beanFactory != null) {
			entityCallbacks = EntityCallbacks.create(beanFactory);
		}

		super.afterPropertiesSet();
	}
}
