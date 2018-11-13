MATCH (c:Country {code: 'DE'})
MERGE (m:MusicVenue {name: 'Olympiahalle'})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: 48.175, longitude: 11.55})
RETURN *

MATCH (c:Country {code: 'DE'})
MERGE (m:MusicVenue {name: 'Hanns-Martin-Schleyer-Halle'})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: 48.793889, longitude: 9.226944})
RETURN *

MATCH (c:Country {code: 'GB'})
MERGE (m:MusicVenue {name: 'Wembley Arena'})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: 51.558083, longitude: -0.282972})
RETURN *

MATCH (c:Country {code: 'GB'})
MERGE (m:MusicVenue {name: 'Wembley Stadium'})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: 51.555556, longitude: -0.279722})
RETURN *

MATCH (c:Country {code: 'AT'})
MERGE (m:MusicVenue {name: 'Wiener Stadthalle'})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: 48.201944, longitude: 16.332778})
RETURN *

MATCH (c:Country {code: 'DE'})
MERGE (m:MusicVenue {name: 'Festhalle Frankfurt'})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: 50.111667, longitude: 8.650833})
RETURN *

MERGE (c:Country {code: 'ZA', name: 'South Africa'})
MERGE (m:MusicVenue {name: "Sun City's Super Bowl"})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: -25.340278, longitude: 27.090833})
RETURN *

MERGE (c:Country {code: 'BR', name: 'Brazil'})
MERGE (m:MusicVenue {name: "City Of Rock"})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: -22.999444, longitude: -43.365833})
RETURN *

MATCH (c:Country {code: 'DE'})
MERGE (m:MusicVenue {name: "Berliner Waldbühne"})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: 52.515917, longitude: 13.229})
RETURN *

MATCH (c:Country {code: 'DE'})
MERGE (m:MusicVenue {name: "Maimarktgelände"})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: 49.469167, longitude: 8.525556})
RETURN *

MATCH (c:Country {code: 'DE'})
MERGE (m:MusicVenue {name: "Müngersdorfer Stadion"})
MERGE (m) - [:IS_LOCATED_IN] -> (c)
SET m.location = point({latitude: 50.933497, longitude: 6.874997})
RETURN *




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




MERGE (c:Country {code: 'US', name: 'United States'})
MERGE (c:Country {code: 'DE', name: 'Germany'})


MATCH (c:Country {code: 'US'}) WITH c
WITH c, "https://app.ticketmaster.com/discovery/v2/venues.json?countryCode=" + c.code + "&apikey=xxx" as url
CALL apoc.load.json(url) YIELD value
UNWIND value._embedded.venues AS item
WITH c, item
WHERE item.location IS NOT NULL
MERGE (m:MusicVenue {name: item.name, location: point({latitude: toFloat(item.location.latitude), longitude: toFloat(item.location.longitude)})})
ON CREATE SET m.createdAt = localdatetime()
ON MATCH SET m.updatedAt = localdatetime()
MERGE (m) - [:IS_LOCATED_IN] -> (c)

MATCH (c:Country {code: 'US'}) WITH c
WITH c, range(0,30) as pages
UNWIND pages as page
WITH c, "https://app.ticketmaster.com/discovery/v2/venues.json?page=" + page + "&countryCode=" + c.code + "&apikey=xxx" as url
CALL apoc.load.json(url) YIELD value
UNWIND value._embedded.venues AS item
WITH c, item
WHERE item.location IS NOT NULL
MERGE (m:MusicVenue {name: item.name, location: point({latitude: toFloat(item.location.latitude), longitude: toFloat(item.location.longitude)})})
ON CREATE SET m.createdAt = localdatetime()
ON MATCH SET m.updatedAt = localdatetime()
MERGE (m) - [:IS_LOCATED_IN] -> (c)