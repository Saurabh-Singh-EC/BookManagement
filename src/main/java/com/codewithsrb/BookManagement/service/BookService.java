package com.codewithsrb.BookManagement.service;

import com.codewithsrb.BookManagement.exception.ApiException;
import com.codewithsrb.BookManagement.exception.ResourceNotFoundException;
import com.codewithsrb.BookManagement.model.Book;
import com.codewithsrb.BookManagement.repository.BookRepository;
import com.codewithsrb.BookManagement.schema.BookKey;
import com.codewithsrb.BookManagement.schema.BookValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service class which interacts with the database to process the operation related to books.
 */
@Service
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final KafkaProducerService kafkaProducerService;

    private static final String NO_BOOK_FOUND_FOR_ID = "No book exists for the given id: %s";
    private static final String API_EXCEPTION = "An error occurred. Please try again";

    public BookService(BookRepository bookRepository, KafkaProducerService kafkaProducerService) {
        this.bookRepository = bookRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public List<Book> retrieveAllBooks() {
        List<Book> allBooks = bookRepository.findAll();
        if (!CollectionUtils.isEmpty(allBooks)) {
            return allBooks;
        } else {
            return Collections.emptyList();
        }
    }

    public Book retrieveBookById(int id) {
        try {
            Optional<Book> retrievedBook = bookRepository.findById(id);
            return retrievedBook.orElse(null);
        } catch (IllegalArgumentException exception) {
            log.error(exception.getMessage());
            throw new IllegalArgumentException(exception);
        }
    }

    public Book createNewBook(Book book) {
        try {
            Book createdBook = bookRepository.save(book);
            sendToKafka(createdBook);
            return createdBook;
        } catch (IllegalArgumentException exception) {
            log.error(exception.getMessage());
            throw new IllegalArgumentException(exception);
        } catch (Exception exception) {
            throw new ApiException(API_EXCEPTION);
        }
    }

    public Book updateExistingBook(int id, Book bookToUpdate) {
        try {
            Optional<Book> optionalExistingBook = bookRepository.findById(id);
            if (optionalExistingBook.isPresent()) {
                Book existingBook = optionalExistingBook.get();
                existingBook.setTitle(bookToUpdate.getTitle());
                existingBook.setAuthor(bookToUpdate.getAuthor());
                existingBook.setBookLanguage(bookToUpdate.getBookLanguage());
                existingBook.setPrice(bookToUpdate.getPrice());
                return bookRepository.save(existingBook);
            } else {
                throw new ResourceNotFoundException(String.format(NO_BOOK_FOUND_FOR_ID, id));
            }
        } catch (IllegalArgumentException exception) {
            log.error(exception.getMessage());
            throw new IllegalArgumentException(exception);
        }
    }

    public void deleteExistingBook(int id) {
        try {
            Optional<Book> bookToDelete = bookRepository.findById(id);
            if (bookToDelete.isPresent()) {
                bookRepository.deleteById(id);
            } else {
                throw new ResourceNotFoundException(String.format(NO_BOOK_FOUND_FOR_ID, id));
            }
        } catch (IllegalArgumentException exception) {
            log.error(exception.getMessage());
            throw new IllegalArgumentException(exception);
        }
    }

    private void sendToKafka(Book createdBook) {
        BookValue value = BookValue.newBuilder()
                .setTitle(createdBook.getTitle())
                .setAuthor(createdBook.getAuthor())
                .setBookLanguage(createdBook.getBookLanguage())
                .setPrice(createdBook.getPrice())
                .build();

        BookKey key = BookKey.newBuilder()
                .setBookId(createdBook.getId())
                .build();

        kafkaProducerService.sendToKafka(key, value);
    }
}