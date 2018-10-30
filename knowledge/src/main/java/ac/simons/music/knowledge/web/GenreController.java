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
package ac.simons.music.knowledge.web;

import static ac.simons.music.knowledge.web.AlbumController.mapAlbumEntities;

import ac.simons.music.knowledge.domain.AlbumService;
import ac.simons.music.knowledge.domain.GenreEntity;
import ac.simons.music.knowledge.domain.GenreRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael J. Simons
 */
@Controller
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

	private final GenreRepository genreRepository;

	private final AlbumService albumService;

	@GetMapping(value = { "", "/" }, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView genres() {

		var genres = this.genreRepository.findAll(Sort.by("name").ascending());
		return new ModelAndView("genres", Map.of("genres", genres));
	}

	@GetMapping(value = "/new", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView genre() {
		return new ModelAndView("genre", Map.of("genreCmd", new GenreCmd()));
	}

	@GetMapping(value = "/{genreId}", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView genre(@PathVariable final Long genreId) {

		var genre = this.genreRepository.findById(genreId)
			.orElseThrow(() -> new NodeNotFoundException(GenreEntity.class, genreId));

		var model = Map.of(
			"genreCmd", new GenreCmd(genre),
			"genre", genre,
			"albums", mapAlbumEntities(albumService.findAllAlbumsWithGenre(genre))
		);
		return new ModelAndView("genre", model);
	}

	@PostMapping(value = { "", "/" }, produces = MediaType.TEXT_HTML_VALUE)
	public String genre(@Valid final GenreCmd genreForm, final BindingResult genreBindingResult) {
		if (genreBindingResult.hasErrors()) {
			return "genre";
		}

		var genre = Optional.ofNullable(genreForm.getId())
			.flatMap(genreRepository::findById)
			.map(existingGenre -> {
				existingGenre.setName(genreForm.getName());
				return existingGenre;
			}).orElseGet(() -> new GenreEntity(genreForm.getName()));
		genre = this.genreRepository.save(genre);

		return String.format("redirect:/genres/%d", genre.getId());
	}

	@Data
	@NoArgsConstructor
	static class GenreCmd {

		private Long id;

		@NotBlank
		private String name;

		GenreCmd(GenreEntity genreEntity) {
			this.id = genreEntity.getId();
			this.name = genreEntity.getName();
		}
	}
}
