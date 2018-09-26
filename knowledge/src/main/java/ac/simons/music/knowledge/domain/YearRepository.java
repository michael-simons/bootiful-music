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

import java.time.Year;
import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.session.Session;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * @author Michael J. Simons
 */
public interface YearRepository extends Neo4jRepository<YearEntity, Long>, YearRepositoryExt {
	// TODO Why isn't the year converter picked up here?
	// Answer, see: https://stackoverflow.com/questions/52431160/spring-data-neo4j-filter-by-localdate-doesnt-work/52475719#52475719
	@Query("MATCH (y:Year {value: :#{#year.value}}) RETURN y")
	Optional<YearEntity> findOneByValue(Year year);
}

interface YearRepositoryExt {
	YearEntity createYear(Year year);
}

class YearRepositoryExtImpl implements YearRepositoryExt {

	private final Session session;

	YearRepositoryExtImpl(Session session) {
		this.session = session;
	}

	@Override
	public YearEntity createYear(Year year) {
		var value = year.getValue();
		var valueOfDecade = value - value % 10;

		var decade = Optional.ofNullable(session.queryForObject(DecadeEntity.class, "MATCH (d:Decade {value: $value}) RETURN d", Map.of("value", valueOfDecade))).orElseGet(() -> {
			var newDecade = new DecadeEntity(valueOfDecade);
			session.save(newDecade);
			return newDecade;
		});

		var yearEntity = new YearEntity(decade, value);
		session.save(yearEntity, 0);
		return yearEntity;
	}
}
