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
package ac.simons.music.knowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.neo4j.Neo4jHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.neo4j.annotation.EnableNeo4jAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Michael J. Simons
 */
@SpringBootApplication(exclude = Neo4jHealthContributorAutoConfiguration.class) // The driver starter has a better health endpoint
@EnableNeo4jAuditing
@EnableAsync
@EntityScan(basePackages = "ac.simons.music.knowledge.domain")
public class KnowledgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnowledgeApplication.class, args);
	}
}
