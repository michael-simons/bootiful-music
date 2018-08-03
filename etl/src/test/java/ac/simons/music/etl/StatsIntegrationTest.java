package ac.simons.music.etl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

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

	private static final Map<String, Object> CREDENTIALS = Map.of("userName", "bootiful-databases",
		"password", "bootiful-databases",
		"url", "jdbc:postgresql://localhost:5432/bootiful-databases");

	private ServerControls serverControls;
	private URI boltURI;

	@BeforeAll
	void initializeDatabase() throws IOException {
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

			var result = session.run("MATCH (r:Album {name: 'Hot Space'}) - [:RELEASED_BY] -> (a:Artist {name: 'Queen'}) RETURN r, a").single();
			assertTrue(result.containsKey("a"));
			assertTrue(result.containsKey("r"));
			assertEquals("Hot Space", result.get("r").get("name").asString());
		}
	}

	@AfterAll
	void tearDownDatabase() {
		this.serverControls.close();

	}
}