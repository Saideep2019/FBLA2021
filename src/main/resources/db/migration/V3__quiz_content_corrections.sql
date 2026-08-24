-- Corrections approved by the Stage 7 content audit.
-- V2 remains the unchanged historical import; this migration changes runtime data only.

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Which U.S. state has the largest total area?'
WHERE QUESTIONID = 1;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'As of 2021, which country was the world''s most populous?'
WHERE QUESTIONID = 2;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Which is the world''s smallest independent state by area?'
WHERE QUESTIONID = 3;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Australia is wider from east to west than the Moon''s diameter.'
WHERE QUESTIONID = 8;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Coffee beans are seeds found inside coffee fruit.'
WHERE QUESTIONID = 9;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'What is the highest mountain in Canada?'
WHERE QUESTIONID = 12;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Which is the world''s largest freshwater lake by surface area?',
    ANSWER = 'Lake Superior'
WHERE QUESTIONID = 16;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'What is the anatomical name for the kneecap?'
WHERE QUESTIONID = 17;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'What is the world''s fastest land mammal?'
WHERE QUESTIONID = 18;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'What was Walt Disney''s first feature-length animated film?'
WHERE QUESTIONID = 20;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Which SI derived unit is named after Italian scientist Alessandro Volta?'
WHERE QUESTIONID = 21;

UPDATE PUBLIC.QUESTIONS
SET ANSWER = '46th'
WHERE QUESTIONID = 22;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'What is the minimum age to purchase alcohol in all 50 U.S. states?'
WHERE QUESTIONID = 27;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'In which city is Harvard Yard located?',
    ANSWER = 'Cambridge'
WHERE QUESTIONID = 31;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'In 2021, Hartsfield-Jackson Atlanta International Airport was the world''s busiest airport by passenger traffic.'
WHERE QUESTIONID = 32;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'As of 2021, what was the capital of Indonesia?'
WHERE QUESTIONID = 34;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Who co-authored The Communist Manifesto with Friedrich Engels?'
WHERE QUESTIONID = 36;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'As of 2021, the Empire State Building was the tallest building in New York City.'
WHERE QUESTIONID = 40;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Asia is the world''s largest continent by land area.'
WHERE QUESTIONID = 41;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'At the 2021 National Leadership Conference, Georgia FBLA was recognized as the largest state chapter.',
    ANSWER = 'true'
WHERE QUESTIONID = 42;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Which is the largest planet in the solar system by equatorial diameter?'
WHERE QUESTIONID = 46;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Which galaxy contains our solar system?'
WHERE QUESTIONID = 47;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'A supermassive black hole called Sagittarius A* is at the center of the Milky Way.'
WHERE QUESTIONID = 48;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'Which tree species includes the world''s tallest living trees?',
    ANSWER = 'Coast redwood'
WHERE QUESTIONID = 49;

UPDATE PUBLIC.QUESTIONS
SET QUESTION = 'According to a 2016 Imperial College London study, men from which country were tallest on average in 2014?'
WHERE QUESTIONID = 50;

