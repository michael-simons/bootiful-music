MERGE (m:MusicVenue {name: 'Olympiahalle'})
SET m.location = point({latitude: 48.175, longitude: 11.55})
RETURN m;

MERGE (m:MusicVenue {name: 'Hanns-Martin-Schleyer-Halle'})
SET m.location = point({latitude: 48.793889, longitude: 9.226944})
RETURN m;

MERGE (m:MusicVenue {name: 'Wembley Arena'})
SET m.location = point({latitude: 51.558083, longitude: -0.282972})
RETURN m;

MERGE (m:MusicVenue {name: 'Wiener Stadthalle'})
SET m.location = point({latitude: 48.201944, longitude: 16.332778})
RETURN m;

MERGE (m:MusicVenue {name: 'Festhalle Frankfurt'})
SET m.location = point({latitude: 50.111667, longitude: 8.650833})
RETURN m;

MERGE (m:MusicVenue {name: "Sun City's Super Bowl"})
SET m.location = point({latitude: -25.340278, longitude: 27.090833})
RETURN m;

MERGE (m:MusicVenue {name: "City Of Rock"})
SET m.location = point({latitude: -22.999444, longitude: -43.365833})
RETURN m;
