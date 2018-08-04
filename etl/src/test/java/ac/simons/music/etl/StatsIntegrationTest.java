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
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

/**
 * @author Michael J. Simons
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatsIntegrationTest {

	private static final Config driverConfig = Config.build().withoutEncryption().toConfig();

	private static final String STATSDB_USER_NAME = "statsdb-dev";
	private static final String STATSDB_PASSWORD = "dev";
	private static final String STATSDB_URL = "jdbc:postgresql://localhost:5432/bootiful-music?currentSchema=test";
	private static final Map<String, Object> CREDENTIALS = Map.of(
		"userName", STATSDB_USER_NAME,
		"password", STATSDB_PASSWORD,
		"url", STATSDB_URL);

	private ServerControls serverControls;
	private URI boltURI;

	@BeforeAll
	void initializeStatsdb() throws IOException {

		final Flyway flyway = new Flyway();
		flyway.setDataSource(STATSDB_URL, STATSDB_USER_NAME, STATSDB_PASSWORD);
		flyway.migrate();
	}

	@BeforeAll
	void initializeNeo4j() throws IOException {

		this.serverControls = TestServerBuilders.newInProcessBuilder().withProcedure(StatsIntegration.class)
			.newServer();
		this.boltURI = serverControls.boltURI();
	}

	@Test
	public void shouldCreateArtistNodes() {

		try (var driver = GraphDatabase.driver(boltURI, driverConfig);
			var session = driver.session()) {

			session.run("CALL stats.loadArtistData({userName}, {password}, {url})", CREDENTIALS);

			var result = session.run("MATCH (a:Artist {name: 'Die Ärzte'}) RETURN a").single();
			assertTrue(result.containsKey("a"));
			assertEquals("Die Ärzte", result.get("a").get("name").asString());
		}
	}

	@Test
	public void shouldCreateAlbumNodesWithRelations() {

		try (var driver = GraphDatabase.driver(boltURI, driverConfig);
			var session = driver.session()) {

			session.run("CALL stats.loadAlbumData({userName}, {password}, {url})", CREDENTIALS);

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
		this.serverControls.close();
	}
}