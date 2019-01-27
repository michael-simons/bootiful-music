// Based on ideas of https://musicmap.info

// Clusters
CREATE (c:Cluster {name: 'Heavy Music'})
CREATE (c:Cluster {name: 'Rock'})
CREATE (c:Cluster {name: 'Pop & Rock Music'})
CREATE (c:Cluster {name: 'Rhythm Music'})
CREATE (c:Cluster {name: 'Blue Note'})
CREATE (c:Cluster {name: 'Electronic Music'})
CREATE (c:Cluster {name: 'Breakbeat Dance'})
CREATE (c:Cluster {name: 'Four-On-The-Floor Dance'})
CREATE (c:Cluster {name: '(Electronic) Dance (Music) / EDM'})

MERGE (g:Genre {name: 'Punk Rock / New Wave'})

match (h:Genre)
where h.name in ['Industrial', 'Heavy Metal', 'Hardcore Punk', 'Punk Rock / New Wave']
with h
match (c:Cluster {name: 'Heavy Music'})
merge (h) - [s:IS_PART_OF] -> (c)
return *

match (h:Genre {name: 'Heavy Metal'})
match (g:Genre) where g.name =~ '.*Metal.*' and g.name <> 'Heavy Metal'
merge (g) - [s:IS_SUBGENRE_OF] -> (h)
return *


match (h:Genre {name: 'Hardcore Punk'})
match (g:Genre) 
where g.name in ['Grindcore']
merge (g) - [s:IS_SUBGENRE_OF] -> (h)
return *


match (h:Genre {name: 'Punk Rock / New Wave'})
match (g:Genre) where g.name in ['Punk Rock', 'New Wave']
merge (g) - [s:IS_SUBGENRE_OF] -> (h)
return *