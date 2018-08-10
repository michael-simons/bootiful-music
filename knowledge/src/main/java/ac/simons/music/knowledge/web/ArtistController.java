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

import static java.util.stream.Collectors.toList;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ac.simons.music.knowledge.domain.Artist;
import ac.simons.music.knowledge.domain.ArtistService;
import ac.simons.music.knowledge.domain.Band;
import ac.simons.music.knowledge.domain.Country;
import ac.simons.music.knowledge.domain.SoloArtist;

/**
 * @author Michael J. Simons
 */
@Controller
@RequestMapping("/artists")
public class ArtistController {

	private final ArtistService artistService;

	public ArtistController(ArtistService artistService) {
		this.artistService = artistService;
	}

	@ModelAttribute("countries")
	public List<CountryValue> countries(final Locale locale) {
		return CountryValue.getAll(locale);
	}

	@GetMapping(value = "/new", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView artist() {
		return new ModelAndView("artist", Map.of("artistForm", new ArtistCmd()));
	}

	@GetMapping(value = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView artists() {

		var artists = this.artistService.findAllArtists()
				.stream().map(ArtistCmd::new).collect(toList());
		return new ModelAndView("artists", Map.of("artists", artists));
	}

	@GetMapping(value = "/{artistId}", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView artist(@PathVariable final Long artistId) {

		var artist = this.artistService.findArtistById(artistId)
				.orElseThrow(() -> new NodeNotFoundException(Artist.class, artistId));

		var soloArtists = this.artistService.findAllSoloArtists();
		var model = Map.of(
				"artistForm", new ArtistCmd(artist),
				"newMemberForm", new NewMemberCmd(), // TODO move add members to it's own page, so that the redirect in #member is not that ugly
				"soloArtists", soloArtists
		);
		return new ModelAndView("artist", model);
	}

	@PostMapping(value = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
	public String artist(@Valid final ArtistCmd artistCmd, final BindingResult artistBindingResult) {
		if (artistBindingResult.hasErrors()) {
			return "artist";
		}

		final Class<? extends Artist> targetType = artistCmd.getType().getImplementingClass();
		Optional.ofNullable(artistCmd.getId())
				.flatMap(this.artistService::findArtistById)
				.ifPresentOrElse(
						artist -> this.artistService.updateArtist(artist, artistCmd.getOrigin(), targetType),
						() -> this.artistService
								.createNewArtist(artistCmd.getName(), artistCmd.getOrigin(), targetType));

		return String.format("redirect:/artists");
	}

	@PostMapping(value = "/{bandId}/member", produces = MediaType.TEXT_HTML_VALUE)
	public String member(@PathVariable final Long bandId, @Valid final NewMemberCmd newMemberCmd, final BindingResult newMemberBindingResult) {
		var band = this.artistService.findBandById(bandId)
				.orElseThrow(() -> new NodeNotFoundException(Band.class, bandId));

		if (!newMemberBindingResult.hasErrors()) {
			var soloArtist = this.artistService.findSoloArtistById(newMemberCmd.artistId)
					.orElseThrow(() -> new NodeNotFoundException(SoloArtist.class, bandId));
			band = this.artistService.addMember(band, soloArtist, newMemberCmd.joinedIn, newMemberCmd.leftIn);
		}
		return String.format("redirect:/artists/%d", band.getId());
	}

	enum ArtistType {
		ARTIST(Artist.class), BAND(Band.class), SOLO_ARTIST(SoloArtist.class);

		private final Class<? extends Artist> implementingClass;

		ArtistType(Class<? extends Artist> implementingClass) {
			this.implementingClass = implementingClass;
		}

		public Class<? extends Artist> getImplementingClass() {
			return implementingClass;
		}

		static ArtistType determineFrom(final Artist artist) {
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

	static class ArtistCmd {
		private Long id;

		@NotBlank
		private String name;

		@NotNull
		private ArtistType type;

		private String origin;

		public ArtistCmd() {
		}

		private ArtistCmd(final Artist artist) {
			this.id = artist.getId();
			this.name = artist.getName();
			this.type = ArtistType.determineFrom(artist);
			Country origin = null;
			if (artist instanceof Band) {
				origin = ((Band) artist).getFoundedIn();
			} else if (artist instanceof SoloArtist) {
				origin = ((SoloArtist) artist).getBornIn();
			}
			this.origin = Optional.ofNullable(origin).map(Country::getCode).orElse(null);
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ArtistType getType() {
			return type;
		}

		public void setType(ArtistType type) {
			this.type = type;
		}

		public String getOrigin() {
			return origin;
		}

		public void setOrigin(String origin) {
			this.origin = origin;
		}

		@Nullable
		public String getLocalizedOrigin(final Locale locale) {
			return Optional.ofNullable(this.origin)
					.filter(code -> !code.isEmpty())
					.map(code -> new Locale("", code).getDisplayCountry(locale))
					.orElse(null);
		}

		@Override
		public String toString() {
			return "ArtistCmd{" +
					"id=" + id +
					", name='" + name + '\'' +
					", type=" + type +
					", origin='" + origin + '\'' +
					'}';
		}
	}

	static class NewMemberCmd {
		@NotNull
		private Long artistId;

		@NotNull
		private Year joinedIn;

		private Year leftIn;

		public Long getArtistId() {
			return artistId;
		}

		public void setArtistId(Long artistId) {
			this.artistId = artistId;
		}

		public Year getJoinedIn() {
			return joinedIn;
		}

		public void setJoinedIn(Year joinedIn) {
			this.joinedIn = joinedIn;
		}

		public Year getLeftIn() {
			return leftIn;
		}

		public void setLeftIn(Year leftIn) {
			this.leftIn = leftIn;
		}
	}
}