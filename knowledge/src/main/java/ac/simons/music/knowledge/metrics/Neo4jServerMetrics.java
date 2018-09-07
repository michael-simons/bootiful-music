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
package ac.simons.music.knowledge.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.Map;
import java.util.function.ToDoubleFunction;

import org.neo4j.ogm.session.Session;
import org.springframework.stereotype.Component;

/**
 * @author Michael J. Simons
 */
@Component
public class Neo4jServerMetrics implements MeterBinder {

	private final Session session;

	Neo4jServerMetrics(Session session) {
		this.session = session;
	}

	@Override
	public void bindTo(MeterRegistry registry) {

		ToDoubleFunction<Session> nodeCount = session -> session
			.queryForObject(Double.class, "MATCH (n) RETURN COUNT(*)", Map.of());

		ToDoubleFunction<Session> storeSize = session -> retrieveSysInfo(session, "Store sizes", "TotalStoreSize");

		Gauge.builder("neo4j.nodes.count", session, nodeCount)
			.description("Number of nodes in the graph")
			.baseUnit("nodes")
			.register(registry);

		Gauge.builder("neo4j.store.size", session, storeSize)
			.description("Size of all stores")
			.baseUnit("bytes")
			.register(registry);
	}

	private static double retrieveSysInfo(Session session, String group, String attribute) {
		var query
			= "call dbms.queryJmx($jmxQuery) yield attributes\n"
			+ "with keys(attributes) as k, attributes\n"
			+ "unwind k as row \n"
			+ "with row, attributes\n"
			+ "where row = $attribute\n"
			+ "return attributes[row][\"value\"]";

		var jmxQuery = String.format("org.neo4j:instance=kernel#0,name=%s", group);
		return session.queryForObject(Double.class, query, Map.of("jmxQuery", jmxQuery, "attribute", attribute));
	}
}
