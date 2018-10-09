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
package ac.simons.music.knowledge.domain;

import ac.simons.music.knowledge.support.AbstractAuditableBaseEntity;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Album")
@Getter
@EqualsAndHashCode(of = { "artist", "name", "releasedIn" }, callSuper = false)
public class AlbumEntity extends AbstractAuditableBaseEntity {

	@Relationship("RELEASED_BY")
	private ArtistEntity artist;

	private String name;

	@Relationship("RELEASED_IN")
	private YearEntity releasedIn;

	@Relationship("HAS")
	@Setter
	private GenreEntity genre;

	@Setter
	private boolean live = false;

	public AlbumEntity(ArtistEntity artist, String name, YearEntity releasedIn) {
		this.artist = artist;
		this.name = name;
		this.releasedIn = releasedIn;
	}

}
