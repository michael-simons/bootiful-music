/*
 * Copyright 2018-2019 the original author or authors.
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

import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.Repository;

/**
 * @author Michael J. Simons
 */
public interface GenreRepository extends Repository<GenreEntity, Long> {

	GenreEntity save(GenreEntity genre);

	Optional<GenreEntity> findById(Long genreId);

	List<GenreEntity> findAll(Sort sortOrder);

	@Query(value
		= " MATCH (a:Album)-[:CONTAINS]->(track)-[pc:HAS_BEEN_PLAYED_IN]->(),"
		+ "       (a)-[:RELEASED_BY]->(artist)"
		+ "  WITH artist, a, sum(pc.value) AS frequencyAlbum"
		+ " MATCH (a)-[:HAS]->(g:Genre),"
		+ "       (a)-[:RELEASED_IN]->(:Year)-[:PART_OF]->(d:Decade),"
		+ "       (artist)-[:FOUNDED_IN|:BORN_IN]->(c:Country)"
		+ "  WITH g, d, c, "
		+ "       sum(frequencyAlbum) AS frequencyGenre, "
		+ "       collect({name: artist.name + ': '  + a.name, frequency: frequencyAlbum}) AS albums"
		+ "  RETURN d.value + ' ' + c.name + '  ' + g.name  AS name,"
		+ "         frequencyGenre as frequency,"
		+ "         albums"
	)
	List<Subgenre> findAllSubgrenes();
}
