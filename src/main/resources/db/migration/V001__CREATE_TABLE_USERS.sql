CREATE TABLE IF NOT EXISTS `TB_USER` (
    ID LONG NOT NULL PRIMARY KEY,
    FULL_NAME VARCHAR(70) NOT NULL,
    EMAIL VARCHAR(70) NOT NULL UNIQUE,
    PASSWORD VARCHAR(50) NOT NULL,
    CPF VARCHAR(11) NOT NOT NULL UNIQUE,
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP

);