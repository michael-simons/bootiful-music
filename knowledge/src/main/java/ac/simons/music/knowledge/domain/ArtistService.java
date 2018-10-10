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

import ac.simons.music.knowledge.support.AbstractAuditableBaseEntity;
import lombok.RequiredArgsConstructor;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.session.Session;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons
 */
@Service
@RequiredArgsConstructor
public class ArtistService {

	private final Session session;

	private final BandRepository bands;

	private final SoloArtistRepository soloArtists;

	private final CountryRepository countryRepository;

	public Optional<ArtistEntity> findArtistById(Long id) {

		return Optional.ofNullable(session.load(ArtistEntity.class, id))
			.flatMap(this::loadArtistsDetails);
	}

	@Transactional(readOnly = true)
	public Optional<BandEntity> findBandById(Long id) {
		return bands.findById(id, 2);
	}

	@Transactional(readOnly = true)
	public Optional<SoloArtistEntity> findSoloArtistById(Long id) {
		return soloArtists.findById(id);
	}

	@Transactional(readOnly = true)
	public List<ArtistEntity> findAllArtists() {

		var cypher = "MATCH (a:Artist) WITH a OPTIONAL MATCH p=(a)-[*0..1]-(c:Country) RETURN a, p ORDER BY a.name";

		final List<ArtistEntity> allArtists = new ArrayList<>();
		session.query(ArtistEntity.class, cypher, Map.of())
			.forEach(allArtists::add);
		return allArtists;
	}

	@Transactional(readOnly = true)
	public List<SoloArtistEntity> findAllSoloArtists() {
		return this.soloArtists.findAll(Sort.by("name").ascending());
	}

	@Transactional(readOnly = true)
	public List<BandEntity> findAllBands() {
		return this.bands.findAll(Sort.by("name").ascending());
	}

	@Transactional
	public void associate(ArtistEntity artist1, ArtistEntity artist2) {

		var cypher
			= " MATCH (artist1:Artist {name: $artistName1})"
			+ " MATCH (artist2:Artist {name: $artistName2})"
			+ " MERGE (artist1) - [:ASSOCIATED_WITH] - (artist2)";
		session.query(cypher, Map.of("artistName1", artist1.getName(), "artistName2", artist2.getName()));
	}

	@Transactional
	public List<ArtistEntity> findAssociatedArtists(ArtistEntity artist) {

		var cypher = "MATCH (:Artist {name: $artistName}) - [:ASSOCIATED_WITH] - (another:Artist) RETURN another";

		final List<ArtistEntity> associatedArtists = new ArrayList<>();
		session.query(ArtistEntity.class, cypher, Map.of("artistName", artist.getName()))
			.forEach(associatedArtists::add);
		return associatedArtists;
	}

	@Transactional
	public List<ArtistEntity> findArtistsNotAssociatedWith(ArtistEntity artist) {

		var cypher
			= " MATCH (artist:Artist {name: $artistName})"
			+ " MATCH (notAssociated:Artist)"
			+ " WHERE (artist) <> (notAssociated)"
			+ "   AND NOT (artist:Artist {name: $artistName}) - [:ASSOCIATED_WITH] - (notAssociated)"
			+ "   AND (notAssociated:Band OR notAssociated:SoloArtist)"
			+ " RETURN notAssociated"
			+ " ORDER BY notAssociated.name";

		final List<ArtistEntity> associatedArtists = new ArrayList<>();
		session.query(ArtistEntity.class, cypher, Map.of("artistName", artist.getName()))
			.forEach(associatedArtists::add);
		return associatedArtists;
	}

	@Transactional
	public <T extends ArtistEntity> T createNewArtist(final String name, final String countryOfOrigin, final Class<T> type) {

		ArtistEntity rv;
		final CountryEntity country = determineCountry(countryOfOrigin);
		if (type == BandEntity.class) {
			rv = this.bands.save(new BandEntity(name, country));
		} else if (type == SoloArtistEntity.class) {
			rv = this.soloArtists.save(new SoloArtistEntity(name, country));
		} else {
			rv = new ArtistEntity(name);
			this.session.save(rv);
		}
		return type.cast(rv);
	}

	@Transactional
	public <T extends ArtistEntity> T updateArtist(final ArtistEntity artist, final String countryOfOrigin, final Class<T> type) {

		ArtistEntity rv;
		if (type == BandEntity.class) {
			var band = this.markAsBand(artist);
			band.setFoundedIn(determineCountry(countryOfOrigin));
			rv = this.bands.save(band);
		} else if (type == SoloArtistEntity.class) {
			var soloArtist = this.markAsSoloArtist(artist);
			soloArtist.setBornIn(determineCountry(countryOfOrigin));
			rv = this.soloArtists.save(soloArtist);
		} else {
			rv = this.removeQualification(artist);
		}
		return type.cast(rv);
	}

	@Transactional
	public BandEntity addMember(final BandEntity band, final SoloArtistEntity newMember, final Year joinedIn, @Nullable final Year leftIn) {
		return this.bands.save(band.addMember(newMember, joinedIn, leftIn));
	}

	private BandEntity markAsBand(ArtistEntity artist) {
		var cypher = String.format(
			"MATCH (n) WHERE id(n) = $id\n" +
				"OPTIONAL MATCH (n) - [f:BORN_IN] -> (:Country)\n" +
				"REMOVE n:%s SET n:%s\n" +
				"DELETE f",
			getLabel(SoloArtistEntity.class),
			getLabel(BandEntity.class));

		session.query(cypher, Map.of("id", artist.getId()));
		// Needs to clear the mapping context at this point because this shared session
		// will know the node only as class Artist in this transaction otherwise.
		session.clear();
		return session.load(BandEntity.class, artist.getId());
	}

	private SoloArtistEntity markAsSoloArtist(ArtistEntity artist) {
		var cypher = String.format(
			"MATCH (n) WHERE id(n) = $id\n" +
				"OPTIONAL MATCH (n) - [f:FOUNDED_IN] -> (:Country)\n" +
				"REMOVE n:%s SET n:%s\n" +
				"DELETE f",
			getLabel(BandEntity.class),
			getLabel(SoloArtistEntity.class));

		session.query(cypher, Map.of("id", artist.getId()));
		session.clear();
		return session.load(SoloArtistEntity.class, artist.getId());
	}

	private ArtistEntity removeQualification(ArtistEntity artist) {
		var cypher = String.format(
			"MATCH (n) WHERE id(n) = $id\n" +
				"OPTIONAL MATCH (n) - [f] -> (:Country)\n" +
				"REMOVE n:%s, n:%s\n" +
				"DELETE f",
			getLabel(BandEntity.class),
			getLabel(SoloArtistEntity.class));

		session.query(cypher, Map.of("id", artist.getId()));
		session.clear();
		return session.load(ArtistEntity.class, artist.getId());
	}

	private Optional<? extends ArtistEntity> loadArtistsDetails(final ArtistEntity base) {

		if (base instanceof BandEntity) {
			return this.findBandById(base.getId());
		} else if (base instanceof SoloArtistEntity) {
			return this.findSoloArtistById(base.getId());
		} else {
			return Optional.of(base);
		}
	}

	@Nullable
	private CountryEntity determineCountry(@Nullable final String code) {
		return code == null || code.trim().isEmpty() ? null :
			countryRepository.findByCode(code).orElseGet(() -> countryRepository.save(new CountryEntity(code)));
	}

	private static String getLabel(Class<? extends AbstractAuditableBaseEntity> clazz) {
		return clazz.getSimpleName().replaceAll("Entity$", "");
	}
}
