MATCH (a:Album) -[:RELEASED_BY]->(:Artist {name: 'Queen'}) 
WHERE a.name IN ['Return Of The Champions', 'Queen Rock Montreal', 'The Cosmos Rocks', "Live At The Rainbow '74", 'Live Killers'] 
SET a.live = true