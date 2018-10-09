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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Album")
@Getter
@EqualsAndHashCode(of = { "artist", "name", "releasedIn" }, callSuper = false)
public class AlbumEntity extends AbstractAuditableBaseEntity {

	@Relationship("RELEASED_BY")
	private ArtistEntity artist;

	private String name;

	@Relationship("RELEASED_IN")
	private YearEntity releasedIn;

	@Relationship("HAS")
	@Setter
	private GenreEntity genre;

	@Setter
	private boolean live = false;

	@Relationship("CONTAINS")
	@Getter(AccessLevel.NONE)
	private Set<AlbumTrack> tracks = new TreeSet<>(
		Comparator.comparing(AlbumTrack::getDiscNumber).thenComparing(AlbumTrack::getTrackNumber));

	public AlbumEntity(ArtistEntity artist, String name, YearEntity releasedIn) {
		this.artist = artist;
		this.name = name;
		this.releasedIn = releasedIn;
	}

	public AlbumEntity addTrack(final TrackEntity track, final Integer discNumber, final Integer trackNumber) {
		this.tracks.add(new AlbumTrack(this, track, discNumber, trackNumber));
		return this;
	}

	@RelationshipEntity("CONTAINS")
	@Getter
	@ToString(of = { "discNumber", "trackNumber", "track" })
	public static class AlbumTrack {
		@Id
		@GeneratedValue
		@Getter(AccessLevel.NONE)
		private Long trackID;

		@StartNode
		@Getter(AccessLevel.NONE)
		private AlbumEntity album;

		@EndNode
		private TrackEntity track;

		private Integer discNumber;
		private Integer trackNumber;

		public AlbumTrack(AlbumEntity album, TrackEntity track, Integer discNumber, Integer trackNumber) {
			this.album = album;
			this.track = track;
			this.discNumber = discNumber;
			this.trackNumber = trackNumber;
		}
	}
}
