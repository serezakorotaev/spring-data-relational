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
package org.springframework.data.relational.core.sql.render;

import org.springframework.data.relational.core.dialect.InsertRenderContext;
import org.springframework.data.relational.core.dialect.InsertRenderContexts;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

/**
 * Default {@link RenderContext} implementation.
 *
 * @author Mark Paluch
 * @author Jens Schauder
 * @since 1.1
 */
final class SimpleRenderContext implements RenderContext {

	private final RenderNamingStrategy namingStrategy;

	SimpleRenderContext(RenderNamingStrategy namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	@Override
	public IdentifierProcessing getIdentifierProcessing() {
		return IdentifierProcessing.NONE;
	}

	@Override
	public SelectRenderContext getSelectRenderContext() {
		return DefaultSelectRenderContext.INSTANCE;
	}

	@Override
	public InsertRenderContext getInsertRenderContext() {
		return InsertRenderContexts.DEFAULT;
	}

	public RenderNamingStrategy getNamingStrategy() {
		return this.namingStrategy;
	}

	@Override
	public String toString() {

		return "SimpleRenderContext{" + "namingStrategy=" + namingStrategy + '}';
	}

	enum DefaultSelectRenderContext implements SelectRenderContext {
		INSTANCE
	}

}
