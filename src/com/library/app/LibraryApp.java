package com.library.app;

import com.library.service.LibraryService;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;


public class LibraryApp {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final LibraryService SERVICE = new LibraryService();

    public static void main(String[] args) {
        System.out.println("=== Library Management System ===");
        while (true) {
            printMenu();
            String choice = prompt("Choose an option: ");
            try {
                switch (choice) {
                    case "1" -> SERVICE.addBook(prompt("Title: "), prompt("Author: "), prompt("ISBN: "), readPositiveInt("Quantity: "));
                    case "2" -> SERVICE.searchBooks(prompt("Search title, author, or ISBN: "));
                    case "3" -> SERVICE.addMember(prompt("Member name: "), prompt("Email: "), prompt("Phone: "));
                    case "4" -> SERVICE.issueBook(readPositiveInt("Book ID: "), readPositiveInt("Member ID: "), readDate("Due date (YYYY-MM-DD): "));
                    case "5" -> SERVICE.returnBook(readPositiveInt("Issue ID: "));
                    case "6" -> SERVICE.showIssueHistory();
                    case "0" -> { System.out.println("Goodbye!"); 
                    return;
                    }
                    default -> System.out.println("Please choose a valid option.");
                }
            } catch (SQLException exception) {
                System.out.println("Database operation failed: " + exception.getMessage());
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid input: " + exception.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n1. Add book\n2. "
        		+ "Search books\n3."
        		+ " Add member\n4."
        		+ " Issue book\n5. "
        		+ "Return book\n6. "
        		+ "View issue history\n0. "
        		+ "Exit");
    }

    private static String prompt(String message) {
    	System.out.print(message); 
    return INPUT.nextLine().trim(); 
    }
    private static int readPositiveInt(String message) {
        int value = Integer.parseInt(prompt(message));
        if (value <= 0) throw new IllegalArgumentException("Enter a positive number.");
        return value;
    }
    private static LocalDate readDate(String message) {
    	return LocalDate.parse(prompt(message)); 
    	}
}
