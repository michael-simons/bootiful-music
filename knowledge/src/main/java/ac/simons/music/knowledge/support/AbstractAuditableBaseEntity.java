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

import ac.simons.music.knowledge.support.NoOpLocalDateTimeConversion;
import lombok.Getter;

import java.time.LocalDateTime;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * Base class for our entities.
 *
 * @author Michael J. Simons
 */
@Getter
public abstract class AbstractAuditableBaseEntity {
	@Id
	@GeneratedValue
	private Long id;

	@CreatedDate
	@Convert(NoOpLocalDateTimeConversion.class)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Convert(NoOpLocalDateTimeConversion.class)
	private LocalDateTime updatedAt;
}
