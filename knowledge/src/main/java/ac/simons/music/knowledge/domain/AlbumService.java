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

import lombok.RequiredArgsConstructor;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * @author Michael J. Simons
 */
@Service
@RequiredArgsConstructor
public class AlbumService {
	private final AlbumRepository albumRepository;

	public List<AlbumEntity> findAllAlbumsByArtist(ArtistEntity artist) {
		return this.albumRepository.findAllByArtistName(artist.getName(), Sort.by("name").ascending(), 1);
	}

	public List<AlbumEntity> findAllAlbumsByArtistName(String artist) {
		return this.albumRepository.findAllByArtistNameMatchesRegex(createRegex(artist), Sort.by("name").ascending(), 1);
	}

	public List<AlbumEntity> findAllAlbumsByName(String name) {
		return this.albumRepository.findAllByNameMatchesRegex(createRegex(name), Sort.by("name").ascending(), 1);
	}

	public List<AlbumEntity> findAllAlbumsByYear(Year year) {
		return this.albumRepository.findAllByReleasedInValue(year.getValue(), Sort.by("name").ascending(), 1);
	}

	public Optional<AlbumEntity> findAlbumById(Long id) {
		return this.albumRepository.findById(id, 1);
	}

	public List<AlbumTrack> findAllTracksContainedOn(AlbumEntity album) {
		return this.albumRepository.findAllAlbumTracks(album.getId());
	}

	public List<AlbumEntity> findAllAlbumsContaining(TrackEntity track) {
		return this.albumRepository.findAllByTrack(track.getId());
	}

	public List<ReleasesByYear> getNumberOfReleasesByYear() {
		return albumRepository.getNumberOfReleasesByYear();
	}

	public List<AlbumEntity> findAllAlbumsWithGenre(GenreEntity genre) {
		return albumRepository.findAllByGenreNameOrderByName(genre.getName(), 1);
	}

	private static String createRegex(String value) {
		return String.format("(?i).*%s.*", Pattern.quote(value));
	}
}
