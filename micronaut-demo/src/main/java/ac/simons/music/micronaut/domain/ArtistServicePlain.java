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

import ac.simons.music.micronaut.domain.ArtistService.DefaultArtistService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.inject.Singleton;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Value;

/**
 * @author Michael J. Simons
 */
@Singleton
@DefaultArtistService
class ArtistServicePlain implements ArtistService {
	private final Driver driver;

	ArtistServicePlain(Driver driver) {
		this.driver = driver;
	}

	public List<ArtistEntity> findByName(String name) {
		try (Session s = driver.session()) {
			String statement
				= " MATCH (a:Artist) "
				+ " WHERE a.name contains $name "
				+ " WITH a "
				+ " OPTIONAL MATCH (a) - [:HAS_LINK_TO] -> (w:WikipediaArticle)"
				+ " RETURN a, collect(w) as wikipediaArticles";

			return s.readTransaction(tx -> tx.run(statement, Collections.singletonMap("name", name)))
				.list(record -> {
					final Value artistNode = record.get("a");
					final List<WikipediaArticleEntity> wikipediaArticles = record.get("wikipediaArticles")
						.asList(node -> new WikipediaArticleEntity(
							node.get("site").asString(), node.get("title").asString(), node.get("url").asString()));

					return new ArtistEntity(
						artistNode.get("name").asString(),
						artistNode.get("wikidataEntityId").asString(),
						new HashSet<>(wikipediaArticles)
					);
				});
		}
	}
}
