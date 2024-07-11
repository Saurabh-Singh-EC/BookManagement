package com.codewithsrb.BookManagement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "BOOK_DETAILS")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

    private String title;
    private String author;
    private String bookLanguage;
    private double price;

}
