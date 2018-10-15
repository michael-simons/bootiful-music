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
package ac.simons.music.knowledge.config;

import static org.neo4j.ogm.driver.ParameterConversionMode.*;

import java.util.Optional;

import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Michael J. Simons
 */
@Configuration
public class Neo4jConfiguration {
	@Bean
	public org.neo4j.ogm.config.Configuration configuration(Neo4jProperties properties) {
		var builder = new org.neo4j.ogm.config.Configuration.Builder();
		builder.uri(Optional.ofNullable(properties.getUri()).orElse("bolt://localhost:7687"));
		builder.credentials(properties.getUsername(), properties.getPassword());
		builder.autoIndex(properties.getAutoIndex().getName());
		builder.withCustomProperty(CONFIG_PARAMETER_CONVERSION_MODE, CONVERT_NON_NATIVE_ONLY);
		return builder.build();
	}
}
