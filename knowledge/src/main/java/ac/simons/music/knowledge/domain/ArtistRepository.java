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

import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.session.Session;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * @author Michael J. Simons
 */
public interface ArtistRepository<T extends Artist> extends Neo4jRepository<T, Long>, ArtistRepositoryExt {
	Optional<T> findOneByName(String name);
}

interface ArtistRepositoryExt {
	Band markAsBand(Artist artist);

	SoloArtist markAsSoloArtist(Artist artist);
}

class ArtistRepositoryExtImpl implements ArtistRepositoryExt {

	private static final String CYPHER_MARK_AS_BAND = String
		.format("MATCH (n) WHERE id(n) = $id REMOVE n:%s SET n:%s", SoloArtist.class.getSimpleName(),
			Band.class.getSimpleName());
	private static final String CYPHER_MARK_AS_SOLO_ARTIST = String
		.format("MATCH (n) WHERE id(n) = $id REMOVE n:%s SET n:%s", Band.class.getSimpleName(),
			SoloArtist.class.getSimpleName());

	private final Session session;

	public ArtistRepositoryExtImpl(Session session) {
		this.session = session;
	}

	@Override
	public Band markAsBand(Artist artist) {
		session.query(CYPHER_MARK_AS_BAND, Map.of("id", artist.getId()));
		// Needs to clear the mapping context at this point because this shared session
		// will know the node only as class Artist in this transaction otherwise.
		session.clear();
		return session.load(Band.class, artist.getId());
	}

	@Override
	public SoloArtist markAsSoloArtist(Artist artist) {
		session.query(CYPHER_MARK_AS_SOLO_ARTIST, Map.of("id", artist.getId()));
		// See above
		session.clear();
		return session.load(SoloArtist.class, artist.getId());
	}
}