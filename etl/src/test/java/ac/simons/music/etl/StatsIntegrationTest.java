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
package ac.simons.music.etl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Config;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * @author Michael J. Simons
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatsIntegrationTest {

	private static final Config driverConfig = Config.builder().withoutEncryption().build();

	private PostgreSQLContainer statsDb;
	private Map<String, Object> statsDbCredentials;

	private ServerControls knowledgeDbControls;
	private URI boltURI;

	@BeforeAll
	void initializeStatsdb() {

		this.statsDb = new PostgreSQLContainer<>("postgres:9.6-alpine");
		this.statsDb.start();

		statsDbCredentials = Map.of(
			"userName", this.statsDb.getUsername(),
			"password", this.statsDb.getPassword(),
			"url", this.statsDb.getJdbcUrl());

		final Flyway flyway = new Flyway();
		flyway.setDataSource(this.statsDb.getJdbcUrl(), this.statsDb.getUsername(),
			this.statsDb.getPassword());
		flyway.migrate();
	}

	@BeforeAll
	void initializeNeo4j() throws IOException {

		this.knowledgeDbControls = TestServerBuilders.newInProcessBuilder().withProcedure(StatsIntegration.class)
			.newServer();
		this.boltURI = knowledgeDbControls.boltURI();
	}

	@Test
	public void shouldCreateArtistNodes() {

		try (var driver = GraphDatabase.driver(boltURI, driverConfig);
			var session = driver.session()) {

			session.run("CALL stats.loadArtistData({userName}, {password}, {url})", statsDbCredentials);

			var result = session.run("MATCH (a:Artist {name: 'Die Ärzte'}) RETURN a").single();
			assertTrue(result.containsKey("a"));
			assertEquals("Die Ärzte", result.get("a").get("name").asString());
		}
	}

	@Test
	public void shouldCreateAlbumNodesWithRelations() {

		try (var driver = GraphDatabase.driver(boltURI, driverConfig);
			var session = driver.session()) {

			session.run("CALL stats.loadAlbumData({userName}, {password}, {url})", statsDbCredentials);

			var result = session
				.run("MATCH (r:Album {name: 'Hot Space'}) - [:RELEASED_BY] -> (a:Artist {name: 'Queen'}) RETURN r, a")
				.single();
			assertTrue(result.containsKey("a"));
			assertTrue(result.containsKey("r"));
			assertEquals("Hot Space", result.get("r").get("name").asString());
		}
	}

	@AfterAll
	void tearDownNeo4j() {
		this.knowledgeDbControls.close();
	}

	@AfterAll
	void tearDownStatsdb() {
		this.statsDb.stop();
	}
}