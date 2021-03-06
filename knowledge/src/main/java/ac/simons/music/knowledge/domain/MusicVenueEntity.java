/*
 * Copyright 2018-2020 the original author or authors.
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
package ac.simons.music.knowledge.domain;

import ac.simons.music.knowledge.support.AbstractAuditableBaseEntity;
import lombok.Getter;
import lombok.Setter;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;

/**
 * @author Michael J. Simons
 */
@NodeEntity("MusicVenue")
@Getter
public class MusicVenueEntity extends AbstractAuditableBaseEntity {
	@Index
	private String name;

	private GeographicPoint2d location;

	@Relationship("IS_LOCATED_IN")
	@Setter
	private CountryEntity foundedIn;

	public MusicVenueEntity(String name, GeographicPoint2d location) {
		this.name = name;
		this.location = location;
	}
}
