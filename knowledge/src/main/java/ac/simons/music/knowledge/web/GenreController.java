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
package ac.simons.music.knowledge.web;

import static ac.simons.music.knowledge.web.AlbumController.mapAlbumEntities;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ac.simons.music.knowledge.domain.AlbumService;
import ac.simons.music.knowledge.domain.GenreEntity;
import ac.simons.music.knowledge.domain.GenreService;
import ac.simons.music.knowledge.domain.Microgenre;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * @author Michael J. Simons
 */
@Controller
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

	private final GenreService genreService;

	private final AlbumService albumService;

	@GetMapping(value = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView genres() {

		var model = genresModel();
		return new ModelAndView("genres", model);
	}

	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public Map<String, Object> genresModel() {

		var genres = this.genreService.findAllWithoutParent();
		var allMicrogenres = genreService.findAllMicrogrenes();
		var top10Microgenres = allMicrogenres.stream().sorted(comparingLong(Microgenre::getFrequency).reversed()).limit(10).collect(toList());
		return Map.of(
				"genres", genres,
				"microgenres", allMicrogenres,
				"top10Microgenres", top10Microgenres
		);
	}

	@GetMapping(value = "/new", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView genre() {
		return new ModelAndView("genre", Map.of("genreCmd", new GenreCmd()));
	}

	@GetMapping(value = "/{genreId}", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView genre(@PathVariable final Long genreId) {

		var genre = this.genreService.findById(genreId)
				.orElseThrow(() -> new NodeNotFoundException(GenreEntity.class, genreId));
		var possibleSubgenres = this.genreService.findAllPossibleSubgenres(genre);

		var model = Map.of(
				"genreCmd", new GenreCmd(genre),
				"subgenres", genre.getSubgenres(),
				"possibleSubgenres", possibleSubgenres,
				"albums", mapAlbumEntities(albumService.findAllAlbumsWithGenre(genre))
		);
		return new ModelAndView("genre", model);
	}

	@PostMapping(value = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
	public String genre(@Valid final GenreCmd genreForm, final BindingResult genreBindingResult) {
		if (genreBindingResult.hasErrors()) {
			return "genre";
		}

		var genre = Optional.ofNullable(genreForm.getId())
				.flatMap(genreService::findById)
				.map(existingGenre -> {
					existingGenre.setName(genreForm.getName());
					return existingGenre;
				}).orElseGet(() -> new GenreEntity(genreForm.getName()));
		genre = this.genreService.save(genre);

		return String.format("redirect:/genres/%d", genre.getId());
	}

	@ModelAttribute(name = "newSubgenreForm")
	NewSubGenreCmd newSubgenreForm() {
		return new NewSubGenreCmd();
	}

	@PostMapping(value = "/{genreId}/addSubgenre", produces = MediaType.TEXT_HTML_VALUE)
	public String associateWith(@PathVariable final Long genreId, @Valid final NewSubGenreCmd newSubGenreCmd, final BindingResult newSubGenreCmdBindingResult) {
		var genre = this.genreService.findById(genreId)
				.orElseThrow(() -> new NodeNotFoundException(GenreEntity.class, genreId));

		if (!newSubGenreCmdBindingResult.hasErrors()) {
			var newSubGenre = this.genreService.findById(newSubGenreCmd.genreId)
					.orElseThrow(() -> new NodeNotFoundException(GenreEntity.class, newSubGenreCmd.genreId));
			this.genreService.addSubgenre(genre, newSubGenre);
		}
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

		public boolean isNew() {
			return this.id == null;
		}
	}

	@Data
	static class NewSubGenreCmd {
		@NotNull
		private Long genreId;
	}
}
