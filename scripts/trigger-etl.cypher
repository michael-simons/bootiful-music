call stats.loadArtistData('statsdb-dev', 'dev', 'jdbc:postgresql://statsdb:5432/bootiful-music?currentSchema=dev')
call stats.loadAlbumData('statsdb-dev', 'dev', 'jdbc:postgresql://statsdb:5432/bootiful-music?currentSchema=dev')
call stats.loadPlayCounts('statsdb-dev', 'dev', 'jdbc:postgresql://statsdb:5432/bootiful-music?currentSchema=dev')