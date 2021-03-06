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

import java.util.List;

import org.springframework.data.neo4j.annotation.QueryResult;

/**
 * @author Michael J. Simons
 */
@QueryResult
@Data
public class Microgenre {
	private String name;

	private Long frequency;

	private List<AlbumWithMicrogenre> albums;

	@Data
	static class AlbumWithMicrogenre {
		private String name;

		private Long frequency;
	}
}
