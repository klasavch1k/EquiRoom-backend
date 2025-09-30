package com.klasavchik.modelHorseProject.repository;

import com.klasavchik.modelHorseProject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE u.id = :id
    """)
    Optional<User> findByIdWithProfileRolesAndModels(Long id);

    @Query("""
        SELECT u FROM User u 
        LEFT JOIN FETCH u.profile p 
        LEFT JOIN FETCH u.userRoles ur 
        LEFT JOIN FETCH ur.role r
        WHERE u.email = :email
    """)
    Optional<User> findByEmailWithProfileAndRoles(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.profile")
    List<User> findAllWithProfile();
}

