-- Clean up
DELETE FROM plays WHERE user_id != 1;
DELETE FROM plays WHERE DATE(played_on) NOT BETWEEN DATE('2008-10-01') AND DATE('2018-09-30');

DELETE FROM tracks WHERE NOT EXISTS(SELECT '' FROM plays p WHERE p.track_id = tracks.id);
DELETE FROM tracks WHERE artist_id = (SELECT a.id FROM artists a WHERE a.artist = '');
DELETE FROM tracks WHERE genre_id = (SELECT g.id FROM genres g WHERE g.genre = '');
DELETE FROM tracks WHERE album is NULL or album = '';

DELETE FROM artists WHERE NOT EXISTS (SELECT '' FROM tracks t WHERE t.artist_id = artists.id);
DELETE FROM genres WHERE NOT EXISTS (SELECT '' FROM tracks t WHERE t.genre_id = genres.id);

-- Prepare Top n Artists;

CREATE OR REPLACE VIEW v_top_artists AS
SELECT a.id, a.artist as name, count(*) as playcount
FROM plays p
JOIN tracks t on t.id = p.track_id
JOIN artists a on a.id = t.artist_id
GROUP BY a.id, a.artist
HAVING count(*) >= 25;

-- Prepare Exports

DROP TABLE IF EXISTS exp_artists;
CREATE TABLE exp_artists AS 
SELECT DISTINCT v.id AS id, v.name
FROM v_top_artists v
ORDER BY v.name;


DROP TABLE IF EXISTS exp_tracks;
CREATE TABLE exp_tracks AS
SELECT t.id, 
       t.artist_id, 
       t.genre_id, 
       t.album, 
       t.name,
       t.year,
       t.compilation,
       t.disc_count,
       t.disc_number, 
       t.track_count,
       t.track_number
FROM tracks t 
WHERE t.artist_id IN (SELECT v.id FROM v_top_artists v);


DROP TABLE IF EXISTS exp_genres;
CREATE TABLE exp_genres AS 
SELECT DISTINCT g.id AS id, g.genre AS name
FROM exp_tracks v
JOIN genres g ON g.id = v.genre_id
ORDER BY g.genre;


DROP TABLE IF EXISTS exp_plays;
CREATE TABLE exp_plays AS
SELECT p.id, p.track_id, p.played_on
FROM exp_tracks t
JOIN plays p ON p.track_id = t.id;
