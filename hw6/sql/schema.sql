-- Схема БД проекта. Выполняется автоматически один раз — при создании пустой базы.
-- Каждое изменение структуры (CREATE TABLE, ALTER TABLE, индексы) выполните на своей базе
-- (Adminer или консоль) и допишите тот же SQL в конец этого файла.

CREATE TABLE `User`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `login`        VARCHAR(255) NOT NULL,
    `passwordSha`  VARCHAR(255) NOT NULL,
    `creationTime` DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_User_login` (`login`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Задача 1: email пользователя (уникальный, обязательный)
ALTER TABLE `User`
    ADD COLUMN `email` VARCHAR(255) NULL;
UPDATE `User`
SET `email` = CONCAT('user', `id`, '@example.local')
WHERE `email` IS NULL;
ALTER TABLE `User` MODIFY COLUMN `email` VARCHAR (255) NOT NULL,
    ADD UNIQUE KEY `uk_User_email` (`email`);

-- Задача 2: события входа и выхода
CREATE TABLE `Event`
(
    `id`           BIGINT   NOT NULL AUTO_INCREMENT,
    `userId`       BIGINT   NOT NULL,
    `type`         ENUM('ENTER','LOGOUT') NOT NULL,
    `creationTime` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    KEY            `idx_Event_userId` (`userId`),
    CONSTRAINT `fk_Event_userId`
        FOREIGN KEY (`userId`) REFERENCES `User` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Задача 4: сообщения
CREATE TABLE `Talk`
(
    `id`           BIGINT   NOT NULL AUTO_INCREMENT,
    `sourceUserId` BIGINT   NOT NULL,
    `targetUserId` BIGINT   NOT NULL,
    `text`         TEXT     NOT NULL,
    `creationTime` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    KEY            `idx_Talk_sourceUserId` (`sourceUserId`),
    KEY            `idx_Talk_targetUserId` (`targetUserId`),
    CONSTRAINT `fk_Talk_sourceUserId`
        FOREIGN KEY (`sourceUserId`) REFERENCES `User` (`id`),
    CONSTRAINT `fk_Talk_targetUserId`
        FOREIGN KEY (`targetUserId`) REFERENCES `User` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
