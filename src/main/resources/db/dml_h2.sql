INSERT INTO authors (id, first_name, last_name, bio, image_path)
SELECT UUID(), 'Олена', 'Шевченко', 'Українська письменниця, авторка популярних романів та аудіокниг.', '/images/authors/olena_shevchenko.jpg'
WHERE NOT EXISTS (SELECT 1 FROM authors WHERE first_name = 'Олена' AND last_name = 'Шевченко');

INSERT INTO authors (id, first_name, last_name, bio, image_path)
SELECT UUID(), 'Іван', 'Коваль', 'Сучасний український поет і прозаїк, лауреат літературних премій.', '/images/authors/ivan_koval.jpg'
WHERE NOT EXISTS (SELECT 1 FROM authors WHERE first_name = 'Іван' AND last_name = 'Коваль');

INSERT INTO authors (id, first_name, last_name, bio, image_path)
SELECT UUID(), 'Марія', 'Петренко', 'Авторка дитячих книг, відома своїми казками.', '/images/authors/mariia_petrenko.jpg'
WHERE NOT EXISTS (SELECT 1 FROM authors WHERE first_name = 'Марія' AND last_name = 'Петренко');

INSERT INTO authors (id, first_name, last_name, bio, image_path)
SELECT UUID(), 'Андрій', 'Лисенко', 'Письменник-фантаст, створює захоплюючі науково-фантастичні романи.', '/images/authors/andrii_lysenko.jpg'
WHERE NOT EXISTS (SELECT 1 FROM authors WHERE first_name = 'Андрій' AND last_name = 'Лисенко');

INSERT INTO authors (id, first_name, last_name, bio, image_path)
SELECT UUID(), 'Софія', 'Мельник', 'Авторка історичних романів, дослідниця української культури.', '/images/authors/sofiia_melnyk.jpg'
WHERE NOT EXISTS (SELECT 1 FROM authors WHERE first_name = 'Софія' AND last_name = 'Мельник');

INSERT INTO genres (id, name, description)
SELECT UUID(), 'Фантастика', 'Жанр, що включає наукову фантастику, фентезі та альтернативну історію.'
WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Фантастика');

INSERT INTO genres (id, name, description) 
SELECT UUID(), 'Роман', 'Література, що зосереджується на людських стосунках і почуттях.'
WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Роман');

INSERT INTO genres (id, name, description) 
SELECT UUID(), 'Дитяча література', 'Книги, створені для дітей, включаючи казки та оповідання.'
WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Дитяча література');

INSERT INTO genres (id, name, description) 
SELECT UUID(), 'Історичний роман', 'Романи, що відтворюють історичні події та епохи.'
WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Історичний роман');

INSERT INTO genres (id, name, description) 
SELECT UUID(), 'Детектив', 'Жанр, що включає розслідування злочинів і таємниці.'
WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Детектив');

INSERT INTO audiobooks (id, author_id, genre_id, title, duration, release_year, description, cover_image_path) VALUES
(UUID(), (SELECT id FROM authors WHERE first_name = 'Андрій' AND last_name = 'Лисенко' LIMIT 1), (SELECT id FROM genres WHERE name = 'Фантастика' LIMIT 1), 'Космічна подорож', 7200, 2023, 'Епічна науково-фантастична пригода в далекому космосі.', '/images/covers/kosmichna_podorozh.jpg'),
(UUID(), (SELECT id FROM authors WHERE first_name = 'Олена' AND last_name = 'Шевченко' LIMIT 1), (SELECT id FROM genres WHERE name = 'Роман' LIMIT 1), 'Світло в темряві', 10800, 2021, 'Роман про кохання та боротьбу за щастя.', '/images/covers/svitlo_v_temryavi.jpg'),
(UUID(), (SELECT id FROM authors WHERE first_name = 'Марія' AND last_name = 'Петренко' LIMIT 1), (SELECT id FROM genres WHERE name = 'Дитяча література' LIMIT 1), 'Казки лісу', 3600, 2020, 'Збірка дитячих казок про природу.', '/images/covers/kazky_lisu.jpg'),
(UUID(), (SELECT id FROM authors WHERE first_name = 'Софія' AND last_name = 'Мельник' LIMIT 1), (SELECT id FROM genres WHERE name = 'Історичний роман' LIMIT 1), 'Козацька слава', 14400, 2022, 'Історичний роман про козацьку добу.', '/images/covers/kozatska_slava.jpg'),
(UUID(), (SELECT id FROM authors WHERE first_name = 'Іван' AND last_name = 'Коваль' LIMIT 1), (SELECT id FROM genres WHERE name = 'Детектив' LIMIT 1), 'Таємниця старого маєтку', 9000, 2024, 'Детективна історія з несподіваними поворотами.', '/images/covers/tayemnytsia_mayetku.jpg');

