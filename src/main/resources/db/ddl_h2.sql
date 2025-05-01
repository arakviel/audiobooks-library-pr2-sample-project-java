CREATE TYPE IF NOT EXISTS file_format_enum AS ENUM ('mp3', 'ogg', 'wav', 'm4b', 'aac', 'flac');

-- 3NF - дані атомарні, залежать лише від первинного ключа
CREATE TABLE IF NOT EXISTS authors (
    PRIMARY KEY(id),
    id          UUID,
    first_name  VARCHAR(64) NOT NULL,
    last_name   VARCHAR(64) NOT NULL,
    bio         TEXT,
    image_path  VARCHAR(2048)
);

-- 3NF
CREATE TABLE IF NOT EXISTS genres (
    PRIMARY KEY(id),
    id          UUID,
    name        VARCHAR(64) NOT NULL,
                CONSTRAINT genres_name_key
                    UNIQUE (name),
    description TEXT
);

-- 3NF
CREATE TABLE IF NOT EXISTS audiobooks (
    PRIMARY KEY(id),
    id               UUID,
    author_id        UUID        NOT NULL,
                      CONSTRAINT audiobooks_author_id_authors_id_fkey
                     FOREIGN KEY (author_id)
                      REFERENCES authors(id)
                       ON DELETE CASCADE,

    genre_id         UUID        NOT NULL,
                      CONSTRAINT audiobooks_genre_id_genres_id_fkey
                     FOREIGN KEY (genre_id)
                      REFERENCES genres(id)
                       ON DELETE CASCADE,

    title            VARCHAR(255) NOT NULL,
    duration         INTEGER      NOT NULL,
                     CONSTRAINT audiobooks_duration_positive_check
                          CHECK (duration > 0),
    
    release_year     INTEGER      NOT NULL,
                     CONSTRAINT audiobooks_release_year_check
                          CHECK (release_year >= 1900 AND release_year <= EXTRACT(YEAR FROM CURRENT_DATE) + 1),

    description      TEXT,
    cover_image_path VARCHAR(2048)
);

CREATE INDEX IF NOT EXISTS audiobooks_author_id_idx ON audiobooks(author_id);
CREATE INDEX IF NOT EXISTS audiobooks_genre_id_idx  ON audiobooks(genre_id);

-- 3NF
CREATE TABLE IF NOT EXISTS users (
    PRIMARY KEY(id),
    id               UUID,
    username         VARCHAR(64)   NOT NULL,
                     CONSTRAINT users_username_key
                         UNIQUE (username),
                     CONSTRAINT users_username_not_empty_check
                          CHECK (length(trim(username)) > 0),

    password_hash    VARCHAR(128)  NOT NULL,
    email            VARCHAR(376),
    avatar_path      VARCHAR(2048)
);

CREATE INDEX IF NOT EXISTS users_email_idx ON users(email);

-- 3NF
CREATE TABLE IF NOT EXISTS collections (
    PRIMARY KEY(id),
    id               UUID,
    user_id          UUID,
                      CONSTRAINT collections_user_id_users_id_fkey
                     FOREIGN KEY (user_id)
                      REFERENCES users(id)
                       ON DELETE CASCADE,

    name             VARCHAR(128) NOT NULL,
                     CONSTRAINT collections_name_not_empty_check
                          CHECK (length(trim(name)) > 0),
    created_at       TIMESTAMP
);

-- 2NF
CREATE TABLE IF NOT EXISTS audiobook_collection (
    PRIMARY KEY(collection_id, audiobook_id),
    collection_id   UUID NOT NULL,
                     CONSTRAINT audiobook_collection_collection_id_collections_id_fkey
                    FOREIGN KEY (collection_id)
                     REFERENCES collections(id)
                      ON DELETE CASCADE,

    audiobook_id    UUID NOT NULL,
                     CONSTRAINT audiobook_collection_audiobook_id_audiobooks_id_fkey
                    FOREIGN KEY (audiobook_id)
                     REFERENCES audiobooks(id)
                      ON DELETE CASCADE
);

-- 3NF
CREATE TABLE IF NOT EXISTS audiobook_files (
    PRIMARY KEY(id),
    id               UUID,
    audiobook_id     UUID             NOT NULL, 
                      CONSTRAINT audiobook_files_audiobook_id_audiobooks_id_fkey
                     FOREIGN KEY (audiobook_id)
                      REFERENCES audiobooks(id)
                         ON DELETE CASCADE,

    file_path        VARCHAR(2048)    NOT NULL,
                     CONSTRAINT audiobook_files_file_path_not_empty_check
                          CHECK (length(trim(file_path)) > 0),

    format           file_format_enum NOT NULL,
    size             INTEGER,
                     CONSTRAINT audiobook_files_size_positive_check
                          CHECK (size IS NULL OR size > 0)
);

CREATE INDEX IF NOT EXISTS audiobook_files_audiobook_id_idx ON audiobook_files(audiobook_id);

-- 3NF
CREATE TABLE IF NOT EXISTS listening_progresses (
    PRIMARY KEY(id),
    id               UUID,
    user_id          UUID,
                      CONSTRAINT listening_progresses_user_id_users_id_fkey
                     FOREIGN KEY (user_id)
                      REFERENCES users(id)
                       ON DELETE CASCADE,

    audiobook_id     UUID       NOT NULL,
                      CONSTRAINT listening_progresses_audiobook_id_audiobooks_id_fkey
                     FOREIGN KEY (audiobook_id)
                      REFERENCES audiobooks(id)
                       ON DELETE CASCADE,

    position         INTEGER    NOT NULL,
                     CONSTRAINT listening_progresses_position_positive_check
                          CHECK (position > 0),

    last_listened    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS listening_progresses_user_id_idx       ON listening_progresses(user_id);
CREATE INDEX IF NOT EXISTS listening_progresses_audiobook_id_idx  ON listening_progresses(audiobook_id);