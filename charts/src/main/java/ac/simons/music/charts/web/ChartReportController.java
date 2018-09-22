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

import java.io.IOException;
import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ac.simons.music.charts.domain.ChartService;

/**
 * @author Michael J. Simons
 */
@Controller
public class ChartReportController {

	private final ChartService chartService;

	public ChartReportController(ChartService chartService) {
		this.chartService = chartService;
	}

	@GetMapping(value = {"/", "/charts/{year}/{month}"}, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView charts(
			@PathVariable final Optional<Integer> year,
			@PathVariable final Optional<Integer> month,
			@RequestParam(defaultValue = "10") final int n
	) {

		final YearMonth yearMonth = getYearMonth(year, month);
		final String charts = this.chartService.getCharts(yearMonth, n).formatJSON();
		return new ModelAndView("index", Map.of("charts", charts, "yearMonth", yearMonth));
	}

	@GetMapping(value = {"/", "/charts/{year}/{month}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void chartsApi(
			@PathVariable final Optional<Integer> year,
			@PathVariable final Optional<Integer> month,
			@RequestParam(defaultValue = "10") final int n,
			final HttpServletResponse response
	) throws IOException {

		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		this.chartService.getCharts(getYearMonth(year, month), n).formatJSON(response.getOutputStream());
	}

	private static YearMonth getYearMonth(@PathVariable Optional<Integer> year, @PathVariable Optional<Integer> month) {
		return year.isPresent() && month.isPresent() ? YearMonth.of(year.get(), month.get()) : YearMonth.now();
	}
}
