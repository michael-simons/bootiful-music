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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.ogm.driver.ParameterConversionMode;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Michael J. Simons
 */
@Testcontainers
@DataNeo4jTest
class CountryRepositoryTest {

	@Container
	private static final Neo4jContainer neo4jContainer = new Neo4jContainer()
		.withAdminPassword(null);

	private final Session session;

	private final CountryRepository countryRepository;

	@Autowired
	CountryRepositoryTest(Session session, CountryRepository countryRepository) {
		this.session = session;
		this.countryRepository = countryRepository;
	}

	@BeforeAll
	static void prepareTestdata() {
		try (var driver = GraphDatabase.driver(neo4jContainer.getBoltUrl(), AuthTokens.none());
			var session = driver.session()
		) {
			session.writeTransaction(work ->
				work.run(""
					+ "MERGE (a:ArtistEntity:Band {name: 'Die Ärzte'}) - [:FOUNDED_IN] ->  (:Country {code: 'DE', name:'Germany'}) "
					+ "MERGE (bestie:Album {name: 'Die Bestie in Menschengestalt', releasedIn: 1993}) - [:RELEASED_BY] -> (a) "
					+ "MERGE (drei10:Album {name: '13', releasedIn: 1998}) - [:RELEASED_BY] -> (a)")
			);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void getStatisticsForCountryShouldWork() {
		var statistics = this.countryRepository.getStatisticsFor(new CountryEntity("DE"));
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

	@TestConfiguration
	static class Config {

		@Bean
		public org.neo4j.ogm.config.Configuration configuration() {
			var builder = new org.neo4j.ogm.config.Configuration.Builder();
			builder.uri(neo4jContainer.getBoltUrl());
			builder.withCustomProperty(ParameterConversionMode.CONFIG_PARAMETER_CONVERSION_MODE,
				ParameterConversionMode.CONVERT_NON_NATIVE_ONLY);
			return builder.build();
		}
	}
}