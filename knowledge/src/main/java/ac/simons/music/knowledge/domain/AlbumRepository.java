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
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Michael J. Simons
 */
interface AlbumRepository extends Neo4jRepository<AlbumEntity, Long> {
	Optional<AlbumEntity> findOneByArtistNameAndName(String artistName, String name);

	List<AlbumEntity> findAllByArtistNameMatchesRegex(String artistName, Sort sort, @Depth int depth);

	List<AlbumEntity> findAllByNameMatchesRegex(String name, Sort sort, @Depth int depth);

	@Query(value
		= " MATCH (album:Album) - [c:CONTAINS] -> (track:Track) WHERE id(album) = $albumId"
		+ " RETURN id(track) as id, track.name as name, c.discNumber as discNumber, c.trackNumber as trackNumber"
		+ " ORDER BY c.discNumber ASC, c.trackNumber ASC"
	)
	List<AlbumTrack> findAllAlbumTracks(@Param("albumId") Long albumId);

	@Query(value
		= " MATCH (track:Track) <- [:CONTAINS] - (album:Album)"
		+ " MATCH p=(album) - [*1] - ()"
		+ " WHERE id(track) = $trackId"
		+ "   AND ALL(relationship IN relationships(p) WHERE type(relationship) <> 'CONTAINS')"
		+ " RETURN p"
	)
	List<AlbumEntity> findAllByTrack(@Param("trackId") Long trackId);
}
