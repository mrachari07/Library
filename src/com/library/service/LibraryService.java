package com.library.service;

import com.library.db.DBCon;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class LibraryService {
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("5.00");

    public void addBook(String title, String author, String isbn, int quantity) throws SQLException {
        String sql = "INSERT INTO books (title, author, isbn, total_quantity, available_quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBCon.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setString(3, isbn);
            statement.setInt(4, quantity);
            statement.setInt(5, quantity);
            statement.executeUpdate();
            System.out.println("Book added successfully.");
        }
    }

    public void addMember(String name, String email, String phone) throws SQLException {
        String sql = "INSERT INTO members (name, email, phone) VALUES (?, ?, ?)";
        try (Connection connection = DBCon.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.executeUpdate();
            System.out.println("Member added successfully.");
        }
    }

    public void searchBooks(String keyword) throws SQLException {
        String sql = "SELECT book_id, title, author, isbn, available_quantity FROM books "
                + "WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? ORDER BY title";
        try (Connection connection = DBCon.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet result = statement.executeQuery()) {
                boolean found = false;
                while (result.next()) {
                    found = true;
                    System.out.printf("%d | %s | %s | ISBN: %s | Available: %d%n",
                            result.getInt("book_id"), result.getString("title"),
                            result.getString("author"), result.getString("isbn"),
                            result.getInt("available_quantity"));
                }
                if (!found) System.out.println("No books found.");
            }
        }
    }

    public void issueBook(int bookId, int memberId, LocalDate dueDate) throws SQLException {
        String availableBook = "SELECT available_quantity FROM books WHERE book_id = ? FOR UPDATE";
        String memberExists = "SELECT member_id FROM members WHERE member_id = ?";
        String insertIssue = "INSERT INTO book_issues (book_id, member_id, issue_date, due_date) VALUES (?, ?, ?, ?)";
        String reduceStock = "UPDATE books SET available_quantity = available_quantity - 1 WHERE book_id = ?";

        try (Connection connection = DBCon.getConnection()) {
            connection.setAutoCommit(false); // Start transaction.
            try {
                try (PreparedStatement member = connection.prepareStatement(memberExists)) {
                    member.setInt(1, memberId);
                    try (ResultSet result = member.executeQuery()) {
                        if (!result.next()) throw new SQLException("Member ID does not exist.");
                    }
                }
                try (PreparedStatement book = connection.prepareStatement(availableBook)) {
                    book.setInt(1, bookId);
                    try (ResultSet result = book.executeQuery()) {
                        if (!result.next()) throw new SQLException("Book ID does not exist.");
                        if (result.getInt("available_quantity") < 1) throw new SQLException("This book is unavailable.");
                    }
                }
                try (PreparedStatement issue = connection.prepareStatement(insertIssue);
                     PreparedStatement stock = connection.prepareStatement(reduceStock)) {
                    issue.setInt(1, bookId); issue.setInt(2, memberId);
                    issue.setDate(3, Date.valueOf(LocalDate.now())); issue.setDate(4, Date.valueOf(dueDate));
                    issue.executeUpdate();
                    stock.setInt(1, bookId); stock.executeUpdate();
                }
                connection.commit();
                System.out.println("Book issued successfully.");
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void returnBook(int issueId) throws SQLException {
        String issueSql = "SELECT book_id, due_date FROM book_issues WHERE issue_id = ? AND return_date IS NULL FOR UPDATE";
        String returnSql = "UPDATE book_issues SET return_date = ?, fine_amount = ? WHERE issue_id = ?";
        String stockSql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE book_id = ?";

        try (Connection connection = DBCon.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement findIssue = connection.prepareStatement(issueSql)) {
                findIssue.setInt(1, issueId);
                try (ResultSet result = findIssue.executeQuery()) {
                    if (!result.next()) throw new SQLException("Active issue ID not found.");
                    int bookId = result.getInt("book_id");
                    LocalDate dueDate = result.getDate("due_date").toLocalDate();
                    long lateDays = Math.max(0, ChronoUnit.DAYS.between(dueDate, LocalDate.now()));
                    BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(lateDays));
                    try (PreparedStatement updateReturn = connection.prepareStatement(returnSql);
                         PreparedStatement addStock = connection.prepareStatement(stockSql)) {
                        updateReturn.setDate(1, Date.valueOf(LocalDate.now()));
                        updateReturn.setBigDecimal(2, fine); updateReturn.setInt(3, issueId);
                        updateReturn.executeUpdate();
                        addStock.setInt(1, bookId); addStock.executeUpdate();
                    }
                    connection.commit();
                    System.out.println("Book returned. Fine: Rs. " + fine);
                }
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void showIssueHistory() throws SQLException {
        String sql = "SELECT i.issue_id, b.title, m.name, i.issue_date, i.due_date, i.return_date, i.fine_amount "
                + "FROM book_issues i JOIN books b ON i.book_id = b.book_id "
                + "JOIN members m ON i.member_id = m.member_id ORDER BY i.issue_id DESC";
        try (Connection connection = DBCon.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            System.out.println("ID | Book | Member | Issued | Due | Returned | Fine");
            while (result.next()) {
                System.out.printf("%d | %s | %s | %s | %s | %s | %s%n",
                        result.getInt("issue_id"), result.getString("title"), result.getString("name"),
                        result.getDate("issue_date"), result.getDate("due_date"), result.getDate("return_date"),
                        result.getBigDecimal("fine_amount"));
            }
        }
    }
}
