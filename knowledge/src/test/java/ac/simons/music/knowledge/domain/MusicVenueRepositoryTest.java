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

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverAutoConfiguration;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
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
@ImportAutoConfiguration(Neo4jDriverAutoConfiguration.class)
public class MusicVenueRepositoryTest {

	@Container
	private static final Neo4jContainer neo4jContainer = new Neo4jContainer()
		.withAdminPassword(null);

	private final MusicVenueRepository musicVenueRepository;

	MusicVenueRepositoryTest(@Autowired MusicVenueRepository musicVenueRepository) {
		this.musicVenueRepository = musicVenueRepository;
	}

	@BeforeAll
	static void prepareTestdata(@Autowired Driver driver) {
		try (var session = driver.session()) {
			session.writeTransaction(work ->
				work.run(""
					+ "create (mv:MusicVenue {name: 'Deutschlandhalle'})\n"
					+ " set mv.location = point({latitude: 52.500278, longitude: 13.269722})\n"
					+ " return mv;")
			);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void mappingShouldWork() {
		List<MusicVenueEntity> musicVenueEntity = musicVenueRepository.findAllByName("Deutschlandhalle");

		assertThat(musicVenueEntity)
			.hasSize(1)
			.extracting(MusicVenueEntity::getLocation)
			.containsExactly(new GeographicPoint2d(52.500278, 13.269722));
	}

	@TestConfiguration
	static class Config {

		@Bean
		public Driver neo4jDriver() {
			return GraphDatabase.driver(neo4jContainer.getBoltUrl(), AuthTokens.none());
		}
	}

}
