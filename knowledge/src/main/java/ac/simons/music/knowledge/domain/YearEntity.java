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

import java.time.Year;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.lang.Nullable;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Year")
public class YearEntity {

	private Long id;

	@Index(unique = true)
	private long value;

	@Relationship("IS_PART_OF")
	private DecadeEntity decade;

	YearEntity(final DecadeEntity decade, final long value) {
		this.decade = decade;
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public DecadeEntity getDecade() {
		return decade;
	}

	public long getValue() {
		return value;
	}
}
