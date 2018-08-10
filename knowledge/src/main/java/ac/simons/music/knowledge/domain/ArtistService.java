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

import java.util.List;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons
 */
@Service
public class ArtistService {
	private final ArtistRepository<Artist> artistRepository;

	private final CountryRepository countryRepository;

	ArtistService(
		ArtistRepository<Artist> artistRepository,
		CountryRepository countryRepository) {
		this.artistRepository = artistRepository;
		this.countryRepository = countryRepository;
	}

	public List<Artist> findAllOrderedByName() {
		return artistRepository.findAllOrderedByName();
	}

	public Optional<Artist> findOneById(Long id) {
		return artistRepository.findOneById(id);
	}

	@Transactional
	public <T extends Artist> T createNewArtist(final String name, final String countryOfOrigin, final Class<T> type) {
		Artist rv;
		final Country country = determineCountry(countryOfOrigin);
		if (type == Band.class) {
			rv = this.artistRepository.save(new Band(name, country));
		} else if (type == SoloArtist.class) {
			rv = this.artistRepository.save(new SoloArtist(name, country));
		} else {
			rv = this.artistRepository.save(new Artist(name));
		}
		return type.cast(rv);
	}

	@Transactional
	public <T extends Artist> T updateArtist(final Artist artist, final String countryOfOrigin, final Class<T> type) {
		Artist rv;
		if (type == Band.class) {
			var band = artistRepository.markAsBand(artist);
			band.setFoundedIn(determineCountry(countryOfOrigin));
			// TODO this is weird... Or at least I wonder if we are doing dirty tracking at all?! Or is it because of the cleared session?
			rv = artistRepository.save(band);
		} else if (type == SoloArtist.class) {
			var soloArtist = artistRepository.markAsSoloArtist(artist);
			soloArtist.setBornIn(determineCountry(countryOfOrigin));
			rv = this.artistRepository.save(soloArtist);
		} else {
			rv = artistRepository.removeQualification(artist);
		}
		return type.cast(rv);
	}

	@Nullable Country determineCountry(final @Nullable String code) {
		return code == null || code.trim().isEmpty() ? null :
			countryRepository.findByCode(code).orElseGet(() -> countryRepository.save(new Country(code)));
	}
}
