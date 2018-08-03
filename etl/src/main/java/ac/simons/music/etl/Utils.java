package ac.simons.music.etl;

import java.util.Locale;
import java.util.stream.Stream;

import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

public class Utils {

	/**
	 * This method can be used to create nodes for all countries known to Java with<br>
	 * <code>
	 * CALL utils.getCountries() YIELD code, name
	 * MERGE (:Country {code: code, name: name})
	 * </code>
	 *
	 * @return
	 */
	@Procedure(name = "utils.getCountries", mode = Mode.READ)
	public Stream<Country> getCountries() {
		return Stream.concat(
			Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream(),
			Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3).stream())
			.map(Country::new);
	}

	// Returned classes must be public and so must be there fields.
	public static class Country {
		public final String code;

		public final String name;

		public Country(String code) {
			this.code = code;
			this.name = new Locale("", this.code).getDisplayCountry(Locale.ENGLISH);
		}
	}
}
