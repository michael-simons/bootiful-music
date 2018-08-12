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

import ac.simons.music.knowledge.support.YearConverter;

import java.time.Year;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.typeconversion.Convert;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class Album {
	private Long id;

	@Relationship("RELEASED_BY")
	private Artist artist;

	private String name;

	@Relationship("RELEASED_IN")
	private YearEntity releasedIn;

	private boolean live = false;

	@Relationship("CONTAINS")
	private Set<AlbumTrack> tracks = new TreeSet<>(
		Comparator.comparing(AlbumTrack::getDiscNumber).thenComparing(AlbumTrack::getTrackNumber));

	public Album(Artist artist, String name, YearEntity releasedIn) {
		this.artist = artist;
		this.name = name;
		this.releasedIn = releasedIn;
	}

	public Artist getArtist() {
		return artist;
	}

	public String getName() {
		return name;
	}

	public YearEntity getReleasedIn() {
		return releasedIn;
	}

	public boolean isLive() {
		return live;
	}

	public void setLive(boolean live) {
		this.live = live;
	}

	public Album addTrack(final Track track, final Integer discNumber, final Integer trackNumber) {
		this.tracks.add(new AlbumTrack(this, track, discNumber, trackNumber));
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Album))
			return false;
		Album album = (Album) o;
		return Objects.equals(artist, album.artist) && Objects.equals(name, album.name)
			&& Objects.equals(releasedIn, album.releasedIn);
	}

	@Override
	public int hashCode() {
		return Objects.hash(artist, name, releasedIn);
	}

	@RelationshipEntity("CONTAINS")
	static class AlbumTrack {
		@Id
		@GeneratedValue
		private Long trackID;

		@StartNode
		private Album album;
		@EndNode
		private Track track;
		private Integer discNumber;
		private Integer trackNumber;

		public AlbumTrack(Album album, Track track, Integer discNumber, Integer trackNumber) {
			this.album = album;
			this.track = track;
			this.discNumber = discNumber;
			this.trackNumber = trackNumber;
		}

		public Track getTrack() {
			return track;
		}

		public Integer getDiscNumber() {
			return discNumber;
		}

		public Integer getTrackNumber() {
			return trackNumber;
		}
	}
}
