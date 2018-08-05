MATCH (t:Track) where t.name =~ ".*\\[feat\\..*\\]" 
MERGE (featuredArtist:Artist {name: apoc.text.regexGroups(t.name, ".*\\[feat\\. (.*)\\]")[0][1]})
MERGE (t) - [:FEATURING] -> (featuredArtist)