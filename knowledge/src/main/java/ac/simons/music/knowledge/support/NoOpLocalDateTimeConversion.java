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

import java.time.LocalDateTime;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * This is a stupid workaround for a bug in OGM together with Java-Driver 1.6 and native LocalDateTimes.
 *
 * @author Michael J. Simons
 */
public class NoOpLocalDateTimeConversion implements AttributeConverter<LocalDateTime, LocalDateTime> {
	@Override
	public LocalDateTime toGraphProperty(LocalDateTime value) {
		return value;
	}

	@Override
	public LocalDateTime toEntityAttribute(LocalDateTime value) {
		return value;
	}
}
