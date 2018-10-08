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

import static java.util.stream.Collectors.*;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.data.annotation.PersistenceConstructor;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Band")
public class BandEntity extends ArtistEntity {

	@Relationship("FOUNDED_IN")
	@Getter
	@Setter
	private CountryEntity foundedIn;

	@Relationship("HAS")
	private List<MemberEntity> member = new ArrayList<>();

	public BandEntity(String name) {
		this(name, null);
	}

	@PersistenceConstructor
	public BandEntity(String name, CountryEntity foundedIn) {
		super(name);
		this.foundedIn = foundedIn;
	}

	BandEntity addMember(final SoloArtistEntity soloArtist, final YearEntity joinedIn, final YearEntity leftIn) {
		Optional<MemberEntity> existingMember = this.member.stream()
			.filter(m -> m.getArtist().equals(soloArtist) && m.getJoinedIn().equals(joinedIn)).findFirst();
		existingMember.ifPresentOrElse(m -> m.setLeftIn(leftIn), () -> {
			this.member.add(new MemberEntity(soloArtist, joinedIn, leftIn));
		});

		return this;
	}

	public List<SoloArtistEntity> getActiveMember() {
		return member.stream()
			.filter(MemberEntity::isActive)
			.map(MemberEntity::getArtist)
			.collect(toList());
	}
}