INSERT INTO users (id, username, password_hash, email, avatar_path)
SELECT UUID(), 'oleksandr23', 'hashed_password_1', 'oleksandr23@gmail.com', '/avatars/oleksandr23.jpg'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'oleksandr23');

INSERT INTO users (id, username, password_hash, email, avatar_path)
SELECT UUID(), 'kateryna_p', 'hashed_password_2', 'kateryna.p@ukr.net', '/avatars/kateryna_p.jpg'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'kateryna_p');

INSERT INTO users (id, username, password_hash, email, avatar_path)
SELECT UUID(), 'mykola_k', 'hashed_password_3', 'mykola.k@gmail.com', '/avatars/mykola_k.jpg'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'mykola_k');

INSERT INTO users (id, username, password_hash, email, avatar_path)
SELECT UUID(), 'anna_maria', 'hashed_password_4', 'anna.maria@i.ua', '/avatars/anna_maria.jpg'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'anna_maria');

INSERT INTO users (id, username, password_hash, email, avatar_path)
SELECT UUID(), 'dmytro88', 'hashed_password_5', 'dmytro88@outlook.com', '/avatars/dmytro88.jpg'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'dmytro88');

INSERT INTO collections (id, user_id, name, created_at)
SELECT UUID(), (SELECT id FROM users WHERE username = 'oleksandr23' LIMIT 1), 'Мої улюблені', '2025-01-15 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM collections WHERE name = 'Мої улюблені');

INSERT INTO collections (id, user_id, name, created_at)
SELECT UUID(), (SELECT id FROM users WHERE username = 'kateryna_p' LIMIT 1), 'Для вечорів', '2025-02-20 14:30:00'
WHERE NOT EXISTS (SELECT 1 FROM collections WHERE name = 'Для вечорів');

INSERT INTO collections (id, user_id, name, created_at)
SELECT UUID(), (SELECT id FROM users WHERE username = 'mykola_k' LIMIT 1), 'Дитячі казки', '2024-12-01 09:15:00'
WHERE NOT EXISTS (SELECT 1 FROM collections WHERE name = 'Дитячі казки');

INSERT INTO collections (id, user_id, name, created_at)
SELECT UUID(), (SELECT id FROM users WHERE username = 'anna_maria' LIMIT 1), 'Історичні романи', '2025-03-10 16:45:00'
WHERE NOT EXISTS (SELECT 1 FROM collections WHERE name = 'Історичні романи');

INSERT INTO collections (id, user_id, name, created_at)
SELECT UUID(), (SELECT id FROM users WHERE username = 'dmytro88' LIMIT 1), 'Детективи', '2025-04-01 12:00:00'
WHERE NOT EXISTS (SELECT 1 FROM collections WHERE name = 'Детективи');

INSERT INTO audiobook_collection (collection_id, audiobook_id)
SELECT
    (SELECT id FROM collections WHERE name = 'Мої улюблені' LIMIT 1),
    (SELECT id FROM audiobooks WHERE title = 'Космічна подорож' LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM audiobook_collection
    WHERE collection_id = (SELECT id FROM collections WHERE name = 'Мої улюблені' LIMIT 1)
    AND audiobook_id = (SELECT id FROM audiobooks WHERE title = 'Космічна подорож' LIMIT 1)
);

INSERT INTO audiobook_collection (collection_id, audiobook_id)
SELECT
    (SELECT id FROM collections WHERE name = 'Мої улюблені' LIMIT 1),
    (SELECT id FROM audiobooks WHERE title = 'Світло в темряві' LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM audiobook_collection
    WHERE collection_id = (SELECT id FROM collections WHERE name = 'Мої улюблені' LIMIT 1)
    AND audiobook_id = (SELECT id FROM audiobooks WHERE title = 'Світло в темряві' LIMIT 1)
);

