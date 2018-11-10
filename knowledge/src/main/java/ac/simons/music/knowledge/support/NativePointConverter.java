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

import java.util.Optional;

import org.neo4j.driver.v1.Values;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * @author Michael J. Simons
 */
@Component
public class NativePointConverter
	implements AttributeConverter<org.springframework.data.geo.Point, org.neo4j.driver.v1.types.Point>,
	Converter<org.neo4j.driver.v1.types.Point, org.springframework.data.geo.Point> {

	@Override
	public org.neo4j.driver.v1.types.Point toGraphProperty(org.springframework.data.geo.Point value) {
		return Optional.ofNullable(value)
			.map(p -> Values.point(4326, value.getX(), value.getY()).asPoint())
			.orElse(null);
	}

	@Override
	public org.springframework.data.geo.Point toEntityAttribute(org.neo4j.driver.v1.types.Point value) {
		return Optional.ofNullable(value)
			.map(p -> new org.springframework.data.geo.Point(p.x(), p.y()))
			.orElse(null);
	}

	@Override
	public org.springframework.data.geo.Point convert(org.neo4j.driver.v1.types.Point source) {
		return toEntityAttribute(source);
	}
}
