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

import static java.util.Collections.*;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ac.simons.music.knowledge.domain.MusicVenueEntity;
import ac.simons.music.knowledge.domain.MusicVenueRepository;
import lombok.RequiredArgsConstructor;

/**
 * @author Michael J. Simons
 */
@Controller
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

	private final MusicVenueRepository musicVenueRepository;

	@GetMapping(value = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView venues() {
		return new ModelAndView("venues", emptyMap());
	}

	@GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public List<MusicVenueEntity> venuesAround(
			@RequestParam double latitude,
			@RequestParam double longitude,
			@RequestParam double distanceInMeter
	) {
		return musicVenueRepository.findAllByLocationNear(latitude, longitude, distanceInMeter);
	}

	@GetMapping(value = {"/by-tour"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public List<MusicVenueEntity> byTour(
			@RequestParam long tourId
	) {
		return musicVenueRepository.findAllByTour(tourId);
	}
}
