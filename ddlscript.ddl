CREATE DATABASE IF NOT EXISTS concert_wishlist 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE concert_wishlist;

DROP TABLE IF EXISTS attended_concerts;
DROP TABLE IF EXISTS concert_wishlist;
DROP TABLE IF EXISTS artists;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS flyway_schema_history;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    spotify_user_id VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_users_username (username),
    INDEX idx_users_email (email)
);

CREATE TABLE artists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    spotify_artist_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    popularity INT DEFAULT 0,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_artists_spotify_id (spotify_artist_id),
    INDEX idx_artists_name (name)
);

CREATE TABLE concert_wishlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    artist_id BIGINT NOT NULL,
    city VARCHAR(100) NOT NULL,
    venue VARCHAR(255),
    priority ENUM('HIGH', 'MEDIUM', 'LOW') NOT NULL DEFAULT 'MEDIUM',
    status ENUM('PENDING', 'PLANNED', 'ATTENDED') NOT NULL DEFAULT 'PENDING',
    target_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_user_artist_city (user_id, artist_id, city),
    
    INDEX idx_wishlist_user_id (user_id),
    INDEX idx_wishlist_artist_id (artist_id),
    INDEX idx_wishlist_status (status),
    INDEX idx_wishlist_priority (priority),
    INDEX idx_wishlist_target_date (target_date)
);

CREATE TABLE attended_concerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    artist_id BIGINT NOT NULL,
    city VARCHAR(100) NOT NULL,
    venue VARCHAR(255) NOT NULL,
    concert_date DATE NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    memories TEXT,
    wishlist_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE,
    FOREIGN KEY (wishlist_id) REFERENCES concert_wishlist(id) ON DELETE SET NULL,
    
    INDEX idx_attended_user_id (user_id),
    INDEX idx_attended_artist_id (artist_id),
    INDEX idx_attended_concert_date (concert_date),
    INDEX idx_attended_rating (rating)
);

CREATE TABLE flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
);

INSERT INTO flyway_schema_history (
    installed_rank, version, description, type, script, 
    checksum, installed_by, installed_on, execution_time, success
) VALUES (
    1, '1', 'Initial Schema', 'SQL', 'V1__Initial_Schema.sql', 
    -1, 'manual_setup', NOW(), 0, 1
);

-- =====================================================
CREATE USER IF NOT EXISTS 'app_user'@'localhost' IDENTIFIED BY 'app_password';
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'app_password';

GRANT ALL PRIVILEGES ON concert_wishlist.* TO 'app_user'@'localhost';
GRANT ALL PRIVILEGES ON concert_wishlist.* TO 'app_user'@'%';
FLUSH PRIVILEGES;


INSERT INTO users (username, email, password, role, enabled) VALUES 
('testuser', 'test@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ROLE_USER', true),
('admin', 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ROLE_ADMIN', true);

INSERT INTO artists (spotify_artist_id, name, popularity, image_url) VALUES 
('4Z8W4fKeB5YxbusRsdQVPb', 'Radiohead', 85, 'https://i.scdn.co/image/radiohead.jpg'),
('3WrFJ7ztbogyGnTHbHJFl2', 'The Beatles', 95, 'https://i.scdn.co/image/beatles.jpg'),
('22bE4uQ6baNwSHPVcDxLCe', 'The Rolling Stones', 90, 'https://i.scdn.co/image/rollingstones.jpg'),
('1dfeR4HaWDbWqFHLkxsg1d', 'Queen', 94, 'https://i.scdn.co/image/queen.jpg'),
('36QJpDe2go2KgaRleHCDTp', 'Led Zeppelin', 92, 'https://i.scdn.co/image/ledzeppelin.jpg');

INSERT INTO concert_wishlist (user_id, artist_id, city, venue, priority, status, target_date, notes) VALUES 
(1, 1, 'New York', 'Madison Square Garden', 'HIGH', 'PENDING', '2024-12-31', 'Must see before end of year!'),
(1, 2, 'London', 'O2 Arena', 'MEDIUM', 'PENDING', '2025-06-15', 'Dream venue for The Beatles tribute'),
(1, 3, 'Los Angeles', 'Hollywood Bowl', 'LOW', 'PENDING', NULL, 'Would be nice if opportunity arises');

INSERT INTO attended_concerts (user_id, artist_id, city, venue, concert_date, rating, memories, wishlist_id) VALUES 
(1, 4, 'Boston', 'TD Garden', '2023-10-15', 5, 'Absolutely incredible show! Freddie Mercury tribute was amazing.', NULL),
(1, 5, 'Chicago', 'United Center', '2023-08-20', 4, 'Great sound quality, but seats were far from stage.', NULL);


SHOW TABLES;

DESCRIBE users;
DESCRIBE artists;
DESCRIBE concert_wishlist;
DESCRIBE attended_concerts;

SELECT 'USERS' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'ARTISTS', COUNT(*) FROM artists
UNION ALL
SELECT 'WISHLIST', COUNT(*) FROM concert_wishlist
UNION ALL
SELECT 'ATTENDED', COUNT(*) FROM attended_concerts;

SELECT 
    u.username,
    a.name as artist_name,
    cw.city,
    cw.priority,
    cw.status
FROM concert_wishlist cw
JOIN users u ON cw.user_id = u.id
JOIN artists a ON cw.artist_id = a.id;
