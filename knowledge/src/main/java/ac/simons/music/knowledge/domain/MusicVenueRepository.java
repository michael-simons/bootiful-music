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

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.Repository;

/**
 * @author Michael J. Simons
 */
public interface MusicVenueRepository extends Repository<MusicVenueEntity, Long> {

	List<MusicVenueEntity> findAllByName(String name);

	@Query(value
			= " MATCH (m:MusicVenue)"
			+ " WHERE distance(m.location, point({latitude: $latitude, longitude: $longitude})) <= $distanceInMeter"
			+ " RETURN m")
	List<MusicVenueEntity> findAllByLocationNear(double latitude, double longitude, double distanceInMeter);

	@Query(value
			= " MATCH (t:Tour) - [p:HAD_PART_OF_ITINERARY] -> (m:MusicVenue)"
			+ " WHERE id(t) = $tourId"
			+ " RETURN m ORDER BY p.visitedAt ASC")
	List<MusicVenueEntity> findAllByTour(long tourId);
}
