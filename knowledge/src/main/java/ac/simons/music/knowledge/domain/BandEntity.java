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

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

import ac.simons.music.knowledge.support.YearConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.typeconversion.Convert;
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

	@Relationship("ACTIVE_SINCE")
	@Getter
	@Setter
	private YearEntity activeSince;

	@Relationship("HAS_MEMBER")
	private List<Member> member = new ArrayList<>();

	public BandEntity(String name) {
		this(name, null);
	}

	@PersistenceConstructor
	public BandEntity(String name, CountryEntity foundedIn) {
		super(name);
		this.foundedIn = foundedIn;
	}

	BandEntity addMember(final SoloArtistEntity soloArtist, final Year joinedIn, final Year leftIn) {
		Optional<Member> existingMember = this.member.stream()
			.filter(m -> m.getArtist().equals(soloArtist) && m.getJoinedIn().equals(joinedIn)).findFirst();
		existingMember.ifPresentOrElse(m -> m.setLeftIn(leftIn), () -> {
			this.member.add(new Member(this, soloArtist, joinedIn, leftIn));
		});

		return this;
	}

	public List<Member> getMember() {

		return this.member.stream().
			sorted(comparing(Member::getJoinedIn).thenComparing(Member::getLeftIn, nullsLast(naturalOrder())))
			.collect(toList());
	}

	public List<SoloArtistEntity> getActiveMember() {
		return member.stream()
			.filter(Member::isActive)
			.map(Member::getArtist)
			.collect(toList());
	}

	@RelationshipEntity("HAS_MEMBER")
	public static class Member {
		@Id
		@GeneratedValue
		private Long memberId;

		@StartNode
		private BandEntity band;

		@EndNode
		@Getter(AccessLevel.PACKAGE)
		private SoloArtistEntity artist;

		@Convert(YearConverter.class)
		@Getter
		private Year joinedIn;

		@Convert(YearConverter.class)
		@Getter
		@Setter
		private Year leftIn;

		Member(final BandEntity band, final SoloArtistEntity artist, final Year joinedIn, final Year leftIn) {
			this.band = band;
			this.artist = artist;
			this.joinedIn = joinedIn;
			this.leftIn = leftIn;

		}

		public Long getId() {
			return this.artist.getId();
		}

		public String getName() {
			return this.artist.getName();
		}

		public boolean isActive() {
			return this.leftIn == null;
		}
	}

}
