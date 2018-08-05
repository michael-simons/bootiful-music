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
package ac.simons.music.charts.domain;

import static ac.simons.music.statsdb.Tables.ARTISTS;
import static ac.simons.music.statsdb.Tables.PLAYS;
import static ac.simons.music.statsdb.Tables.TRACKS;
import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.denseRank;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.orderBy;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.trunc;
import static org.jooq.impl.DSL.val;

import java.sql.Date;
import java.time.YearMonth;

import org.jooq.CommonTableExpression;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Result;
import org.springframework.stereotype.Service;

/**
 * @author Michael J. Simons
 */
@Service
public class ChartService {

	private final DSLContext statsDb;

	ChartService(DSLContext statsDb) {
		this.statsDb = statsDb;
	}

	public Result<? extends Record3<String, ?, ?>> getCharts(
			final YearMonth selectedMonthOfYear,
			final int n
	) {

		final Field<Date> playedOnTruncatedToDay = trunc(PLAYS.PLAYED_ON, DatePart.DAY).cast(Date.class);

		final CommonTableExpression<Record3<Integer, Integer, Integer>> currentMonth = name("current_month")
				.fields("track_id", "cnt", "position")
				.as(select(PLAYS.TRACK_ID, count().as("cnt"), denseRank().over(orderBy(count().desc())).as("position"))
						.from(PLAYS)
						.where(playedOnTruncatedToDay.between(Date.valueOf(selectedMonthOfYear.atDay(1)), Date.valueOf(selectedMonthOfYear.atEndOfMonth())))
						.groupBy(PLAYS.TRACK_ID));

		final YearMonth previousMonthOfYear = selectedMonthOfYear.minusMonths(1);
		final CommonTableExpression<Record3<Integer, Integer, Integer>> previousMonth = name("previous_month")
				.fields("track_id", "cnt", "position")
				.as(select(PLAYS.TRACK_ID, count().as("cnt"), denseRank().over(orderBy(count().desc())).as("position"))
						.from(PLAYS)
						.where(playedOnTruncatedToDay.between(Date.valueOf(previousMonthOfYear.atDay(1)), Date.valueOf(previousMonthOfYear.atEndOfMonth())))
						.groupBy(PLAYS.TRACK_ID));

		final Field<String> label = concat(ARTISTS.ARTIST, val(" - "), TRACKS.NAME, val(" ("), TRACKS.ALBUM, val(")"))
				.as("label");
		return this.statsDb
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
				.fetch();
	}
}
