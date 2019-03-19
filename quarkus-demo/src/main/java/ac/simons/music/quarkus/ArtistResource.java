package ac.simons.music.quarkus;

import ac.simons.music.quarkus.domain.ArtistEntity;
import ac.simons.music.quarkus.domain.ArtistService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/artists")
public class ArtistResource {

	@Inject
	ArtistService artistService;

	@GET
	@Path("/by-name/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Iterable<ArtistEntity> byName(@PathParam("name") String name) {
		return artistService.findByName(name);
	}
}