/*
 * Copyright 2018-2019 the original author or authors.
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
package ac.simons.music.knowledge.domain;

import static org.neo4j.ogm.annotation.Relationship.INCOMING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import ac.simons.music.knowledge.support.AbstractAuditableBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Genre")
@Getter
@EqualsAndHashCode(of = "name", callSuper = false)
public class GenreEntity extends AbstractAuditableBaseEntity {

	@Setter
	@Relationship(value = "IS_SUBGENRE_OF", direction = INCOMING)
	private List<GenreEntity> subgenres = new ArrayList<>();

	@Index(unique = true)
	@Setter
	private String name;

	public GenreEntity(String name) {
		this.name = name;
	}

	public List<GenreEntity> getSubgenres() {

		return Collections.unmodifiableList(this.subgenres);
	}

	GenreEntity addSubgenre(GenreEntity subgenre) {

		Optional<GenreEntity> existingMember = this.subgenres.stream()
				.filter(g -> g.getId().equals(subgenre.getId())).findFirst();
		if (existingMember.isEmpty()) {
			this.subgenres.add(subgenre);
		}
		return this;
	}
}
