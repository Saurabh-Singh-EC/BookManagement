### Application Overview
* BookManagement Spring Boot Application designed for managing the books.
* It provides various rest EndPoint in order to get, create, update and delete the books.

### Features
  * Register a new user
  * User Login
  * Create new book
  * Update existing book
  * Delete existing book
  * Get book by id and get all

### Technologies Used
* Java 17, Maven 3.9.6, Spring boot 3.2.5, Spring security, Spring Data JPA
* JWT authentication and authorization.
* H2 in memory database
* Spring Kafka Producer and Consumer
* Docker for running Kafka broker, schema registry and Zookeeper

### Rest ENDPOINT
* Register new user:  http://localhost:9291/books/register
* Login: http://localhost:9291/books/login
* Get all books: http://localhost:9291/books
* Get book by id: http://localhost:9291/books/{id}
* Create new book: http://localhost:9291/books
* Update existing book: http://localhost:9291/books/{id}
* Delete existing book: http://localhost:9291/books/{id}

### Prerequisite:
* Application uses docker in order to run the Kafka (Broker, Schema registry and Zookeeper).
* Make sure the docker is installed and running in your local machine.

### How to start the application and access the end points.
* Make sure you have docker running in your system.
* Docker compose file already exists in the application class path, Just run the command `docker compose up -d` from IDE terminal in order to create the kafka containers.
* Simply start the application.
* Once tha application is up and running, User need to register them in order to access the book end points.
* Use register new user rest EndPoint with below request body for registering a new user

`{
  "email": "<YourEmailid>",
  "password": "YourPassword"
  }`
* Once the registration is successful, user can hit the login rest EndPoint with below request body to get the access token.

`{
  "email": "<YourEmailid>",
  "password": "YourPassword"
  }`
* Use the access token returned from the Login request to access the book end points.
* User should send the access token as a Bearer token from the client (ex: postman), token will be validated first.
* If the token is not expired and valid, then user will be given access to requested resource.
* If the token is invalid or expired, User will be prompted with relevant error message.
* Please note that token will be expired automatically after 30 minutes.
* We have enabled H2 database console, hence books can be viewed into the H2 localhost client.
* In order to access H2 database, Once the application is up and running, we can open URL `http://localhost:9291/h2-console` and give the datasource url from the application.yaml file to connect.
* All the tables can be viewed from H2 localhost client and can be queried from there.
* We also have kafka producer and consumers, Kafka producers will produce the newly created book details to the topic which will be further consumed by the Kafka consumer.
* In both consumer and producer we are logging the information which can ve verified in the application logs.

### Examples:
* Create new book url and request body:
  `http://localhost:9291/books`


        {
          "title": "The Hoobit",
          "author": "Bilbo Baggins",
          "bookLanguage": "English",
          "price": 40.50
        }

* Update existing book url and request body:
  `http://localhost:9291/books/{id}


        {
          "title": "The lord of the rings",
          "author": "Frodo baggins",
          "bookLanguage": "English",
          "price": 40.50
        }

### Testing:
  * Unit tests are provided for testing the all rest EndPoints in controller.
  * Integration test is also added to test the end to end application flow while creating a new book.
  * Integration test also requires the kafka containers, hence before running the integration test please make sure that kafka containers are up and running.