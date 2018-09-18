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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.session.Session;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * @author Michael J. Simons
 */
interface ArtistRepository<T extends Artist> extends Repository<T, Long>, ArtistRepositoryExt {

	Optional<T> findOneByName(String name);

	// Specifying the relationships is necessary here because the generic queries won't recognize that
	// Band has a relationship to country that _should_ be loaded with default depth of 1.

	@Query("MATCH (n:Artist) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p ORDER BY n.name")
	List<T> findAllOrderedByName();

	@Query("MATCH (n:Artist) WHERE id(n) = $id WITH n MATCH p=(n)-[*0..1]-(m) RETURN p")
	Optional<T> findById(@Param("id") Long id);

	<S extends T> S save(S artist);
}

interface ArtistRepositoryExt {
	Band markAsBand(Artist artist);

	SoloArtist markAsSoloArtist(Artist artist);

	Artist removeQualification(Artist artist);
}

class ArtistRepositoryExtImpl implements ArtistRepositoryExt {

	private static final String CYPHER_MARK_AS_BAND = String.format(
		"MATCH (n) WHERE id(n) = $id\n" +
			"OPTIONAL MATCH (n) - [f:BORN_IN] -> (:Country)\n" +
			"REMOVE n:%s SET n:%s\n" +
			"DELETE f",
		SoloArtist.class.getSimpleName(),
		Band.class.getSimpleName());
	private static final String CYPHER_MARK_AS_SOLO_ARTIST = String.format(
		"MATCH (n) WHERE id(n) = $id\n" +
			"OPTIONAL MATCH (n) - [f:FOUNDED_IN] -> (:Country)\n" +
			"REMOVE n:%s SET n:%s\n" +
			"DELETE f",
		Band.class.getSimpleName(),
		SoloArtist.class.getSimpleName());
	private static final String CYPHER_REMOVE_QUALIFICATION = String.format(
		"MATCH (n) WHERE id(n) = $id\n" +
			"OPTIONAL MATCH (n) - [f] -> (:Country)\n" +
			"REMOVE n:%s, n:%s\n" +
			"DELETE f",
		Band.class.getSimpleName(),
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

	@Override
	public Artist removeQualification(Artist artist) {
		session.query(CYPHER_REMOVE_QUALIFICATION, Map.of("id", artist.getId()));
		// See above
		session.clear();
		return session.load(Artist.class, artist.getId());
	}
}