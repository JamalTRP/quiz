-- ============================================================
-- QUIZ APPLICATION DATABASE SCHEMA & SEED DATA
-- Target RDBMS : MySQL 8.0+
-- Technology   : JDBC / Plain Java 17 Swing
-- Features     : Users (Roles: ADMIN, STUDENT), Categories,
--                Questions (4 options + correct), Results
-- Passwords    : SHA-256 hashed
-- ============================================================

-- 1. Create and switch to the database
DROP DATABASE IF EXISTS quiz_db;
CREATE DATABASE quiz_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quiz_db;

-- 2. Create 'users' table
-- Passwords stored as SHA-256 hex strings (64 characters)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,
    role ENUM('ADMIN', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Create 'categories' table
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT ''
) ENGINE=InnoDB;

-- 4. Create 'questions' table
-- Contains 4 options (A, B, C, D) and correct_option ('A', 'B', 'C', or 'D')
CREATE TABLE questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_option CHAR(1) NOT NULL CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Create 'results' table
-- Records each student quiz attempt, score, counts, and timestamp
CREATE TABLE results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL,
    wrong_answers INT NOT NULL,
    score INT NOT NULL,
    percentage DECIMAL(5, 2) NOT NULL,
    taken_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- SEED DATA
-- Passwords hashed using SHA-256:
-- 'admin123'   -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- 'student123' -> 03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4
-- ============================================================

-- Insert sample users (1 Admin, 2 Students)
INSERT INTO users (username, password_hash, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
('john_doe', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'STUDENT'),
('sarah_dev', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'STUDENT');

-- Insert categories (Java, DBMS, and Aptitude)
INSERT INTO categories (id, name, description) VALUES
(1, 'Java Programming', 'Core Java, OOPs concepts, Collections, and JVM fundamentals'),
(2, 'Database Management (DBMS)', 'SQL, Normalization, ACID properties, and Transactions'),
(3, 'Quantitative Aptitude', 'Arithmetic reasoning, percentages, speed-time, and logic');

-- Insert 10 Questions for Category 1 (Java Programming)
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES
(1, 'Which of the following is not a Java primitive data type?', 'int', 'boolean', 'String', 'char', 'C'),
(1, 'What is the default value of an uninitialized boolean instance variable in Java?', 'true', 'false', 'null', '0', 'B'),
(1, 'Which Java keyword is used to prevent method overriding?', 'final', 'static', 'abstract', 'const', 'A'),
(1, 'Which component of the Java platform actually executes compiled bytecode?', 'JDK', 'JVM', 'JRE', 'JIT', 'B'),
(1, 'Which interface in the Java Collections Framework allows unique elements only?', 'List', 'Map', 'Set', 'Queue', 'C'),
(1, 'What does JDBC stand for?', 'Java Database Connectivity', 'Java Data Binding Center', 'Java Driver Bridge Control', 'Joint Database Connection', 'A'),
(1, 'Which Exception is thrown when an array is accessed with an illegal index?', 'IllegalAccessException', 'ArrayIndexOutOfBoundsException', 'NullPointerException', 'IndexOutOfBoundsException', 'B'),
(1, 'Which method is called to start a new execution thread in Java?', 'run()', 'init()', 'start()', 'execute()', 'C'),
(1, 'In Java Swing, which event dispatching mechanism is used to update GUI components safely?', 'Event Dispatch Thread (EDT)', 'Main Thread', 'ForkJoinPool', 'Garbage Collector Thread', 'A'),
(1, 'Which statement type should always be used to protect against SQL Injection?', 'Statement', 'PreparedStatement', 'CallableStatement', 'DynamicStatement', 'B');

-- Insert 10 Questions for Category 2 (Database Management - DBMS)
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES
(2, 'What does the ACID acronym stand for in DBMS?', 'Atomicity, Consistency, Isolation, Durability', 'Access, Control, Indexing, Data', 'Action, Condition, Integrity, Distribution', 'Array, Cursor, Instance, Database', 'A'),
(2, 'Which SQL command is used to remove all records from a table without logging individual row deletes?', 'DELETE', 'DROP', 'TRUNCATE', 'REMOVE', 'C'),
(2, 'In relational algebra, which key uniquely identifies a row in a table?', 'Foreign Key', 'Primary Key', 'Composite Key', 'Candidate Key', 'B'),
(2, 'Which normal form eliminates partial functional dependencies on a composite primary key?', '1NF', '2NF', '3NF', 'BCNF', 'B'),
(2, 'Which SQL clause is used to filter groups created by the GROUP BY statement?', 'WHERE', 'ORDER BY', 'HAVING', 'FILTER', 'C'),
(2, 'Which type of JOIN returns all records when there is a match in either left or right table?', 'INNER JOIN', 'LEFT JOIN', 'RIGHT JOIN', 'FULL OUTER JOIN', 'D'),
(2, 'Which of the following is an example of a DDL (Data Definition Language) command?', 'SELECT', 'UPDATE', 'CREATE', 'INSERT', 'C'),
(2, 'What data structure is most commonly used for creating B-tree indexes in relational databases?', 'Binary Search Tree', 'Balanced Multi-way Tree (B+ Tree)', 'Hash Map', 'Red-Black Tree', 'B'),
(2, 'What is the default port number used by MySQL Server?', '1521', '5432', '3306', '1433', 'C'),
(2, 'Which command is used to permanently save all changes made during a transaction?', 'ROLLBACK', 'SAVEPOINT', 'COMMIT', 'STORE', 'C');

-- Insert sample past quiz attempts into 'results'
INSERT INTO results (user_id, category_id, total_questions, correct_answers, wrong_answers, score, percentage, taken_at) VALUES
(2, 1, 10, 8, 2, 80, 80.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 1, 10, 9, 1, 90, 90.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 2, 10, 10, 0, 100, 100.00, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 2, 10, 7, 3, 70, 70.00, DATE_SUB(NOW(), INTERVAL 5 HOUR));
