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
package ac.simons.music.micronaut.domain;

import javax.inject.Singleton;

import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;

/**
 * @author Michael J. Simons
 */
@Singleton
class ArtistServiceOGM implements ArtistService {
	private final Session session;

	ArtistServiceOGM(Session session) {
		this.session = session;
	}

	public Iterable<ArtistEntity> findByName(String name) {
		return this.session.loadAll(ArtistEntity.class, new Filter("name", ComparisonOperator.CONTAINING, name), 1);
	}
}
