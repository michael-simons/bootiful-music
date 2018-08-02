package ac.simons.music.etl;

import static ac.simons.music.statsdb.Tables.*;

import java.sql.DriverManager;
import java.util.Map;

import org.jooq.impl.DSL;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

/**
 * This extensions integrates the music graph database with the music statistics database. The music statistics
 * database is a relational database containing all the "nacked" artist data, track data and each individual play of a track.
 *
 * @author Michael J. Simons, 2018-08-02
 */
public class StatsIntegration {

	private static final String CREATE_ARTIST_NODE = "MERGE (a:Artist {name: $artistName}) RETURN a";
	private static final String CREATE_ALBUM_WITH_ARTIST //
		= "MERGE (artist:Artist {name: $artistName})\n"
		+ "MERGE (album:Album {name: $albumName, releasedIn: $releasedIn})\n"
		+ "MERGE (album) - [:RELEASED_BY] -> (artist)";

	@Context
	public GraphDatabaseService db;

	@Context
	public Log log;

	@Procedure(name = "stats.loadArtistData", mode = Mode.WRITE)
	public void loadArtistData(
		@Name("userName") final String userName,
		@Name("password") final String password,
		@Name("url") final String url) {

		try (var connection = DriverManager.getConnection(url, userName, password);
			var neoTransaction = db.beginTx()) {

			DSL.using(connection)
				.selectFrom(ARTISTS)
				.forEach(a ->
					db.execute(CREATE_ARTIST_NODE, Map.of("artistName", a.getArtist()))
				);
			neoTransaction.success();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Procedure(name = "stats.loadAlbumData", mode = Mode.WRITE)
	public void loadAlbumData(
		@Name("userName") final String userName,
		@Name("password") final String password,
		@Name("url") final String url) {

		try (var connection = DriverManager.getConnection(url, userName, password);
			var neoTransaction = db.beginTx()) {

			DSL.using(connection)
				.selectDistinct(ARTISTS.ARTIST, TRACKS.ALBUM, TRACKS.YEAR)
				.from(TRACKS).join(ARTISTS).onKey()
				.where(TRACKS.COMPILATION.eq("f"))
				.forEach(r -> {
						var parameters = Map.<String, Object>of(
							"artistName", r.get(ARTISTS.ARTIST),
							"albumName", r.get(TRACKS.ALBUM),
							"releasedIn", Long.valueOf(r.get(TRACKS.YEAR)));
						db.execute(CREATE_ALBUM_WITH_ARTIST, parameters);
					}
				);
			neoTransaction.success();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
