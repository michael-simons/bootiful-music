/*
 * Copyright 2016-2018 the original author or authors.
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
package ac.simons.music.charts.web;

import ac.simons.music.charts.domain.GenreRepository;
import ac.simons.music.charts.domain.GenreWithPlaycount;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael J. Simons
 */
@RestController
@RequestMapping("/api/genres")
public class GenreApiController {
	private final GenreRepository genreRepository;

	public GenreApiController(GenreRepository genreRepository) {
		this.genreRepository = genreRepository;
	}

	@GetMapping("/playcounts")
	public List<GenreWithPlaycount> getPlaycounts() {
		return this.genreRepository
			.findAllWithPlaycount();
	}
}
