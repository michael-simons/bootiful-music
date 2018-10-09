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

import lombok.Data;
import lombok.ToString;

import org.springframework.data.neo4j.annotation.QueryResult;

/**
 * This is a value object, representing a {@link TrackEntity track} contained on an {@link AlbumEntity album}.
 * The relationship is expressed inside the database as <pre>(:Album) - [:CONTAINS] -> (:Track)</pre>.
 *
 * @author Michael J. Simons
 */
@QueryResult
@Data
@ToString(of = { "discNumber", "trackNumber", "track" })
public class AlbumTrack {

	private Long id;

	private String name;

	private Long discNumber;

	private Long trackNumber;
}
