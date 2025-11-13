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
    Optional<User> findByProfile_NicknameIgnoreCase(String nickname);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile LEFT JOIN FETCH u.userRoles WHERE u.id = :id")
    Optional<User> findByIdWithProfileRolesAndModels(@Param("id") Long id);

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
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.profile p WHERE p.nickname = :nickname")
    Boolean existsByNickname(@Param("nickname") String nickname);


    @Query("SELECT new com.klasavchik.modelHorseProject.dto.UserSearchDTO(" +
            "u.id, p.firstName, p.lastName, p.nickname, p.avatar, COUNT(m.id)) " +
            "FROM User u JOIN u.profile p LEFT JOIN Model m ON m.owner = u " +
            "WHERE LOWER(p.firstName) LIKE :search OR LOWER(p.lastName) LIKE :search OR LOWER(p.nickname) LIKE :search " +
            "GROUP BY u.id, p.firstName, p.lastName, p.nickname, p.avatar " +
            "ORDER BY u.id ASC")
    Page<UserSearchDTO> findBySearch(@Param("search") String search, Pageable pageable);
    
}

