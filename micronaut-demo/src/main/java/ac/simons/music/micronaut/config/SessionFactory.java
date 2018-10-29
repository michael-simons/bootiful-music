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
package ac.simons.music.micronaut.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.context.scope.ThreadLocal;

import org.neo4j.driver.v1.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;

/**
 * @author Michael J. Simons
 */
@Factory
public class SessionFactory {
	private final org.neo4j.ogm.session.SessionFactory delegate;

	public SessionFactory(Driver driver) {
		this.delegate = new org.neo4j.ogm.session.SessionFactory(
			new BoltDriver(driver), "ac.simons.music.micronaut.domain");
	}

	@Bean
	@ThreadLocal
	public Session session() {
		return delegate.openSession();
	}
}
