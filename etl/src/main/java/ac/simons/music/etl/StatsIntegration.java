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

import static ac.simons.music.statsdb.Tables.*;

import java.sql.DriverManager;
import java.util.Map;

import org.jooq.impl.DSL;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
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

	private static final String CREATE_YEAR_AND_DECADE //
			= " MERGE (decade:Decade {value: $yearValue-$yearValue%10})"
			+ " MERGE (year:Year {value: $yearValue})"
			+ " MERGE (year) - [:PART_OF] -> (decade)";

	private static final String CREATE_ALBUM_WITH_ARTIST // Statement is to be used with CREATE_YEAR_AND_DECADE
		= " MERGE (artist:Artist {name: $artistName})"
		+ " MERGE (album:Album {name: $albumName})"
		+ " MERGE (album) - [:RELEASED_BY] -> (artist)"
		+ " MERGE (album) - [:RELEASED_IN] -> (year)";

	private static final String CREATE_TRACK_IN_ALBUM // Regarding FOREACH see https://stackoverflow.com/a/27578798
		= " MATCH (album:Album {name: $albumName}) - [:RELEASED_BY] -> (artist:Artist {name: $artistName})"
		+ " OPTIONAL MATCH (possibleTrack:Track {name: $trackName}) <- [:CONTAINS] - (:Album) - [:RELEASED_BY] -> (artist)"
		+ " FOREACH( _ IN CASE WHEN possibleTrack IS NULL THEN ['create_track_and_add_relation'] ELSE [] END |"
		+ "   CREATE (newTrack:Track {name: $trackName})"
		+ "   MERGE (album) - [:CONTAINS {discNumber: $discNumber, trackNumber: $trackNumber}] -> (newTrack)"
		+ " ) "
		+ " FOREACH( _ IN CASE WHEN possibleTrack IS NOT NULL THEN ['add_relation'] ELSE [] END |"
		+ "   MERGE (album) - [:CONTAINS {discNumber: $discNumber, trackNumber: $trackNumber}] -> (possibleTrack)"
		+ " )";

	@Context
	public GraphDatabaseService db;

	@Procedure(name = "stats.loadArtistData", mode = Mode.WRITE)
	@Description("Loads all artist data from the given connection.")
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
	@Description("Loads all artist data and also all tracks, generating album nodes in the process.")
	public void loadAlbumData(
		@Name("userName") final String userName,
		@Name("password") final String password,
		@Name("url") final String url) {

		try (var connection = DriverManager.getConnection(url, userName, password);
			var neoTransaction = db.beginTx()) {

			var stats = DSL.using(connection);
			var isNoCompilation = TRACKS.COMPILATION.eq("f");

			stats
				.selectDistinct(ARTISTS.ARTIST, TRACKS.ALBUM, TRACKS.YEAR)
				.from(TRACKS).join(ARTISTS).onKey()
				.where(isNoCompilation)
				.forEach(r -> {
						var parameters = Map.<String, Object>of(
							"artistName", r.get(ARTISTS.ARTIST),
							"albumName", r.get(TRACKS.ALBUM),
							"yearValue", Long.valueOf(r.get(TRACKS.YEAR)));
						db.execute(CREATE_YEAR_AND_DECADE + CREATE_ALBUM_WITH_ARTIST, parameters);
					}
				);

			stats
				.select(ARTISTS.ARTIST, TRACKS.ALBUM, TRACKS.NAME, TRACKS.DISC_NUMBER, TRACKS.TRACK_NUMBER)
				.from(TRACKS).join(ARTISTS).onKey()
				.where(isNoCompilation)
				.forEach(r -> {
					var parameters = Map.<String, Object>of(
						"artistName", r.get(ARTISTS.ARTIST),
						"albumName", r.get(TRACKS.ALBUM),
						"trackName", r.get(TRACKS.NAME),
						"discNumber", r.get(TRACKS.DISC_NUMBER),
						"trackNumber", r.get(TRACKS.TRACK_NUMBER)
					);
					db.execute(CREATE_TRACK_IN_ALBUM, parameters);
				});

			neoTransaction.success();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
