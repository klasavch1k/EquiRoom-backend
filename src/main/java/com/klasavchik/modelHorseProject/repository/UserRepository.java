package com.klasavchik.modelHorseProject.repository;

import com.klasavchik.modelHorseProject.dto.UserSearchDTO;
import com.klasavchik.modelHorseProject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query
    Boolean existsByEmail(String email);

    @Query("SELECT new com.klasavchik.modelHorseProject.dto.UserSearchDTO(" +
            "u.id, p.firstName, p.lastName, p.nickName, p.avatar, COUNT(m.id)) " +
            "FROM User u JOIN u.profile p LEFT JOIN Model m ON m.owner = u " +
            "WHERE LOWER(p.firstName) LIKE :search OR LOWER(p.lastName) LIKE :search OR LOWER(p.nickName) LIKE :search " +
            "GROUP BY u.id, p.firstName, p.lastName, p.nickName, p.avatar " +
            "ORDER BY u.id ASC")
    Page<UserSearchDTO> findBySearch(@Param("search") String search, Pageable pageable);
}

