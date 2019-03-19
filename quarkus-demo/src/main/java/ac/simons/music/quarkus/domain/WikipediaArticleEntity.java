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
package ac.simons.music.quarkus.domain;

/**
 * @author Michael J. Simons
 */
public class WikipediaArticleEntity implements Comparable<WikipediaArticleEntity> {

	private String site;

	private String title;

	private String url;

	WikipediaArticleEntity() {
	}

	public WikipediaArticleEntity(String site, String title, String url) {
		this.site = site;
		this.title = title;
		this.url = url;
	}

	@Override
	public int compareTo(WikipediaArticleEntity o) {
		return this.site.compareTo(o.site);
	}

	public String getSite() {
		return this.site;
	}

	public String getTitle() {
		return this.title;
	}

	public String getUrl() {
		return this.url;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
