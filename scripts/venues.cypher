MERGE (m:MusicVenue {name: 'Olympiahalle'})
SET m.location = point({latitude: 48.175, longitude: 11.55})
RETURN m;

MERGE (m:MusicVenue {name: 'Hanns-Martin-Schleyer-Halle'})
SET m.location = point({latitude: 48.793889, longitude: 9.226944})
RETURN m;

MERGE (m:MusicVenue {name: 'Wembley Arena'})
SET m.location = point({latitude: 51.558083, longitude: -0.282972})
RETURN m;

MERGE (m:MusicVenue {name: 'Wembley Stadium'})
SET m.location = point({latitude: 51.555556, longitude: -0.279722})
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

MERGE (m:MusicVenue {name: "Berliner Waldbühne"})
SET m.location = point({latitude: 52.515917, longitude: 13.229})
RETURN m;

MERGE (m:MusicVenue {name: "Maimarktgelände"})
SET m.location = point({latitude: 49.469167, longitude: 8.525556})
RETURN m;

MERGE (m:MusicVenue {name: "Müngersdorfer Stadion"})
SET m.location = point({latitude: 50.933497, longitude: 6.874997})
RETURN m;




MATCH (a:Artist {name: 'Queen'})
MATCH (y:Year {value: 1984})

MERGE (a) - [:WAS_ON] -> (t:Tour {name: "The Works Tour"}) - [:STARTED_IN] -> (y)
WITH t

MATCH (o:MusicVenue {name: 'Olympiahalle'})
MATCH (f:MusicVenue {name: 'Festhalle Frankfurt'})
MATCH (w:MusicVenue {name: 'Wiener Stadthalle'})
MATCH (h:MusicVenue {name: 'Hanns-Martin-Schleyer-Halle'})

MERGE (t) - [:HAD_PART_OF_ITINERARY {visitedAt: date('1984-09-16')}] -> (o)
MERGE (t) - [:HAD_PART_OF_ITINERARY {visitedAt: date('1984-09-26')}] -> (f)
MERGE (t) - [:HAD_PART_OF_ITINERARY {visitedAt: date('1984-09-29')}] -> (w)
MERGE (t) - [:HAD_PART_OF_ITINERARY {visitedAt: date('1984-09-30')}] -> (w)
MERGE (t) - [:HAD_PART_OF_ITINERARY {visitedAt: date('1984-09-27')}] -> (h)

RETURN *



MATCH (a:Artist {name: 'Queen'})
MATCH (y:Year {value: 1986})
MERGE (a) - [:WAS_ON] -> (t:Tour {name: "Magic Tour"}) - [:STARTED_IN] -> (y)
WITH t

MATCH (o:MusicVenue {name: 'Berliner Waldbühne'})
MATCH (f:MusicVenue {name: 'Maimarktgelände'})
MATCH (w:MusicVenue {name: 'Müngersdorfer Stadion'})
MATCH (h:MusicVenue {name: 'Wembley Stadium'})



MERGE (t) - [:HAD_PART_OF_ITINERARY {visitedAt: date('1986-06-26')}] -> (o)
MERGE (t) - [:HAD_PART_OF_ITINERARY {visitedAt: date('1986-06-21')}] -> (f)
MERGE (t) - [:HAD_PART_OF_ITINERARY {visitedAt: date('1986-07-19')}] -> (w)
MERGE (t) - [:HAD_PART_OF_ITINERARY {visitedAt: date('1986-07-11')}] -> (h)
MERGE (t) - [:HAD_PART_OF_ITINERARY {visitedAt: date('1986-07-12')}] -> (h)


return *

