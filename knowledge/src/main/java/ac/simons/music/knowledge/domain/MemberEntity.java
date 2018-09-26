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
package ac.simons.music.knowledge.domain;

import static org.neo4j.ogm.annotation.Relationship.INCOMING;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.data.annotation.PersistenceConstructor;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Member")
public class MemberEntity {
	@Id
	@GeneratedValue
	private Long id;

	@Relationship(value = "IS_A", direction = INCOMING)
	private SoloArtistEntity value;

	@Relationship("JOINED_IN")
	private YearEntity joinedIn;

	@Relationship("LEFT_IN")
	private YearEntity leftIn;

	@PersistenceConstructor MemberEntity(final SoloArtistEntity artist, final YearEntity joinedIn, final YearEntity leftIn) {
		this.value = artist;
		this.joinedIn = joinedIn;
		this.leftIn = leftIn;
	}

	public Long getId() {
		return id;
	}

	public SoloArtistEntity getArtist() {
		return value;
	}

	public YearEntity getJoinedIn() {
		return joinedIn;
	}

	public YearEntity getLeftIn() {
		return leftIn;
	}

	public void setLeftIn(YearEntity leftIn) {
		this.leftIn = leftIn;
	}

	public boolean isActive() {
		return this.leftIn == null;
	}
}
