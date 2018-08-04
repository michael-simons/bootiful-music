/*
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ac.simons.music.charts.config;

import java.util.Map;

import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.RecordMapperProvider;
import org.jooq.RecordType;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultRecordMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;

/**
 * @author Michael J. Simons
 */
@Configuration
@EnableConfigurationProperties(JooqProperties.class)
public class JooqConfig {

	@Bean
	public Settings settings(
		final JooqProperties jooqProperties
	) {
		return new DefaultConfiguration().settings()
			.withRenderNameStyle(RenderNameStyle.LOWER)
			.withRenderKeywordStyle(RenderKeywordStyle.UPPER)
			.withRenderFormatted(jooqProperties.isRenderFormatted());
	}

	@Bean
	public RecordMapperProvider recordMapperProvider(
		final Map<String, RecordMapper> recordMappers
	) {
		return new RecordMapperProvider() {
			@Override
			public <R extends Record, E> RecordMapper<R, E> provide(final RecordType<R> recordType,
				final Class<? extends E> type) {
				return recordMappers
					.values().stream()
					.filter(rm -> {
						final ResolvableType resolvableMapperType = ResolvableType.forClass(rm.getClass()).as(
							RecordMapper.class);
						return resolvableMapperType.resolveGeneric(1).isAssignableFrom(type);
					})
					.findFirst()
					.orElseGet(() -> new DefaultRecordMapper<>(recordType, type));
			}
		};
	}
}
