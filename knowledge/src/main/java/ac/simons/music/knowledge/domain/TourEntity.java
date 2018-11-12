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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.typeconversion.Convert;

import ac.simons.music.knowledge.support.AbstractAuditableBaseEntity;
import ac.simons.music.knowledge.support.NoOpLocalDateConversion;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Tour")
@Getter
public class TourEntity extends AbstractAuditableBaseEntity {

	@Relationship(type = "WAS_ON", direction = INCOMING)
	private ArtistEntity artist;

	@Index
	private String name;

	@Relationship("STARTED_IN")
	private YearEntity startedIn;

	@Relationship("ASSOCIATED_WITH")
	@Setter
	private AlbumEntity associatedAlbum;

	@Relationship("HAD_PART_OF_ITINERARY")
	private List<PlayedIn> itinerary = new ArrayList<>();

	public TourEntity(ArtistEntity artist, String name, YearEntity startedIn) {
		this.artist = artist;
		this.name = name;
		this.startedIn = startedIn;
	}

	@RelationshipEntity("HAD_PART_OF_ITINERARY")
	@Getter
	public static class PlayedIn {
		@Id
		@GeneratedValue
		private Long playedInId;

		@StartNode
		private TourEntity tour;

		@EndNode
		private MusicVenueEntity venue;

		@Convert(NoOpLocalDateConversion.class)
		@Getter
		private LocalDate visitedAt;

		PlayedIn(TourEntity tour, MusicVenueEntity venue, LocalDate visitedAt) {
			this.tour = tour;
			this.venue = venue;
			this.visitedAt = visitedAt;
		}
	}
}
