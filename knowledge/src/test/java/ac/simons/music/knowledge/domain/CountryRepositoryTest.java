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

import static org.assertj.core.api.Assertions.*;

import java.time.Year;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Michael J. Simons
 */
@ExtendWith(SpringExtension.class)
@DataNeo4jTest
@TestInstance(Lifecycle.PER_CLASS)
class CountryRepositoryTest {

	private final Session session;

	private final CountryRepository countryRepository;

	@Autowired CountryRepositoryTest(Session session, CountryRepository countryRepository) {
		this.session = session;
		this.countryRepository = countryRepository;
	}

	@BeforeAll
	void createTestData() {
		session.query(
			"MERGE (a:Artist:Band {name: 'Die Ärzte'}) - [:FOUNDED_IN] ->  (:Country {code: 'DE', name:'Germany'})\n"
				+ "MERGE (bestie:Album {name: 'Die Bestie in Menschengestalt', releasedIn: 1993}) - [:RELEASED_BY] -> (a)\n"
				+ "MERGE (drei10:Album {name: '13', releasedIn: 1998}) - [:RELEASED_BY] -> (a)", Map.of());
	}

	@Test
	void getStatisticsForCountryShouldWork() {

		var statistics = this.countryRepository.getStatisticsFor(new Country("DE"));
		assertThat(statistics)
			.hasSize(2)
			.satisfies(c -> {
				assertThat(c.getYear()).isEqualTo(Year.of(1993));
				assertThat(c.getAlbums()).containsExactly("Die Bestie in Menschengestalt");
			}, atIndex(0))
			.satisfies(c -> {
				assertThat(c.getYear()).isEqualTo(Year.of(1998));
				assertThat(c.getAlbums()).containsExactly("13");
			}, atIndex(1));
	}
}