package com.codewithsrb.BookManagement;

import com.codewithsrb.BookManagement.model.Book;
import com.codewithsrb.BookManagement.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Integration test which is testing the flow end to end.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles({"test"})
class BookManagementIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    private Book book;

    @BeforeEach
    public void init() {
        book = Book.builder().title("dummy title").author("test author").bookLanguage("test language").price(40).build();
    }

    /**
     * Test to create a new book and save it in test repository. Same record will be sent to
     * test topic by producer and will be consumed by the consumer
     */
    @Test
    void testCreateNewBook() throws Exception {
        ResultActions result = this.mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)));

        // waiting for the data consumed by the kafka listener
        Thread.sleep(5000);

        //check if the book is saved in the database
        Assertions.assertEquals(1, bookRepository.findAll().size());


        // assert the status code and response message
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Successfully created a new book with id: 1")));
    }
}