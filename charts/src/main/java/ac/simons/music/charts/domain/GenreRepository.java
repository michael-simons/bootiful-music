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
package ac.simons.music.charts.domain;

import static ac.simons.music.statsdb.tables.Genres.*;
import static ac.simons.music.statsdb.tables.Plays.*;
import static ac.simons.music.statsdb.tables.Tracks.*;
import static org.jooq.impl.DSL.*;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

/**
 * @author Michael J. Simons
 */
@Repository
public class GenreRepository {

	private final DSLContext create;

	public GenreRepository(DSLContext create) {
		this.create = create;
	}

	public List<GenreWithPlaycount> findAllWithPlaycount() {
		final Field<Integer> cnt = count().as("cnt");
		return this.create
			.select(GENRES.GENRE, cnt)
			.from(PLAYS)
			.join(TRACKS).onKey()
			.join(GENRES).onKey()
			.groupBy(GENRES.GENRE)
			.orderBy(cnt)
			.fetchInto(GenreWithPlaycount.class);
	}
}