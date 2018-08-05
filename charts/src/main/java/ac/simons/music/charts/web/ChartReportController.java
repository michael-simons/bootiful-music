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
import java.time.YearMonth;

import javax.servlet.http.HttpServletResponse;

import org.jooq.CommonTableExpression;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.Record3;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael J. Simons
 */
@Controller
public class ChartReportController {

	private final DSLContext create;

	public ChartReportController(DSLContext create) {
		this.create = create;
	}

	@GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView index() {
		return new ModelAndView("index");
	}

	@RequestMapping("/{year}/{month}")
	public void getCharts(
		@PathVariable final int year,
		@PathVariable final int month,
		@RequestParam(defaultValue = "10") final int n,
		final HttpServletResponse response
	) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		final Field<Date> playedOnTruncatedToDay = trunc(PLAYS.PLAYED_ON, DatePart.DAY).cast(Date.class);

		YearMonth hlp = YearMonth.of(year, month);
		final CommonTableExpression<Record3<Integer, Integer, Integer>> currentMonth = name("current_month")
			.fields("track_id", "cnt", "position")
			.as(select(PLAYS.TRACK_ID, count().as("cnt"), denseRank().over(orderBy(count().desc())).as("position"))
				.from(PLAYS)
				.where(playedOnTruncatedToDay.between(Date.valueOf(hlp.atDay(1)), Date.valueOf(hlp.atEndOfMonth())))
				.groupBy(PLAYS.TRACK_ID));

		hlp = hlp.minusMonths(1);
		final CommonTableExpression<Record3<Integer, Integer, Integer>> previousMonth = name("previous_month")
			.fields("track_id", "cnt", "position")
			.as(select(PLAYS.TRACK_ID, count().as("cnt"), denseRank().over(orderBy(count().desc())).as("position"))
				.from(PLAYS)
				.where(playedOnTruncatedToDay.between(Date.valueOf(hlp.atDay(1)), Date.valueOf(hlp.atEndOfMonth())))
				.groupBy(PLAYS.TRACK_ID));

		final Field<String> label = concat(ARTISTS.ARTIST, val(" - "), TRACKS.NAME, val(" ("), TRACKS.ALBUM, val(")"))
			.as("label");
		this.create
			.with(currentMonth)
			.with(previousMonth)
			.select(label,
				currentMonth.field("cnt"),
				previousMonth.field("position").minus(currentMonth.field("position"))
					.as("changeInPosition")
			)
			.from(TRACKS)
			.join(ARTISTS).onKey()
			.join(currentMonth).on(currentMonth.field("track_id", Integer.class).eq(TRACKS.ID))
			.leftOuterJoin(previousMonth).on(previousMonth.field("track_id", Integer.class).eq(TRACKS.ID))
			.orderBy(currentMonth.field("cnt").desc(), label.asc())
			.limit(n)
			.fetch()
			.formatJSON(response.getOutputStream());
	}
}
