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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons
 */
@ExtendWith(SpringExtension.class)
@DataNeo4jTest
@Rollback(false)
public class ArtistRepositoryTest {
	private final Session session;

	private final ArtistRepository<? super Artist> artistRepository;

	private final ArtistRepository<Band> bands;

	private final ArtistRepository<SoloArtist> soloArtists;

	@Autowired
	public ArtistRepositoryTest(Session session,
		ArtistRepository<? super Artist> artistRepository,
		ArtistRepository<Band> bands,
		ArtistRepository<SoloArtist> soloArtists) {
		this.session = session;
		this.artistRepository = artistRepository;
		this.bands = bands;
		this.soloArtists = soloArtists;
	}

	@Test
	void markAsSpecificKindOfArtistShouldWork() {
		Artist unspecified = this.artistRepository.save(new Artist("Queen"));

		List<Artist> hlp = new ArrayList<>();
		hlp.clear();
		this.session.query(Artist.class, "MATCH (a:Artist {name: 'Queen'}) WHERE not a:Band RETURN a", Map.of())
			.iterator().forEachRemaining(hlp::add);
		assertThat(hlp).hasSize(1).extracting(Artist::getName).contains("Queen");

		Band queen = this.artistRepository.markAsBand(unspecified);

		hlp.clear();
		this.session.query(Artist.class, "MATCH (a:Artist {name: 'Queen'}) WHERE not a:Band RETURN a", Map.of())
			.iterator().forEachRemaining(hlp::add);
		assertThat(hlp).isEmpty();

/*
Does not work at the moment due to a possible OGM bug in LoadOneDelegate, have to investigate that.

// queen is null when running as @DataNeo4jTest
//  o.n.o.session.delegates.LoadOneDelegate  : Could not cast entity ac.simons.music.knowledge.domain.Artist@4ac2729 for id 79 to class ac.simons.music.knowledge.domain.Band
// queen is correct when run as SpringBootTest... ??!



		System.out.println(this.bands.findOneByName("Queen").get().getFoundedIn());
		Assertions.assertThat(this.bands.findOneByName("Queen")).isPresent().containsInstanceOf(Band.class);

		System.out.println(queen.getFoundedIn());
		*/
	}
}