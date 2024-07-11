package com.codewithsrb.BookManagement.repository;

import com.codewithsrb.BookManagement.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserInfo, String> {

    UserInfo findByEmail(String email);
}
