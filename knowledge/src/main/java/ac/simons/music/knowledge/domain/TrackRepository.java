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

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Michael J. Simons
 */
interface TrackRepository extends Neo4jRepository<TrackEntity, Long> {

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
		+ " RETURN DISTINCT monthsPlayedTogether, otherTrack AS track, artist "
		+ " ORDER BY artist.name, track.name"
	)
	List<TrackAndArtist> findAllByPlayedTogetherInSameMonth(
		@Param("trackId") Long trackId,
		@Param("monthsPlayedTogether") long monthsPlayedTogether,
		@Param("limit") long limit
	);

}