package com.docappoint.repository;

import com.docappoint.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByName(String name);

    Optional<User> findByPhone(String phone);

    Optional<User> findByNameOrPhone(String name, String phone);

    boolean existsByName(String name);

    boolean existsByPhone(String phone);

    boolean existsByNameOrPhone(String name, String phone);
}
