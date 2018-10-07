MATCH (ap:PlayCount) WITH sum(ap.value) as totalPlays 
MATCH (a:Artist) <- [:RELEASED_BY] - () - [:CONTAINS] -> (t:Track)
WITH DISTINCT  a, t, totalPlays 
MATCH (t) - [:HAS_BEEN_PLAYED] -> (p:PlayCount) 
WITH a, sum(p.value) as playCount, totalPlays
SET a.percentageOfAllPlays = 100.0/totalPlays * playCount
RETURN  a;