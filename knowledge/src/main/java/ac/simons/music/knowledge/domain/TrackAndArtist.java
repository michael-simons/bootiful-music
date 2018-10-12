package ac.simons.music.knowledge.domain;

import lombok.Data;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
@Data
public class TrackAndArtist {
	private TrackEntity track;

	private ArtistEntity artist;
}
