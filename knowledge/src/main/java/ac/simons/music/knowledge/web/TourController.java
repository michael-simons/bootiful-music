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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ac.simons.music.knowledge.domain.TourEntity;
import ac.simons.music.knowledge.domain.TourRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author Michael J. Simons
 */
@Controller
@RequestMapping("/tours")
@RequiredArgsConstructor
public class TourController {

	private final TourRepository tourRepository;

	@GetMapping(value = "/{tourId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public TourItinerary tour(@PathVariable final Long tourId) {
		TourEntity tourEntity = tourRepository.findById(tourId)
				.orElseThrow(() -> new NodeNotFoundException(TourEntity.class, tourId));

		var venues = tourEntity.getItinerary().stream()
				.sorted(comparing(TourEntity.PlayedIn::getVisitedAt))
				.map(p -> new Venue(p.getVenue().getName(), p.getVisitedAt(), p.getVenue().getLocation()))
				.collect(toList());

		return new TourItinerary(tourEntity.getName(), venues);
	}

	@Data
	static class TourItinerary {
		private final String name;

		private final List<Venue> venues;
	}

	@Data
	static class Venue {
		private final String name;

		private final LocalDate visitedAt;

		private final Point location;

		public String getLabel() {
			return String.format("%s (%s)", this.name, this.visitedAt.toString());
		}
	}
}
