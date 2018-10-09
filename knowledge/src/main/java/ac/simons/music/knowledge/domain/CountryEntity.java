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
package ac.simons.music.knowledge.domain;

import java.util.Locale;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

import ac.simons.music.knowledge.support.AbstractAuditableBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Country")
@Getter
@EqualsAndHashCode(of = "code", callSuper = false)
public class CountryEntity extends AbstractAuditableBaseEntity {

	@Index(unique = true)
	private String code;

	private String name;

	public CountryEntity(String code) {
		this.code = code;
		this.name = new Locale("", this.code).getDisplayCountry(Locale.ENGLISH);
	}
}
