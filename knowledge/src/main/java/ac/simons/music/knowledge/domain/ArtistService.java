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

import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons
 */
@Service
public class ArtistService {
	private final ArtistRepository<Artist> allArtists;

	private final BandRepository bands;

	private final SoloArtistRepository soloArtists;

	private final YearRepository yearRepository;

	private final CountryRepository countryRepository;

	public ArtistService(ArtistRepository<Artist> allArtists, BandRepository bands, SoloArtistRepository soloArtists, YearRepository yearRepository, CountryRepository countryRepository) {
		this.allArtists = allArtists;
		this.bands = bands;
		this.soloArtists = soloArtists;
		this.yearRepository = yearRepository;
		this.countryRepository = countryRepository;
	}

	public Optional<Artist> findArtistById(Long id) {
		return allArtists.findById(id);
	}

	public Optional<Band> findBandById(Long id) {
		return bands.findById(id);
	}

	public Optional<SoloArtist> findSoloArtistById(Long id) {
		return soloArtists.findById(id);
	}

	public List<Artist> findAllArtists() {
		return allArtists.findAllOrderedByName();
	}

	public List<SoloArtist> findAllSoloArtists() {
		return this.soloArtists.findAll(Sort.by("name").ascending());
	}

	@Transactional
	public <T extends Artist> T createNewArtist(final String name, final String countryOfOrigin, final Class<T> type) {
		Artist rv;
		final Country country = determineCountry(countryOfOrigin);
		if (type == Band.class) {
			rv = this.allArtists.save(new Band(name, country));
		} else if (type == SoloArtist.class) {
			rv = this.allArtists.save(new SoloArtist(name, country));
		} else {
			rv = this.allArtists.save(new Artist(name));
		}
		return type.cast(rv);
	}

	@Transactional
	public <T extends Artist> T updateArtist(final Artist artist, final String countryOfOrigin, final Class<T> type) {
		Artist rv;
		if (type == Band.class) {
			var band = allArtists.markAsBand(artist);
			band.setFoundedIn(determineCountry(countryOfOrigin));
			// TODO this is weird... Or at least I wonder if we are doing dirty tracking at all?! Or is it because of the cleared session?
			rv = this.allArtists.save(band);
		} else if (type == SoloArtist.class) {
			var soloArtist = allArtists.markAsSoloArtist(artist);
			soloArtist.setBornIn(determineCountry(countryOfOrigin));
			rv = this.allArtists.save(soloArtist);
		} else {
			rv = allArtists.removeQualification(artist);
		}
		return type.cast(rv);
	}

	@Transactional
	public Band addMember(final Band band, final SoloArtist newMember, final Year joinedIn, @Nullable final Year leftIn) {

		final YearEntity joinedInEntity = this.yearRepository.findOneByValue(joinedIn).orElseGet(() -> yearRepository.createYear(joinedIn));
		final YearEntity leftInEntity = leftIn == null ? null : this.yearRepository.findOneByValue(leftIn).orElseGet(() -> yearRepository.createYear(leftIn));

		return this.bands.save(band.addMember(newMember, joinedInEntity, leftInEntity));
	}

	@Nullable
	Country determineCountry(@Nullable final String code) {
		return code == null || code.trim().isEmpty() ? null :
			countryRepository.findByCode(code).orElseGet(() -> countryRepository.save(new Country(code)));
	}
}
