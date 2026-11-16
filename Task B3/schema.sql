CREATE DATABASE IF NOT EXISTS assignment5_db;
USE assignment5_db;

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(30) PRIMARY KEY,
    pwd VARCHAR(60) NOT NULL
);

INSERT IGNORE INTO users (id, pwd) VALUES ('admin', 'secret123');

DROP TABLE IF EXISTS bus_pass;
CREATE TABLE bus_pass (
    pass_id VARCHAR(20) PRIMARY KEY,
    reg_no VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    route_no INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL
);