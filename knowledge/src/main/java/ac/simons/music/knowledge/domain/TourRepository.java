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
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.Repository;

/**
 * @author Michael J. Simons
 */
public interface TourRepository extends Repository<TourEntity, Long> {

	Optional<TourEntity> findById(Long tourId);

	@Query(value
			= " MATCH (a:Artist) - [:WAS_ON] -> (t:Tour) - [p:HAD_PART_OF_ITINERARY] -> (m:MusicVenue)"
			+ " WHERE a.name = $name"
			+ " WITH t, m ORDER BY t.name, p.visitedAt ASC"
			+ " WITH t, COLLECT(m) AS venues"
			+ " WITH t, reduce(hlp = {d: 0.0, l: head(venues).location}, v IN venues | {d: hlp.d + distance(hlp.l, v.location), l: v.location}).d  AS distanceInMeter"
			+ " MATCH (t) - [:STARTED_IN] -> (y:Year)"
			+ " RETURN id(t) as id, t.name as name, y.value as year, distanceInMeter"
			+ " ORDER by year asc"
	)
	List<TourOverview> findAllByArtistName(String name);
}