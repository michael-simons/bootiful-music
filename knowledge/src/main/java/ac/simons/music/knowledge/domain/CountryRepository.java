/*
 * Copyright (c) 2018 michael-simons.eu.
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

/**
 * @author Michael J. Simons
 */
public interface CountryRepository extends Neo4jRepository<Country, Long> {
	Optional<Country> findByCode(String code);

	@Query("MATCH (a:Album) - [:RELEASED_BY] -> (b:Band) - [:FOUNDED_IN] -> (:Country {code: :#{#country.code}}) "
			+ "WITH a AS album ORDER BY album.name "
			+ "RETURN album.releasedIn AS year, COLLECT(album.name) AS albums ORDER BY year ")
	List<CountryStatistics> getStatisticsFor(Country country);
}
