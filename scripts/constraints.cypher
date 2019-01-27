DROP CONSTRAINT ON (a:Artist) ASSERT a.name IS UNIQUE
CREATE CONSTRAINT ON (a:Artist) ASSERT a.name IS UNIQUE
DROP CONSTRAINT ON (y:Year) ASSERT y.value IS UNIQUE
CREATE CONSTRAINT ON (y:Year) ASSERT y.value IS UNIQUE
DROP CONSTRAINT ON (d:Decade) ASSERT d.value IS UNIQUE
CREATE CONSTRAINT ON (d:Decade) ASSERT d.value IS UNIQUE
DROP CONSTRAINT ON (c:Country) ASSERT c.code IS UNIQUE
CREATE CONSTRAINT ON (c:Country) ASSERT c.code IS UNIQUE
DROP CONSTRAINT ON (g:Genre) ASSERT g.name IS UNIQUE
CREATE CONSTRAINT ON (g:Genre) ASSERT g.name IS UNIQUE

DROP CONSTRAINT ON (c:Cluster) ASSERT c.name IS UNIQUE
CREATE CONSTRAINT ON (c:Cluster) ASSERT c.name IS UNIQUE

DROP INDEX ON :Album(name)
CREATE INDEX ON :Album(name)

DROP INDEX ON :MusicVenue(location)
CREATE INDEX ON :MusicVenue(location)

// Enterprise Feature
DROP CONSTRAINT ON ()-[playCount:HAS_BEEN_PLAYED_IN]-() ASSERT exists(playCount.value)
CREATE CONSTRAINT ON ()-[playCount:HAS_BEEN_PLAYED_IN]-() ASSERT exists(playCount.value)
