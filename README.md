# java-filmorate
Template repository for Filmorate project.

## ER-диаграмма
![ER-diagram Filmorate.jpg](ER-diagram%20Filmorate.jpg)
## **Структура базы данных**

### **Основные сущности:**

* films - фильмы
* users - пользователи
* genres - жанры фильмов
* mpa_ratings - возрастные рейтинги

### **Связующие таблицы:**

* film_genres - связь фильмов и жанров 
* film_likes - лайки пользователей фильмам
* friendships - дружба между пользователями
* friendship_statuses - статусы для связи «дружба» между двумя пользователями

### **Примеры запросов**

#### Получение всех фильмов
SELECT f.film_id,
f.name,
f.description,
f.release_date,
f.duration,
mr.name AS mpa_rating
FROM films AS f
JOIN mpa_ratings AS mr ON f.mpa_rating_id = mr.id;

#### Получение всех пользователей
SELECT user_id,
email,
login,
name,
birthday
FROM users;

#### Топ-5 популярных фильмов
SELECT f.film_id,
f.name,
f.description,
f.release_date,
f.duration,
f.mpa_rating_id,
COUNT(fl.user_id) AS likes_count
FROM films AS f
LEFT JOIN film_likes AS fl ON f.film_id = fl.film_id
GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id
ORDER BY likes_count DESC
LIMIT 5;

#### Список друзей пользователя (с id = 1)
SELECT u.user_id,
u.email,
u.login,
u.name,
u.birthday
FROM friendships AS f
JOIN users AS u ON f.friend_id = u.user_id
JOIN friendship_statuses AS fs ON f.status_id = fs.id
WHERE f.user_id = 1 AND fs.name = 'CONFIRMED';

#### Список общих друзей двух пользователей (с id = 1 b id = 6)
SELECT u.user_id,
u.email,
u.login,
u.name,
FROM friendships AS f1
JOIN friendships AS f2 ON f1.friend_id = f2.friend_id
JOIN users AS u ON u.user_id = f1.friend_id
JOIN friendship_statuses AS fs1 ON f1.status_id = fs1.id
JOIN friendship_statuses AS fs2 ON f2.status_id = fs2.id
WHERE f1.user_id = 1 AND f2.user_id = 6 AND fs1.name = 'CONFIRMED' AND fs2.name = 'CONFIRMED';