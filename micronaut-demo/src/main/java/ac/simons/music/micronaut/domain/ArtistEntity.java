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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Artist")
public class ArtistEntity {

	@Id
	@GeneratedValue
	private Long id;

	public Long getId() {
		return this.id;
	}

	@Index(unique = true)
	private String name;

	@Index(unique = true)
	private String wikidataEntityId;

	@Relationship("HAS_LINK_TO")
	private Set<WikipediaArticleEntity> wikipediaArticles = new TreeSet<>();

	ArtistEntity() {
	}

	public ArtistEntity(String name, String wikidataEntityId) {
		this.name = name;
		this.wikidataEntityId = wikidataEntityId;
	}

	public ArtistEntity(String name, String wikidataEntityId, Set<WikipediaArticleEntity> wikipediaArticles) {
		this.name = name;
		this.wikidataEntityId = wikidataEntityId;
		this.wikipediaArticles = wikipediaArticles;
	}

	Collection<WikipediaArticleEntity> updateWikipediaLinks(Collection<WikipediaArticleEntity> newLinks) {
		Set<WikipediaArticleEntity> oldLinks = this.wikipediaArticles;
		this.wikipediaArticles = new TreeSet<>(newLinks);
		return oldLinks;
	}

	public String getName() {
		return this.name;
	}

	public String getWikidataEntityId() {
		return this.wikidataEntityId;
	}

	public void setWikidataEntityId(String wikidataEntityId) {
		this.wikidataEntityId = wikidataEntityId;
	}

	public Set<WikipediaArticleEntity> getWikipediaArticles() {
		return Collections.unmodifiableSet(this.wikipediaArticles);
	}
}