-- Repair fill-in-the-blank fragments while retaining their displayed order.
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'The abbreviation for intelligence quotient is' WHERE QUESTIONID = 5 AND ID = 54;
UPDATE PUBLIC.ANSWERS SET ANSWERS = '.' WHERE QUESTIONID = 5 AND ID = 500;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'Using low-fat milk will' WHERE QUESTIONID = 11 AND ID = 7;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'the taste. (affect or effect)' WHERE QUESTIONID = 11 AND ID = 8;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'Joe Biden was the' WHERE QUESTIONID = 22 AND ID = 50;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'president of the United States.' WHERE QUESTIONID = 22 AND ID = 51;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'The' WHERE QUESTIONID = 23 AND ID = 52;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'is the star at the center of our solar system.' WHERE QUESTIONID = 23 AND ID = 59;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'A' WHERE QUESTIONID = 24 AND ID = 54;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'is made of water droplets or ice crystals.' WHERE QUESTIONID = 24 AND ID = 55;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'In 2021, the country with the' WHERE QUESTIONID = 25 AND ID = 56;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'GDP in current U.S. dollars was the United States.' WHERE QUESTIONID = 25 AND ID = 57;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'In 2021, Canada''s prime minister was' WHERE QUESTIONID = 26 AND ID = 125;
UPDATE PUBLIC.ANSWERS SET ANSWERS = '.' WHERE QUESTIONID = 26 AND ID = 126;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'The' WHERE QUESTIONID = 28 AND ID = 59;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'separates the Northern and Southern Hemispheres.' WHERE QUESTIONID = 28 AND ID = 505;
UPDATE PUBLIC.ANSWERS SET ANSWERS = 'Bill Gates and Paul Allen co-founded' WHERE QUESTIONID = 30 AND ID = 62;
UPDATE PUBLIC.ANSWERS SET ANSWERS = '.' WHERE QUESTIONID = 30 AND ID = 63;

-- Make applicable correct answers match exactly one displayed choice.
UPDATE PUBLIC.ANSWERS
SET ANSWERS = 'Snow White and the Seven Dwarfs'
WHERE QUESTIONID = 20 AND ID = 42;

UPDATE PUBLIC.ANSWERS
SET ANSWERS = 'Mount Logan'
WHERE QUESTIONID = 12 AND ID = 127;

UPDATE PUBLIC.ANSWERS
SET ANSWERS = 'North Korea'
WHERE QUESTIONID = 37 AND ID = 123;

UPDATE PUBLIC.ANSWERS
SET ANSWERS = 'Coast redwood'
WHERE QUESTIONID = 49 AND ANSWERS = 'Redwoods';

-- Replace Q31's duplicated Cambridge row with the four approved choices.
DELETE FROM PUBLIC.ANSWERS WHERE QUESTIONID = 31;
INSERT INTO PUBLIC.ANSWERS VALUES('Boston', 31, 1);
INSERT INTO PUBLIC.ANSWERS VALUES('Cambridge', 31, 2);
INSERT INTO PUBLIC.ANSWERS VALUES('New Haven', 31, 3);
INSERT INTO PUBLIC.ANSWERS VALUES('New York City', 31, 4);

-- Every True/False question receives two real, ordered choices.
DELETE FROM PUBLIC.ANSWERS WHERE QUESTIONID IN (4, 7, 8, 9, 10, 29, 32, 38, 39, 40, 41, 42, 43, 44, 45, 48);
INSERT INTO PUBLIC.ANSWERS
SELECT 'true', QUESTIONID, 1
FROM PUBLIC.QUESTIONS
WHERE QUESTIONID IN (4, 7, 8, 9, 10, 29, 32, 38, 39, 40, 41, 42, 43, 44, 45, 48);
INSERT INTO PUBLIC.ANSWERS
SELECT 'false', QUESTIONID, 2
FROM PUBLIC.QUESTIONS
WHERE QUESTIONID IN (4, 7, 8, 9, 10, 29, 32, 38, 39, 40, 41, 42, 43, 44, 45, 48);

-- Re-key all answer rows deterministically: question ID plus displayed position.
-- This preserves ordering while removing every reused or duplicated legacy ID.
MERGE INTO PUBLIC.ANSWERS AS TARGET
USING (
    SELECT QUESTIONID,
           ID AS OLD_ID,
           ANSWERS,
           CAST(QUESTIONID * 10 + ROW_NUMBER() OVER (
               PARTITION BY QUESTIONID ORDER BY ID, ANSWERS) AS INTEGER) AS NEW_ID
    FROM PUBLIC.ANSWERS
) AS SOURCE
ON TARGET.QUESTIONID = SOURCE.QUESTIONID
   AND TARGET.ID = SOURCE.OLD_ID
   AND TARGET.ANSWERS = SOURCE.ANSWERS
WHEN MATCHED THEN UPDATE SET TARGET.ID = SOURCE.NEW_ID;
