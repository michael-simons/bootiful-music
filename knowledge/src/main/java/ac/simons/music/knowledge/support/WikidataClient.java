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
package ac.simons.music.knowledge.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Michael J. Simons
 */
@Component
public class WikidataClient {

	public static final String ENTRY_SITELINKS = "sitelinks";
	public static final String ENTRY_ENTITIES = "entities";

	public static final String SUFFIX_WIKILINKS = "wiki";
	private final RestTemplate restTemplate;
	private final HttpHeaders defaultHeaders;

	public WikidataClient(final RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();

		this.defaultHeaders = new HttpHeaders();
		this.defaultHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
	}

	@Async
	public CompletableFuture<LinkResult> retrieveWikipediaLinks(final String entityId) {
		LinkResult linkResult;
		if(entityId == null || entityId.isEmpty()) {
			linkResult = new LinkResult(entityId, List.of());
		} else {
			var entityDataUrl = "https://www.wikidata.org/wiki/Special:EntityData/" + entityId;
			var response = restTemplate
				.exchange(entityDataUrl, HttpMethod.GET, new HttpEntity<>(defaultHeaders), JsonNode.class);
			var entityData = response.getBody();

			var links =
				StreamSupport
					.stream(entityData.get(ENTRY_ENTITIES).get(entityId).get(ENTRY_SITELINKS).spliterator(), false)
					.filter(n -> n.has("site") && n.get("site").asText().endsWith("wiki"))
					.map(n -> {
						var site = n.get("site").asText();
						return Map.of(
							"country", site.replaceAll(SUFFIX_WIKILINKS, ""),
							"site", site,
							"title", n.get("title").asText(),
							"url", n.get("url").asText());
					})
					.collect(Collectors.toList());
			linkResult = new LinkResult(entityId, links);
		}

		return CompletableFuture.completedFuture(linkResult);
	}

	@RequiredArgsConstructor
	@Getter
	public static class LinkResult {
		private final String entityId;

		private final List<Map<String, String>> links;

		public List<Map<String, String>> getLinks() {
			return Collections.unmodifiableList(links);
		}
	}
}
