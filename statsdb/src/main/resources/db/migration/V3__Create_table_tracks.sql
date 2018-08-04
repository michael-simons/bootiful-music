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

CREATE TABLE tracks (
    id SERIAL NOT NULL,
    artist_id INTEGER NOT NULL,
    genre_id INTEGER NOT NULL,
    album VARCHAR(255) NOT NULL,
    name VARCHAR(4000) NOT NULL,
    year SMALLINT NULL,
    compilation VARCHAR(1) DEFAULT 'f' NOT NULL CHECK(compilation IN ('t','f')) ,
    disc_count   SMALLINT NULL,
    disc_number  SMALLINT NULL,
    track_count  SMALLINT NULL,
    track_number SMALLINT NULL,
    CONSTRAINT tracks_pk PRIMARY KEY (id),
    CONSTRAINT tracks_uk UNIQUE (artist_id, genre_id, album, name, track_number, disc_number),
    CONSTRAINT tracks_artists_fk FOREIGN KEY (artist_id) REFERENCES artists(id),
    CONSTRAINT tracks_genres_fk FOREIGN KEY (genre_id) REFERENCES genres(id)
);