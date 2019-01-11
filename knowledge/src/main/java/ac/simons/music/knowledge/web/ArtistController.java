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

import static ac.simons.music.knowledge.web.AlbumController.mapAlbumEntities;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import ac.simons.music.knowledge.domain.AlbumService;
import ac.simons.music.knowledge.domain.ArtistEntity;
import ac.simons.music.knowledge.domain.ArtistService;
import ac.simons.music.knowledge.domain.BandEntity;
import ac.simons.music.knowledge.domain.BandEntity.Member;
import ac.simons.music.knowledge.domain.CountryEntity;
import ac.simons.music.knowledge.domain.SoloArtistEntity;
import ac.simons.music.knowledge.domain.YearEntity;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author Michael J. Simons
 */
@Controller
@RequestMapping("/artists")
@RequiredArgsConstructor
public class ArtistController {

	private final ArtistService artistService;

	private final AlbumService albumService;

	@ModelAttribute("countries")
	public List<CountryValue> countries(final Locale locale) {
		return CountryValue.getAll(locale);
	}

	@GetMapping(value = "/new", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView artist() {
		return new ModelAndView("artist", Map.of("artistCmd", new ArtistCmd()));
	}

	@GetMapping(value = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView artists() {

		var artists = this.artistService.findAllArtists()
				.stream().map(ArtistCmd::new).collect(toList());
		return new ModelAndView("artists", Map.of("artists", artists));
	}

	@ModelAttribute(name = "newMemberForm")
	NewMemberCmd newMemberForm() {
		return new NewMemberCmd();
	}

	@ModelAttribute(name = "newAssociatedArtistForm")
	NewAssociatedArtistCmd newAssociatedArtistForm() {
		return new NewAssociatedArtistCmd();
	}

	@GetMapping(value = "/{artistId}", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView artist(@PathVariable final Long artistId) {

		var artist = this.artistService.findArtistById(artistId)
				.orElseThrow(() -> new NodeNotFoundException(ArtistEntity.class, artistId));
		var members = (artist instanceof BandEntity) ? ((BandEntity) artist).getMember() : List.<Member>of();

		var soloArtists = this.artistService.findAllSoloArtists();
		var associatedArtists = this.artistService.findAssociatedArtists(artist);
		var notAssociatedArtists = this.artistService.findArtistsNotAssociatedWith(artist);
		var wikipediaArticles = artist.getWikipediaArticles().stream()
				.filter(a -> List.of("dewiki", "enwiki").contains(a.getSite())).collect(toList());
		var albums = mapAlbumEntities(this.albumService.findAllAlbumsByArtist(artist.getName()));
		var tours = this.artistService.findToursByArtist(artist);

		var model = Map.of(
				"artistCmd", new ArtistCmd(artist),
				"members", members,
				"soloArtists", soloArtists,
				"associatedArtists", associatedArtists,
				"notAssociatedArtists", notAssociatedArtists,
				"wikipediaArticles", wikipediaArticles,
				"albums", albums,
				"tours", tours
		);
		return new ModelAndView("artist", model);
	}

	@PostMapping(value = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
	public String artist(@Valid final ArtistCmd artistCmd, final BindingResult artistBindingResult) {
		if (artistBindingResult.hasErrors()) {
			return "artist";
		}

		final Class<? extends ArtistEntity> targetType = artistCmd.getType().getImplementingClass();
		var optionalArtists = Optional.ofNullable(artistCmd.getId())
				.flatMap(this.artistService::findArtistById);

		ArtistEntity artist;
		if (optionalArtists.isPresent()) {
			artist = this.artistService.updateArtist(optionalArtists.get(), artistCmd.getOrigin(), artistCmd.getActiveSince(), artistCmd.getWikidataEntityId(), targetType);
		} else {
			artist = this.artistService.createNewArtist(artistCmd.getName(), artistCmd.getOrigin(), artistCmd.getActiveSince(), artistCmd.getWikidataEntityId(), targetType);
		}

		return String.format("redirect:/artists/%d", artist.getId());
	}

	@PostMapping(value = "/{bandId}/member", produces = MediaType.TEXT_HTML_VALUE)
	public String member(@PathVariable final Long bandId, @Valid final NewMemberCmd newMemberCmd, final BindingResult newMemberBindingResult) {
		var band = this.artistService.findBandById(bandId)
				.orElseThrow(() -> new NodeNotFoundException(BandEntity.class, bandId));

		if (!newMemberBindingResult.hasErrors()) {
			var soloArtist = this.artistService.findSoloArtistById(newMemberCmd.artistId)
					.orElseThrow(() -> new NodeNotFoundException(SoloArtistEntity.class, newMemberCmd.artistId));
			band = this.artistService.addMember(band, soloArtist, newMemberCmd.joinedIn, newMemberCmd.leftIn);
		}
		return String.format("redirect:/artists/%d", band.getId());
	}

	@PostMapping(value = "/{artistId}/associateWith", produces = MediaType.TEXT_HTML_VALUE)
	public String associateWith(@PathVariable final Long artistId, @Valid final NewAssociatedArtistCmd newAssociatedArtistCmd, final BindingResult newAssociatedArtistBindingResult) {
		var artist = this.artistService.findArtistById(artistId)
				.orElseThrow(() -> new NodeNotFoundException(ArtistEntity.class, artistId));

		if (!newAssociatedArtistBindingResult.hasErrors()) {
			var newAssociatedArtist = this.artistService.findArtistById(newAssociatedArtistCmd.artistId)
					.orElseThrow(() -> new NodeNotFoundException(ArtistEntity.class, newAssociatedArtistCmd.artistId));
			this.artistService.associate(artist, newAssociatedArtist);
		}
		return String.format("redirect:/artists/%d", artist.getId());
	}

	@DeleteMapping(value = "/{artistId}", produces = MediaType.TEXT_HTML_VALUE)
	public View delete(@PathVariable final Long artistId) {

		this.artistService.deleteArtist(artistId);
		return new RedirectView(fromMethodCall(on(ArtistController.class).artists()).build().toUriString());
	}

	enum ArtistType {
		ARTIST(ArtistEntity.class), BAND(BandEntity.class), SOLO_ARTIST(SoloArtistEntity.class);

		private final Class<? extends ArtistEntity> implementingClass;

		ArtistType(Class<? extends ArtistEntity> implementingClass) {
			this.implementingClass = implementingClass;
		}

		public Class<? extends ArtistEntity> getImplementingClass() {
			return implementingClass;
		}

		static ArtistType determineFrom(final ArtistEntity artist) {
			return Stream.of(BAND, SOLO_ARTIST)
					.filter(t -> t.implementingClass.isInstance(artist))
					.findFirst().orElse(ARTIST);
		}
	}

	static class CountryValue {
		private final String code;

		private final String label;

		public CountryValue(String code, String label) {
			this.code = code;
			this.label = label;
		}

		public String getCode() {
			return code;
		}

		public String getLabel() {
			return label;
		}

		static List<CountryValue> getAll(final Locale locale) {
			return Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream()
					.map(cc -> new CountryValue(cc, new Locale("", cc).getDisplayCountry(locale)))
					.sorted(Comparator.comparing(CountryValue::getLabel))
					.collect(toList());
		}
	}

	@Data
	static class ArtistCmd {
		private Long id;

		@NotBlank
		private String name;

		@NotNull
		private ArtistType type;

		private String origin;

		private Year activeSince;

		private String wikidataEntityId;

		public ArtistCmd() {
		}

		ArtistCmd(final ArtistEntity artist) {
			this.id = artist.getId();
			this.name = artist.getName();
			this.type = ArtistType.determineFrom(artist);
			CountryEntity origin = null;
			if (artist instanceof BandEntity) {
				origin = ((BandEntity) artist).getFoundedIn();
				this.activeSince = Optional.ofNullable(((BandEntity) artist).getActiveSince()).map(YearEntity::asYear).
						orElse(null);
			} else if (artist instanceof SoloArtistEntity) {
				origin = ((SoloArtistEntity) artist).getBornIn();
				activeSince = null;
			}
			this.origin = Optional.ofNullable(origin).map(CountryEntity::getCode).orElse(null);
			this.wikidataEntityId = artist.getWikidataEntityId();
		}

		@Nullable
		public String getLocalizedOrigin(final Locale locale) {
			return Optional.ofNullable(this.origin)
					.filter(code -> !code.isEmpty())
					.map(code -> new Locale("", code).getDisplayCountry(locale))
					.orElse(null);
		}

		public boolean isNew() {
			return this.id == null;
		}
	}

	@Data
	static class NewMemberCmd {
		@NotNull
		private Long artistId;

		@NotNull
		private Year joinedIn;

		private Year leftIn;
	}

	@Data
	static class NewAssociatedArtistCmd {
		@NotNull
		private Long artistId;
	}
}