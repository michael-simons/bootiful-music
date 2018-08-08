MATCH (a:Album) -[:RELEASED_BY]->(:Artist {name: 'Queen'}) 
WHERE a.name IN ['Return Of The Champions', 'Queen Rock Montreal', 'The Cosmos Rocks', "Live At The Rainbow '74", 'Live Killers'] 
SET a.live = true

MATCH (studio:Album)-[:RELEASED_BY]->(:Artist {name: 'Queen'}) WHERE studio.live is null or not studio.live 
MATCH (:Album {live: true})-[:CONTAINS]->(t:Track)<-[:CONTAINS]-(studio)
RETURN studio, COUNT (t) as rank
ORDER BY rank desc