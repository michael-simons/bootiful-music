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

import static ac.simons.music.knowledge.web.AlbumController.*;
import static java.util.stream.Collectors.*;

import ac.simons.music.knowledge.domain.AlbumService;
import ac.simons.music.knowledge.domain.ArtistService;
import ac.simons.music.knowledge.domain.SoloArtistEntity;
import ac.simons.music.knowledge.domain.TrackEntity;
import ac.simons.music.knowledge.domain.TrackService;
import ac.simons.music.knowledge.web.AlbumController.*;
import ac.simons.music.knowledge.web.ArtistController.ArtistCmd;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael J. Simons
 */
@Controller
@RequestMapping("/tracks")
@RequiredArgsConstructor
public class TrackController {

	private final TrackService trackService;

	private final ArtistService artistService;

	private final AlbumService albumService;

	@GetMapping(value = "/{trackId}", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView track(@PathVariable final Long trackId) {

		var track = this.trackService.findById(trackId)
			.orElseThrow(() -> new NodeNotFoundException(TrackEntity.class, trackId));
		var writtenBy = track.getWrittenBy()
			.stream().map(ArtistCmd::new)
			.collect(toList());
		var containedOn = mapAlbumEntities(this.albumService.findAllAlbumsContaining(track));
		var soloArtists = this.artistService.findAllSoloArtists();

		var model = Map.of(
			"track", track,
			"containedOn", containedOn,
			"writtenBy", writtenBy,
			"newAuthorForm", new NewAuthorCmd(),
			"soloArtists", soloArtists
		);
		return new ModelAndView("track", model);
	}

	@PostMapping(value = "/{trackId}/author", produces = MediaType.TEXT_HTML_VALUE)
	public String author(@PathVariable final Long trackId, @Valid final NewAuthorCmd newAuthorCmd,
		final BindingResult newAuthorBindingResult) {

		var track = this.trackService.findById(trackId)
			.orElseThrow(() -> new NodeNotFoundException(TrackEntity.class, trackId));

		if (!newAuthorBindingResult.hasErrors()) {
			var soloArtist = this.artistService.findSoloArtistById(newAuthorCmd.artistId)
				.orElseThrow(() -> new NodeNotFoundException(SoloArtistEntity.class, newAuthorCmd.artistId));
			track = this.trackService.addAuthor(track, soloArtist);
		}
		return String.format("redirect:/tracks/%d", track.getId());
	}

	@Data
	static class NewAuthorCmd {
		@NotNull
		private Long artistId;
	}

}
