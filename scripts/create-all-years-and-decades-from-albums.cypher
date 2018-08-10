MATCH (a:Album) WITH DISTINCT a.releasedIn as year
MERGE (d:Decade {value: (year - year%10) })
MERGE (:Year {value: year}) - [:PART_OF] -> (d);


MATCH (a:Album) WITH a
MATCH (y:Year {value: a.releasedIn})
MERGE (a) - [:RELEASED_IN] -> (y)


MATCH (a:Album) - [:RELEASED_IN] - (year) - [:PART_OF] -> (:Decade {value: 1970}) RETURN a.name, year.value as year ORDER BY year asc


MATCH(:SoloArtist {name: 'Bela B.'}) - [:IS_A] -> (:Member) -- (b:Band) RETURN b.name