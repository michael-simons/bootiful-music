/*
 * Copyright 2018-2019 the original author or authors.
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
import org.springframework.data.repository.Repository;

/**
 * @author Michael J. Simons
 */
interface AlbumRepository extends Repository<AlbumEntity, Long> {
	List<AlbumEntity> findAllByArtistName(String artistName, Sort sort, @Depth int depth);

	List<AlbumEntity> findAllByArtistNameMatchesRegex(String artistName, Sort sort, @Depth int depth);

	List<AlbumEntity> findAllByNameMatchesRegex(String name, Sort sort, @Depth int depth);

	List<AlbumEntity> findAllByReleasedInValue(long value, Sort sort, @Depth int depth);

	@Query(value
			= " MATCH (album:Album) - [c:CONTAINS] -> (track:Track) WHERE id(album) = $albumId"
			+ " RETURN id(track) AS id, track.name AS name, c.discNumber AS discNumber, c.trackNumber AS trackNumber"
			+ " ORDER BY c.discNumber ASC, c.trackNumber ASC"
	)
	List<AlbumTrack> findAllAlbumTracks(long albumId);

	@Query(value
			= " MATCH (album:Album) - [:CONTAINS] -> (track:Track)"
			+ " MATCH p=(album) - [*1] - ()"
			+ " WHERE id(track) = $trackId"
			+ "   AND ALL(relationship IN relationships(p) WHERE type(relationship) <> 'CONTAINS')"
			+ " RETURN p"
	)
	List<AlbumEntity> findAllByTrack(long trackId);

	@Query(value
			= " MATCH (year:Year)"
			+ " WITH year, size( (year) <- [:RELEASED_IN] - (:Album)) AS releasesByYear"
			+ " MATCH (year) - [:PART_OF] -> (decade:Decade)"
			+ " RETURN decade.value AS decade, year.value AS year, releasesByYear AS numberOfReleases"
	)
	List<ReleasesByYear> getNumberOfReleasesByYear();

	@Query(value
			= " MATCH (s:Genre) - [:IS_SUBGENRE_OF] -> (t:Genre)"
			+ " WHERE id(t) = $genreId"
			+ " WITH COLLECT(t) + s AS genres "
			+ " UNWIND genres as g "
			+ " MATCH (a:Album) - [h:HAS] -> (g)"
			+ " MATCH (a) - [ri:RELEASED_IN] -> (y:Year)"
			+ " MATCH (a) - [rb:RELEASED_BY] -> (ar:Artist)"
			+ " RETURN * ORDER BY a.name ASC"
	)
	List<AlbumEntity> findAllByGenre(long genreId);

	Optional<AlbumEntity> findById(Long id);
}
