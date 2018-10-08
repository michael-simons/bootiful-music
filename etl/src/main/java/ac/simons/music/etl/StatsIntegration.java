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
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.extract;

import java.sql.DriverManager;
import java.util.Map;

import org.jooq.DatePart;
import org.jooq.impl.DSL;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

/**
 * This extensions integrates the music graph database with the music statistics database. The music statistics
 * database is a relational database containing all the "naked" artist data, track data and each individual play of a track.
 *
 * @author Michael J. Simons, 2018-08-02
 */
public class StatsIntegration {

	private static final String CREATE_ARTIST //
			= " MERGE (artist:Artist {name: $artistName}) "
			+ "    ON CREATE SET artist.createdAt = localdatetime()"
			+ "    ON MATCH SET artist.updatedAt = localdatetime()";

	private static final String CREATE_YEAR_AND_DECADE //
			= " MERGE (decade:Decade {value: $yearValue-$yearValue%10})"
			+ " MERGE (year:Year {value: $yearValue})"
			+ " MERGE (year) - [:PART_OF] -> (decade)";

	private static final String CREATE_ALBUM_WITH_ARTIST // Statement is to be used with CREATE_ARTIST_NODE and  CREATE_YEAR_AND_DECADE
			= " MERGE (album:Album {name: $albumName}) - [:RELEASED_BY] -> (artist) "
		    + "    ON CREATE SET album.createdAt = localdatetime()"
		    + "    ON MATCH SET album.updatedAt = localdatetime()"
			+ " MERGE (genre:Genre {name: $genreName}) "
		    + "    ON CREATE SET genre.createdAt = localdatetime()"
		    + "    ON MATCH SET genre.updatedAt = localdatetime()"
		    + " MERGE (album) - [:HAS] -> (genre)"
			+ " MERGE (album) - [:RELEASED_IN] -> (year)";

	private static final String CREATE_TRACK_IN_ALBUM // Regarding FOREACH see https://stackoverflow.com/a/27578798
			= " MATCH (album:Album {name: $albumName}) - [:RELEASED_BY] -> (artist:Artist {name: $artistName})"
			+ " OPTIONAL MATCH (possibleTrack:Track {name: $trackName}) <- [:CONTAINS] - (:Album) - [:RELEASED_BY] -> (artist)"
			+ " FOREACH( _ IN CASE WHEN possibleTrack IS NULL THEN ['create_track_and_add_relation'] ELSE [] END |"
			+ "   CREATE (newTrack:Track {name: $trackName, createdAt: localdatetime()})"
			+ "   MERGE (album) - [:CONTAINS {discNumber: $discNumber, trackNumber: $trackNumber}] -> (newTrack)"
			+ " ) "
			+ " FOREACH( _ IN CASE WHEN possibleTrack IS NOT NULL THEN ['add_relation'] ELSE [] END |"
			+ "   MERGE (album) - [:CONTAINS {discNumber: $discNumber, trackNumber: $trackNumber}] -> (possibleTrack)"
			+ " )";

	private static final String CREATE_PLAY_COUNTS
			= " MATCH (track:Track {name: $trackName}) <- [:CONTAINS] - (:Album) - [:RELEASED_BY] -> (artist:Artist {name: $artistName})"
			+ CREATE_YEAR_AND_DECADE
			+ " MERGE (month:Month {value: $monthValue}) - [:OF] -> (year)"
			+ " WITH track, month "
			+ " MERGE (track) - [:HAS_BEEN_PLAYED] -> (p:PlayCount) - [:IN] -> (month) ON CREATE set p.value = $newPlayCount ON MATCH SET p.value = $newPlayCount";

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
							db.execute(CREATE_ARTIST, Map.of("artistName", a.getName()))
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
					.selectDistinct(ARTISTS.NAME, TRACKS.ALBUM, GENRES.NAME, TRACKS.YEAR)
					.from(TRACKS)
					.join(ARTISTS).onKey()
					.join(GENRES).onKey()
					.where(isNoCompilation)
					.forEach(r -> {
								var parameters = Map.<String, Object>of(
										"artistName", r.get(ARTISTS.NAME),
										"albumName", r.get(TRACKS.ALBUM),
										"genreName", r.get(GENRES.NAME),
										"yearValue", Long.valueOf(r.get(TRACKS.YEAR)));
								db.execute(CREATE_YEAR_AND_DECADE + CREATE_ARTIST +  CREATE_ALBUM_WITH_ARTIST, parameters);
							}
					);

			stats
					.select(ARTISTS.NAME, TRACKS.ALBUM, TRACKS.NAME, TRACKS.DISC_NUMBER, TRACKS.TRACK_NUMBER)
					.from(TRACKS)
					.join(ARTISTS).onKey()
					.where(isNoCompilation)
					.forEach(r -> {
						var parameters = Map.<String, Object>of(
								"artistName", r.get(ARTISTS.NAME),
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

	@Procedure(name = "stats.loadPlayCounts", mode = Mode.WRITE)
	@Description("Loads the play counts for tracks. Note: All the tracks and albums need to be there, use loadAlbumData before.")
	public void loadPlayCounts(
			@Name("userName") final String userName,
			@Name("password") final String password,
			@Name("url") final String url) {

		try (var connection = DriverManager.getConnection(url, userName, password);
			 var neoTransaction = db.beginTx()) {

			var stats = DSL.using(connection);
			var isNoCompilation = TRACKS.COMPILATION.eq("f");

			final String columnNameYear = "yearValue";
			final String columnNameMonth = "monthValue";
			final String columnNameNewPlayCount = "newPlayCount";

			var year = extract(PLAYS.PLAYED_ON, DatePart.YEAR).as(columnNameYear);
			var month = extract(PLAYS.PLAYED_ON, DatePart.MONTH).as(columnNameMonth);

			stats
					.select(
							ARTISTS.NAME,
							TRACKS.NAME,
							year, month,
							count().as("newPlayCount")
					)
					.from(PLAYS)
					.join(TRACKS).onKey()
					.join(ARTISTS).onKey()
					.where(isNoCompilation)
					.groupBy(ARTISTS.NAME, TRACKS.ALBUM, TRACKS.NAME, year, month)
					.forEach(r -> {
						var parameters = Map.of(
								"artistName", r.get(ARTISTS.NAME),
								"trackName", r.get(TRACKS.NAME),
								columnNameYear, r.get(columnNameYear),
								columnNameMonth, r.get(columnNameMonth),
								columnNameNewPlayCount, r.get(columnNameNewPlayCount)
						);
						db.execute(CREATE_PLAY_COUNTS, parameters);
					});

			neoTransaction.success();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
