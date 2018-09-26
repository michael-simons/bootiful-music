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

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Michael J. Simons
 */
@ExtendWith(SpringExtension.class)
@DataNeo4jTest
class ArtistRepositoryTest {
	private final Session session;

	private final ArtistRepository<? super ArtistEntity> artistRepository;

	@Autowired
	public ArtistRepositoryTest(Session session, ArtistRepository<? super ArtistEntity> artistRepository) {
		this.session = session;
		this.artistRepository = artistRepository;
	}

	@Test
	void markAsSpecificKindOfArtistShouldWork() {

		String artistName = "Queen";
		ArtistEntity unspecified = this.artistRepository.save(new ArtistEntity(artistName));
		BandEntity queen = this.artistRepository.markAsBand(unspecified);

		assertThat(queen).isNotNull();
		assertThat(this.artistRepository.findOneByName(artistName))
			.isPresent()
			.containsInstanceOf(BandEntity.class);
		assertThat(
			this.session.query(ArtistEntity.class, "MATCH (a:ArtistEntity {name: $name}) WHERE not a:BandEntity  RETURN a",
				Map.of("name", artistName))
				.iterator().hasNext()).isFalse();
	}

	@Test
	void changeKindShouldWork() {

		String artistName = "Jon Bon Jovi";
		BandEntity bonJovi = this.artistRepository.save(new BandEntity(artistName));
		SoloArtistEntity jonBonJovi = this.artistRepository.markAsSoloArtist(bonJovi);

		assertThat(jonBonJovi).isNotNull();
		assertThat(this.artistRepository.findOneByName(artistName))
			.isPresent()
			.containsInstanceOf(SoloArtistEntity.class);
		assertThat(
			this.session.query(
				ArtistEntity.class, "MATCH (a:ArtistEntity:BandEntity {name: $name}) RETURN a", Map.of("name", artistName))
				.iterator().hasNext()).isFalse();
	}
}