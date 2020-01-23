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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.data.annotation.PersistenceConstructor;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Track")
@EqualsAndHashCode(of = { "name", "writtenBy" }, callSuper = false)
@ToString(of = "name")
public class TrackEntity extends AbstractAuditableBaseEntity {

	@Index(unique = true)
	@Getter
	private String name;

	@Relationship("WRITTEN_BY")
	private Set<ArtistEntity> writtenBy;

	@Relationship("FEATURING")
	private Set<SoloArtistEntity> featuring = new HashSet<>();

	public TrackEntity(String name) {
		this(name, Collections.emptySet());
	}

	@PersistenceConstructor
	public TrackEntity(String name, Set<ArtistEntity> writtenBy) {
		this.name = name;
		this.writtenBy = Optional.ofNullable(writtenBy)
			.map(HashSet::new).orElseGet(HashSet::new);
	}

	public TrackEntity featuring(final SoloArtistEntity artist) {
		this.featuring.add(artist);
		return this;
	}

	public Set<ArtistEntity> getWrittenBy() {
		return Collections.unmodifiableSet(this.writtenBy);
	}

	TrackEntity addAuthor(final SoloArtistEntity soloArtist) {
		if (!this.writtenBy.contains(soloArtist)) {
			this.writtenBy.add(soloArtist);
		}
		return this;
	}
}
