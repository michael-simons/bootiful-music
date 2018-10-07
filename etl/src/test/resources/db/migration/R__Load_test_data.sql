/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

DELETE FROM plays;
DELETE FROM tracks;
DELETE FROM artists;
DELETE FROM genres;

INSERT INTO artists(name) VALUES('Queen');
INSERT INTO artists(name) VALUES('Die Ärzte');

INSERT INTO genres (name) VALUES ('Pop');

INSERT INTO tracks (artist_id,genre_id,album,name,year,compilation,disc_count,disc_number,track_count,track_number)
SELECT a.id,g.id,'Hot Space','Under Pressure','1982','f','2','1','11','11'
  FROM artists a, genres g
 WHERE a.name = 'Queen'
   AND g.name = 'Pop';
