INSERT INTO mpa_ratings (id, name)
SELECT 1, 'G' WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = 1);
INSERT INTO mpa_ratings (id, name)
SELECT 2, 'PG' WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = 2);
INSERT INTO mpa_ratings (id, name)
SELECT 3, 'PG-13' WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = 3);
INSERT INTO mpa_ratings (id, name)
SELECT 4, 'R' WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = 4);
INSERT INTO mpa_ratings (id, name)
SELECT 5, 'NC-17' WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = 5);

INSERT INTO genres (genre_id, name)
SELECT 1, 'Комедия' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_id = 1);
INSERT INTO genres (genre_id, name)
SELECT 2, 'Драма' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_id = 2);
INSERT INTO genres (genre_id, name)
SELECT 3, 'Мультфильм' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_id = 3);
INSERT INTO genres (genre_id, name)
SELECT 4, 'Триллер' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_id = 4);
INSERT INTO genres (genre_id, name)
SELECT 5, 'Документальный' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_id = 5);
INSERT INTO genres (genre_id, name)
SELECT 6, 'Боевик' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE genre_id = 6);

INSERT INTO friendship_statuses (id, name)
SELECT 1, 'UNCONFIRMED' WHERE NOT EXISTS (SELECT 1 FROM friendship_statuses WHERE id = 1);
INSERT INTO friendship_statuses (id, name)
SELECT 2, 'CONFIRMED' WHERE NOT EXISTS (SELECT 1 FROM friendship_statuses WHERE id = 2);