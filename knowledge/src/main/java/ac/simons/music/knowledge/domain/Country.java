/*
 * Copyright (c) 2018 michael-simons.eu.
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

import java.util.Locale;
import java.util.Objects;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.PostLoad;
import org.neo4j.ogm.annotation.Transient;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class Country {
	private Long id;

	private String code;

	private String name;

	public Country(String code) {
		this.code = code;
		this.name = new Locale("", this.code).getDisplayCountry(Locale.ENGLISH);
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Country))
			return false;
		Country country = (Country) o;
		return Objects.equals(code, country.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code);
	}
}