INSERT INTO audiobook_collection (collection_id, audiobook_id)
SELECT
    (SELECT id FROM collections WHERE name = 'Для вечорів' LIMIT 1),
    (SELECT id FROM audiobooks WHERE title = 'Світло в темряві' LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM audiobook_collection
    WHERE collection_id = (SELECT id FROM collections WHERE name = 'Для вечорів' LIMIT 1)
    AND audiobook_id = (SELECT id FROM audiobooks WHERE title = 'Світло в темряві' LIMIT 1)
);

INSERT INTO audiobook_collection (collection_id, audiobook_id)
SELECT
    (SELECT id FROM collections WHERE name = 'Дитячі казки' LIMIT 1),
    (SELECT id FROM audiobooks WHERE title = 'Казки лісу' LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM audiobook_collection
    WHERE collection_id = (SELECT id FROM collections WHERE name = 'Дитячі казки' LIMIT 1)
    AND audiobook_id = (SELECT id FROM audiobooks WHERE title = 'Казки лісу' LIMIT 1)
);

INSERT INTO audiobook_collection (collection_id, audiobook_id)
SELECT
    (SELECT id FROM collections WHERE name = 'Історичні романи' LIMIT 1),
    (SELECT id FROM audiobooks WHERE title = 'Козацька слава' LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM audiobook_collection
    WHERE collection_id = (SELECT id FROM collections WHERE name = 'Історичні романи' LIMIT 1)
    AND audiobook_id = (SELECT id FROM audiobooks WHERE title = 'Козацька слава' LIMIT 1)
);

INSERT INTO audiobook_collection (collection_id, audiobook_id)
SELECT
    (SELECT id FROM collections WHERE name = 'Детективи' LIMIT 1),
    (SELECT id FROM audiobooks WHERE title = 'Таємниця старого маєтку' LIMIT 1)
WHERE NOT EXISTS (
    SELECT 1 FROM audiobook_collection
    WHERE collection_id = (SELECT id FROM collections WHERE name = 'Детективи' LIMIT 1)
    AND audiobook_id = (SELECT id FROM audiobooks WHERE title = 'Таємниця старого маєтку' LIMIT 1)
);

INSERT INTO audiobook_files (id, audiobook_id, file_path, format, size) VALUES
(UUID(), (SELECT id FROM audiobooks WHERE title = 'Космічна подорож' LIMIT 1), '/audio/kosmichna_podorozh.mp3', 'mp3', 150000000),
(UUID(), (SELECT id FROM audiobooks WHERE title = 'Світло в темряві' LIMIT 1), '/audio/svitlo_v_temryavi.flac', 'flac', 300000000),
(UUID(), (SELECT id FROM audiobooks WHERE title = 'Казки лісу' LIMIT 1), '/audio/kazky_lisu.wav', 'wav', 100000000),
(UUID(), (SELECT id FROM audiobooks WHERE title = 'Козацька слава' LIMIT 1), '/audio/kozatska_slava.m4b', 'm4b', 200000000),
(UUID(), (SELECT id FROM audiobooks WHERE title = 'Таємниця старого маєтку' LIMIT 1), '/audio/tayemnytsia_mayetku.aac', 'aac', 180000000);

INSERT INTO listening_progresses (id, user_id, audiobook_id, position, last_listened) VALUES
(UUID(), (SELECT id FROM users WHERE username = 'oleksandr23' LIMIT 1), (SELECT id FROM audiobooks WHERE title = 'Космічна подорож' LIMIT 1), 3600, '2025-04-28 18:00:00'),
(UUID(), (SELECT id FROM users WHERE username = 'kateryna_p' LIMIT 1), (SELECT id FROM audiobooks WHERE title = 'Світло в темряві' LIMIT 1), 5400, '2025-04-27 20:30:00'),
(UUID(), (SELECT id FROM users WHERE username = 'mykola_k' LIMIT 1), (SELECT id FROM audiobooks WHERE title = 'Казки лісу' LIMIT 1), 1800, '2025-04-25 15:15:00'),
(UUID(), (SELECT id FROM users WHERE username = 'anna_maria' LIMIT 1), (SELECT id FROM audiobooks WHERE title = 'Козацька слава' LIMIT 1), 7200, '2025-04-26 10:45:00'),
(UUID(), (SELECT id FROM users WHERE username = 'dmytro88' LIMIT 1), (SELECT id FROM audiobooks WHERE title = 'Таємниця старого маєтку' LIMIT 1), 4500, '2025-04-29 12:00:00');
