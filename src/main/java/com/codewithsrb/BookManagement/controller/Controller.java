package com.codewithsrb.BookManagement.controller;

import com.codewithsrb.BookManagement.model.*;
import com.codewithsrb.BookManagement.provider.TokenProvider;
import com.codewithsrb.BookManagement.service.BookService;
import com.codewithsrb.BookManagement.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequestMapping("/books")
@Slf4j
public class Controller {

    private final BookService bookService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    public Controller(BookService bookService, UserDetailsServiceImpl userDetailsServiceImpl, AuthenticationManager authenticationManager, TokenProvider tokenProvider) {
        this.bookService = bookService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> registerNewUser(@RequestBody @Valid UserInfo userInfo) {
        UserInfo registeredUser = userDetailsServiceImpl.registerNewUser(userInfo);

        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .httpStatus(CREATED)
                        .statusCode(CREATED.value())
                        .message(String.format("User Created with name :%s", registeredUser.getEmail()))
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid UserLoginForm userLoginForm) {
        Authentication authentication = authenticate(userLoginForm.getEmail(), userLoginForm.getPassword());
        UserDetailsImpl userDetailsImpl = getAuthenticatedUser(authentication);

        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .httpStatus(OK)
                        .statusCode(OK.value())
                        .message("Login Successful")
                        .data(List.of(String.format("user name: %s", userDetailsImpl.getUsername())
                                , String.format("access_token: %s", tokenProvider.createAccessToken(userDetailsImpl))))
                        .build());
    }

    private Authentication authenticate(String email, String password) {
        log.info("Authenticating the user");
        Authentication authentication = authenticationManager.authenticate(unauthenticated(email, password));
        log.info("User authenticated successfully");
        return authentication;
    }

    private UserDetailsImpl getAuthenticatedUser(Authentication authentication) {
        return ((UserDetailsImpl)authentication.getPrincipal());
    }

    @GetMapping()
    public ResponseEntity<HttpResponse> retrieveAllBooks() {
        List<Book> books = bookService.retrieveAllBooks();

        if(CollectionUtils.isEmpty(books)) {
            return generateResponse("No book found", books, HttpStatus.OK);
        }
        return generateResponse("Successfully retrieved all books", books, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpResponse> retrieveBookById(@PathVariable(value = "id") int id) {
        Book book = bookService.retrieveBookById(id);
        if(Objects.isNull(book)) {
            return generateResponse(String.format("No book found for the id: %s", id), null, HttpStatus.OK);
        }
        return generateResponse(String.format("Successfully retrieved book with id: %s", id), List.of(book), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<HttpResponse> createNewBook(@RequestBody Book book) {
        Book createdBook = bookService.createNewBook(book);
        return generateResponse(String.format("Successfully created a new book with id: %s", createdBook.getId()), List.of(createdBook), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HttpResponse> updateExistingBook(@PathVariable(value = "id") int id, @RequestBody Book book) {
        Book updatedBook = bookService.updateExistingBook(id, book);
        return generateResponse(String.format("Successfully updated book with id: %s", id), List.of(updatedBook), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpResponse> deleteExistingBook(@PathVariable(value = "id") int id) {
        bookService.deleteExistingBook(id);
        return generateResponse(String.format("Successfully deleted book with id: %s", id), Collections.emptyList(), HttpStatus.OK);
    }

    private ResponseEntity<HttpResponse> generateResponse(String message, List<Book> data, HttpStatus httpStatus) {

        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .statusCode(httpStatus.value())
                        .httpStatus(httpStatus)
                        .message(message)
                        .data(data)
                        .build());
    }
}