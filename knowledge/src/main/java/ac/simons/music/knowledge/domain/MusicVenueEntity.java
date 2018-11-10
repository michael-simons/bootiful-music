package ac.simons.music.knowledge.domain;

import ac.simons.music.knowledge.support.AbstractAuditableBaseEntity;
import ac.simons.music.knowledge.support.NativePointConverter;
import lombok.Getter;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.springframework.data.geo.Point;

/**
 * @author Michael J. Simons
 */
@NodeEntity("MusicVenue")
@Getter
public class MusicVenueEntity extends AbstractAuditableBaseEntity {
	@Index
	private String name;

	@Convert(NativePointConverter.class)
	private Point location;

	public MusicVenueEntity(String name, Point location) {
		this.name = name;
		this.location = location;
	}
}
