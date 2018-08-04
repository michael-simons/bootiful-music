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

CREATE TABLE plays (
    id SERIAL NOT NULL,
    track_id INTEGER NOT NULL,
    played_on TIMESTAMP NOT NULL,
    CONSTRAINT plays_pk PRIMARY KEY (id),
    CONSTRAINT plays_tracks_fk FOREIGN KEY (track_id) REFERENCES tracks(id)
);

CREATE INDEX plays_played_on ON plays (played_on);

CREATE INDEX plays_played_on_date ON plays (date_trunc('day', played_on));