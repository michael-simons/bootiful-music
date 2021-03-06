/*
 * Copyright 201-20198 the original author or authors.
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
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.Repository;

/**
 * @author Michael J. Simons
 */
interface SoloArtistRepository extends Repository<SoloArtistEntity, Long> {

	SoloArtistEntity save(SoloArtistEntity soloArtistEntity);

	Optional<SoloArtistEntity> findById(Long id);

	List<SoloArtistEntity> findAll(Sort sort);
}
