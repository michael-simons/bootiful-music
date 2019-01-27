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

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.Repository;

/**
 * @author Michael J. Simons
 */
interface TrackRepository extends Repository<TrackEntity, Long> {

	TrackEntity save(TrackEntity trackEntity);

	Optional<TrackEntity> findById(Long aLong);

	/**
	 * Returns a list of tracks that have been played together with the source track at least in
	 * {@code monthsPlayedTogether} months where the source track has played at least twice.
	 *
	 * @param trackId
	 * @param monthsPlayedTogether
	 * @param limit
	 * @return
	 */
	@Query(value
		= " MATCH (sourceTrack:Track) - [playcountSource:HAS_BEEN_PLAYED_IN] -> (:Month) <-[playcountOtherTrack:HAS_BEEN_PLAYED_IN] - (otherTrack:Track)"
		+ " WHERE id(sourceTrack) = $trackId"
		+ "   AND playcountSource.value >= 2"
		+ " WITH sourceTrack, otherTrack, count(playcountOtherTrack) AS monthsPlayedTogether"
		+ " WHERE monthsPlayedTogether >= $monthsPlayedTogether"
		+ " WITH monthsPlayedTogether, otherTrack ORDER BY monthsPlayedTogether DESC LIMIT $limit"
		+ " MATCH (otherTrack) <- [:CONTAINS] -()- [:RELEASED_BY] -> (artist:Artist)"
		+ " RETURN DISTINCT otherTrack AS track, artist "
		+ " ORDER BY artist.name, track.name"
	)
	List<TrackAndArtist> findAllPlayedTogetherInSameMonth(
		Long trackId,
		long monthsPlayedTogether,
		long limit
	);

	/**
	 * Returns a list of tracks that have been played together with the source track at least in
	 * {@code monthsPlayedTogether} months where the source track has played at least twice.
	 * <br>
	 * This query doesn't return tracks from the same album as the reference track.
	 *
	 * @param trackId
	 * @param monthsPlayedTogether
	 * @param limit
	 * @return
	 */
	@Query(value
		= " MATCH (sourceTrack:Track) - [playcountSource:HAS_BEEN_PLAYED_IN] -> (:Month) <-[playcountOtherTrack:HAS_BEEN_PLAYED_IN] - (otherTrack:Track)"
		+ " WHERE id(sourceTrack) = $trackId"
		+ "   AND playcountSource.value >= 2"
		+ "   AND NOT EXISTS ((sourceTrack)<-[:CONTAINS]-()-[:CONTAINS]->(otherTrack)) "
		+ " WITH sourceTrack, otherTrack, count(playcountOtherTrack) AS monthsPlayedTogether"
		+ " WHERE monthsPlayedTogether >= $monthsPlayedTogether"
		+ " WITH monthsPlayedTogether, otherTrack ORDER BY monthsPlayedTogether DESC LIMIT $limit"
		+ " MATCH (otherTrack) <- [:CONTAINS] -()- [:RELEASED_BY] -> (artist:Artist)"
		+ " RETURN DISTINCT otherTrack AS track, artist "
		+ " ORDER BY artist.name, track.name"
	)
	List<TrackAndArtist> findAllPlayedTogetherInSameMonthOnDifferentAlbums(
		Long trackId,
		long monthsPlayedTogether,
		long limit
	);
}