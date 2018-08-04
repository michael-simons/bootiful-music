/*
 * Copyright 2016-2018 the original author or authors.
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
package ac.simons.music.charts.web;

import static ac.simons.music.statsdb.Tables.*;
import static org.jooq.impl.DSL.*;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Michael J. Simons
 */
@Controller
@RequestMapping("/api/artists")
public class ArtistsReportController {

	private static final Field<Date> PLAYED_ON_TRUNCATED_TO_DAY
		= trunc(PLAYS.PLAYED_ON, DatePart.DAY).cast(Date.class);

	private final DSLContext create;

	public ArtistsReportController(DSLContext create) {
		this.create = create;
	}

	@RequestMapping
	public void getAllArtists(final HttpServletResponse response) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		this.create
			.select()
			.from(ARTISTS)
			.orderBy(ARTISTS.ARTIST.asc())
			.fetch()
			.formatJSON(response.getOutputStream());
	}

	@RequestMapping(path = "/{artistIds}/cumulativePlays")
	public void getCumulativePlays(
		@PathVariable final Integer[] artistIds,
		@RequestParam
		@DateTimeFormat(iso = ISO.DATE) final Optional<LocalDate> from,
		@RequestParam
		@DateTimeFormat(iso = ISO.DATE) final Optional<LocalDate> to,
		final HttpServletResponse response
	) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		this.create
			.select(PLAYED_ON_TRUNCATED_TO_DAY,
				ARTISTS.ARTIST,
				sum(count()).over(partitionBy(ARTISTS.ARTIST).orderBy(PLAYED_ON_TRUNCATED_TO_DAY)).as("cumulativePlays")
			)
			.from(PLAYS)
			.join(TRACKS).onKey()
			.join(ARTISTS).onKey()
			.where(ARTISTS.ID.in(artistIds))
			.and(from.map(Date::valueOf)
				.map(PLAYED_ON_TRUNCATED_TO_DAY::greaterOrEqual)
				.orElseGet(DSL::trueCondition)
			)
			.and(to.map(Date::valueOf)
				.map(PLAYED_ON_TRUNCATED_TO_DAY::lessOrEqual)
				.orElseGet(DSL::trueCondition)
			)
			.groupBy(PLAYED_ON_TRUNCATED_TO_DAY,
				ARTISTS.ARTIST
			)
			.orderBy(PLAYED_ON_TRUNCATED_TO_DAY,
				ARTISTS.ARTIST
			)
			.fetch()
			.formatJSON(response.getOutputStream());
	}

	@RequestMapping(path = "/{artistIds}/topNAlbums")
	public void getTopNAlbums(
		@PathVariable final Integer[] artistIds,
		@RequestParam(defaultValue = "10") final int n,
		@RequestParam
		@DateTimeFormat(iso = ISO.DATE) final Optional<LocalDate> from,
		@RequestParam
		@DateTimeFormat(iso = ISO.DATE) final Optional<LocalDate> to,
		final HttpServletResponse response
	) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		this.create
			.select(TRACKS.ALBUM,
				count())
			.from(PLAYS)
			.join(TRACKS).onKey()
			.where(TRACKS.ARTIST_ID.in(artistIds))
			.and(from.map(Date::valueOf)
				.map(PLAYED_ON_TRUNCATED_TO_DAY::greaterOrEqual)
				.orElseGet(DSL::trueCondition)
			)
			.and(to.map(Date::valueOf)
				.map(PLAYED_ON_TRUNCATED_TO_DAY::lessOrEqual)
				.orElseGet(DSL::trueCondition)
			)
			.groupBy(TRACKS.ARTIST_ID, TRACKS.ALBUM)
			.orderBy(count().desc(), TRACKS.ALBUM.asc())
			.limit(n)
			.fetch()
			.formatJSON(response.getOutputStream());
	}

	@RequestMapping(path = "/{artistIds}/topNTracks")
	public void getTopNTracks(
		@PathVariable final Integer[] artistIds,
		@RequestParam(defaultValue = "10") final int n,
		@RequestParam
		@DateTimeFormat(iso = ISO.DATE) final Optional<LocalDate> from,
		@RequestParam
		@DateTimeFormat(iso = ISO.DATE) final Optional<LocalDate> to,
		final HttpServletResponse response
	) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		this.create
			.select(TRACKS.ALBUM,
				TRACKS.NAME,
				count())
			.from(PLAYS)
			.join(TRACKS).onKey()
			.where(TRACKS.ARTIST_ID.in(artistIds))
			.and(from.map(Date::valueOf)
				.map(PLAYED_ON_TRUNCATED_TO_DAY::greaterOrEqual)
				.orElseGet(DSL::trueCondition)
			)
			.and(to.map(Date::valueOf)
				.map(PLAYED_ON_TRUNCATED_TO_DAY::lessOrEqual)
				.orElseGet(DSL::trueCondition)
			)
			.groupBy(TRACKS.ARTIST_ID, TRACKS.ALBUM, TRACKS.NAME)
			.orderBy(count().desc(), TRACKS.ALBUM.asc(), TRACKS.NAME.asc())
			.limit(n)
			.fetch()
			.formatJSON(response.getOutputStream());
	}
}
