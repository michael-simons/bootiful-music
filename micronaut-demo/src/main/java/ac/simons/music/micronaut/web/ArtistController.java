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
package ac.simons.music.micronaut.web;

import ac.simons.music.micronaut.domain.ArtistService;
import ac.simons.music.micronaut.domain.ArtistService.DefaultArtistService;
import ac.simons.music.micronaut.domain.ArtistEntity;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;

/**
 * @author Michael J. Simons
 */
@Controller("/artists")
public class ArtistController {

	private final ArtistService artistService;

	ArtistController(@DefaultArtistService ArtistService artistService) {
		this.artistService = artistService;
	}

	@Get("/by-name")
	@Produces(MediaType.APPLICATION_JSON)
	public Iterable<ArtistEntity> index(@QueryValue String name) {
		return artistService.findByName(name);
	}
}