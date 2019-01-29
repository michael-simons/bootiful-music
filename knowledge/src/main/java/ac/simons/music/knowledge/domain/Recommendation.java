/*
 * Copyright 2019 the original author or authors.
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

/**
 * @author Michael J. Simons
 */
@Data
public class Recommendation {
	private final ArtistEntity recommendedArtist;

	private final String reason;

	public Recommendation(ArtistEntity recommendedArtist) {
		this(recommendedArtist, "");
	}

	public Recommendation(ArtistEntity recommendedArtist, String reason) {
		this.recommendedArtist = recommendedArtist;
		this.reason = reason;
	}
}
