package com.codewithsrb.BookManagement.controller;

import com.codewithsrb.BookManagement.model.Book;
import com.codewithsrb.BookManagement.model.UserDetailsImpl;
import com.codewithsrb.BookManagement.model.UserInfo;
import com.codewithsrb.BookManagement.model.UserLoginForm;
import com.codewithsrb.BookManagement.provider.TokenProvider;
import com.codewithsrb.BookManagement.service.BookService;
import com.codewithsrb.BookManagement.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Test Class which is using mock to test the controller Endpoints.
 */
@WebMvcTest(controllers = Controller.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private TokenProvider tokenProvider;

    @Autowired
    ObjectMapper objectMapper;

    private UserInfo userInfo;
    private UserLoginForm userLoginForm;
    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
    private Book book;
    private Book createdBook;

    @BeforeEach
    public void init() {
        userInfo = UserInfo.builder().email("dummy@email.com").password("testPass").build();
        userLoginForm = UserLoginForm.builder().email("dummy@email.com").password("testPass").build();
        usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(new UserDetailsImpl(userInfo), "testPass");
        book = Book.builder().title("dummy title").author("test author").bookLanguage("test language").price(40).build();
        createdBook = Book.builder().id(1).title("dummy title").author("test author").bookLanguage("test language").price(40).build();
    }

    /**
     * New user registration test
     */
    @Test
    void testUserRegistration() throws Exception {
        when(userDetailsService.registerNewUser(ArgumentMatchers.any())).thenReturn(userInfo);
        ResultActions result = this.mockMvc.perform(post("/books/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userInfo)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("User Created with name :dummy@email.com")))
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * New user login test
     */
    @Test
    void testUserLogin() throws Exception {
        when(authenticationManager.authenticate(ArgumentMatchers.any())).thenReturn(usernamePasswordAuthenticationToken);
        ResultActions result = this.mockMvc.perform(post("/books/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLoginForm)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Login Successful")))
                .andDo(MockMvcResultHandlers.print());
    }

    /**
     * Get all books test
     */
    @Test
    void testRetrieveAllBooks() throws Exception {
        when(bookService.retrieveAllBooks()).thenReturn(List.of(book));
        ResultActions result = this.mockMvc.perform(get("/books")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Successfully retrieved all books")))
                .andDo(MockMvcResultHandlers.print());

        verify(bookService, times(1)).retrieveAllBooks();
    }

    /**
     * Get Book by ID test
     */
    @Test
    void testRetrieveBookById() throws Exception {
        when(bookService.retrieveBookById(1)).thenReturn(book);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.get("/books/1")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Successfully retrieved book with id: 1")))
                .andDo(MockMvcResultHandlers.print());

        verify(bookService, times(1)).retrieveBookById(1);
    }

    /**
     * Create a new book test
     */
    @Test
    void testCreateNewBook() throws Exception {
        when(bookService.createNewBook(book)).thenReturn(createdBook);
        ResultActions result = this.mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Successfully created a new book with id: 1")))
                .andDo(MockMvcResultHandlers.print());

        verify(bookService, times(1)).createNewBook(book);
    }

    /**
     * Update existing book test
     */
    @Test
    void testUpdateExistingBook() throws Exception {
        when(bookService.updateExistingBook(1, createdBook)).thenReturn(createdBook);
        ResultActions result = this.mockMvc.perform(put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdBook)));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Successfully updated book with id: 1")))
                .andDo(MockMvcResultHandlers.print());

        verify(bookService, times(1)).updateExistingBook(1, createdBook);
    }

    /**
     * Delete existing book test
     */
    @Test
    void testDeleteExistingBook() throws Exception {
        doNothing().when(bookService).deleteExistingBook(1);

        ResultActions result = this.mockMvc.perform(delete("/books/1")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Successfully deleted book with id: 1")))
                .andDo(MockMvcResultHandlers.print());

        verify(bookService, times(1)).deleteExistingBook(1);
    }

    /**
     * Testing a scenario when an exception is thrown during accessing the resource
     */
    @Test
    void testExceptionThrownWhileUpdatingBook() throws Exception {
        when(bookService.updateExistingBook(1, createdBook)).thenThrow(new IllegalArgumentException("No book exists for the given id: 1"));

        ResultActions result = this.mockMvc.perform(put("/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdBook)));

        result.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason", CoreMatchers.is("No book exists for the given id: 1")))
                .andDo(MockMvcResultHandlers.print());

        verify(bookService, times(1)).updateExistingBook(1, createdBook);
    }
}
