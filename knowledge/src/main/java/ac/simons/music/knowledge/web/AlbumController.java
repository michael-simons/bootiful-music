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
package ac.simons.music.knowledge.web;

import static java.util.stream.Collectors.*;

import ac.simons.music.knowledge.domain.AlbumEntity;
import ac.simons.music.knowledge.domain.AlbumService;
import ac.simons.music.knowledge.domain.ArtistEntity;
import ac.simons.music.knowledge.domain.BandEntity;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael J. Simons
 */
@Controller
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {
	public static final Comparator<AlbumCmd> ALBUM_COMPARATOR = Comparator.comparing(AlbumCmd::getReleasedIn)
		.thenComparing(AlbumCmd::getName);

	private final AlbumService albumService;

	@GetMapping(value = { "", "/" }, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView albums() {
		return new ModelAndView("albums", Map.of("albums", List.of()));
	}

	@GetMapping(value = { "/by-artist" }, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView byArtist(@RequestParam final String artist) {

		var albums = mapAlbumEntities(this.albumService.findAllAlbumsByArtist(artist));
		return new ModelAndView("albums", Map.of("artist", artist, "albums", albums));
	}

	@GetMapping(value = { "/by-name" }, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView byName(@RequestParam final String name) {

		var albums = mapAlbumEntities(this.albumService.findAllAlbumsByName(name));
		return new ModelAndView("albums", Map.of("name", name, "albums", albums));
	}

	@GetMapping(value = "/{albumId}", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView album(@PathVariable final Long albumId) {

		var album = this.albumService.findAlbumById(albumId)
			.orElseThrow(() -> new NodeNotFoundException(AlbumEntity.class, albumId));

		var model = Map.of(
			"albumForm", new AlbumCmd(album),
			"tracks", this.albumService.findAllTracksContainedOn(album)
		);
		return new ModelAndView("album", model);
	}

	static List<AlbumCmd> mapAlbumEntities(List<AlbumEntity> albums) {

		return albums.stream()
			.map(AlbumCmd::new)
			.sorted(ALBUM_COMPARATOR)
			.collect(toList());
	}

	@Data
	static class AlbumCmd {
		private Long id;

		@NotBlank
		private Long artistId;

		private String artist;

		@NotBlank
		private String name;

		@NotBlank
		private Year releasedIn;

		@NotBlank
		private Long genreId;

		private String genre;

		public AlbumCmd() {
		}

		AlbumCmd(final AlbumEntity album) {
			this.id = album.getId();
			this.name = album.getName();
			this.artistId = album.getArtist().getId();
			this.artist = album.getArtist().getName();
			this.releasedIn = album.getReleasedIn().asYear();
			this.genreId = album.getGenre().getId();
			this.genre = album.getGenre().getName();
		}
	}
}
