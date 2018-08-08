/*
 * Copyright 2018 the original author or authors.
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
package ac.simons.music.knowledge.support;

import java.time.Year;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.Optional;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Michael J. Simons
 */
public class YearConverter implements AttributeConverter<Year, Long> {
	@Override
	public Long toGraphProperty(Year value) {
		return Optional.ofNullable(value)
			.map(Year::getValue).map(Integer::longValue).orElse(null);
	}

	@Override
	public Year toEntityAttribute(Long value) {
		return Optional.ofNullable(value)
			.map(Long::intValue).map(Year::of).orElse(null);
	}
}
