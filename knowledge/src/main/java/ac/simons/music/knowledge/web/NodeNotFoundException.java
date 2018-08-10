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
package ac.simons.music.knowledge.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown by controllers when arbitrary nodes could not be retrieved from their repositories or services.
 *
 * @author Michael J. Simons
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NodeNotFoundException extends RuntimeException {
	private final Class<?> nodeClass;

	private final Long nodeId;

	public NodeNotFoundException(Class<?> nodeClass, Long nodeId) {
		super(String.format("Node of type '%s' with id %d not found", nodeClass, nodeId));
		this.nodeClass = nodeClass;
		this.nodeId = nodeId;
	}

	public Class<?> getNodeClass() {
		return nodeClass;
	}

	public Long getNodeId() {
		return nodeId;
	}
}
