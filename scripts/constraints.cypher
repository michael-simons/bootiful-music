CREATE CONSTRAINT ON (artist:Artist) ASSERT artist.name IS UNIQUE

// Enterprise Feature
// CREATE CONSTRAINT ON ()-[playCount:HAS_BEEN_PLAYED_IN]-() ASSERT exists(playCount.value)
