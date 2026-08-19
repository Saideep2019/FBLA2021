-- H2 2.x schema for the newly initialized quiz database.
-- The legacy database has no primary keys, foreign keys, unique constraints, or indexes.

CREATE TABLE PUBLIC.QUESTIONS (
    QUESTION VARCHAR(255) NOT NULL,
    QUESTIONID INTEGER,
    DISPLAYTYPE INTEGER,
    ANSWER VARCHAR(255) NOT NULL
);

CREATE TABLE PUBLIC.ANSWERS (
    ANSWERS VARCHAR(255) NOT NULL,
    QUESTIONID INTEGER,
    ID INTEGER
);
