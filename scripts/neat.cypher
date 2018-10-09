MATCH (t)-[r:HAS_BEEN_PLAYED_IN|OF*2]-> (y:Year)
WITH  t, y, sum(r[0].value) as x
WHERE x > 10
MATCH (t)<-[]-(a:Album)
RETURN y.value, a.name
ORDER BY y.value desc
