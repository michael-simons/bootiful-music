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

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class SoloArtist extends AbstractArtist {

	@Relationship(type = "PLAYED_IN")
	private List<PlayedIn> playedIn = new ArrayList<>();

	public SoloArtist(String name) {
		super(name);
	}

	public SoloArtist playedIn(Band band, Year from, Year to) {
		this.playedIn.add(new PlayedIn(this, band, List.of(from + "-" + to)));
		return this;
	}
}
