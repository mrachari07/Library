CREATE DATABASE IF NOT EXISTS library_management;
USE library_management;

CREATE TABLE books (
    book_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    total_quantity INT NOT NULL CHECK (total_quantity >= 0),
    available_quantity INT NOT NULL CHECK (available_quantity >= 0)
);

CREATE TABLE members (
    member_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(20)
);

CREATE TABLE book_issues (
    issue_id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_issue_book FOREIGN KEY (book_id) REFERENCES books(book_id),
    CONSTRAINT fk_issue_member FOREIGN KEY (member_id) REFERENCES members(member_id)
);

INSERT INTO books (title, author, isbn, total_quantity, available_quantity) VALUES
('Fight Club', 'David Fincher', '9780061122415', 3, 3),
('Avatar', 'James Cameron', '9780132350884', 2, 2);

INSERT INTO members (name, email, phone) VALUES
('Mohan', 'Mohan@gmail.com', '8332060027'),
('Krishna', 'Krishna@gmail.com', '9765363434');

select*from books;
select*from members;
select*from book_issues;

