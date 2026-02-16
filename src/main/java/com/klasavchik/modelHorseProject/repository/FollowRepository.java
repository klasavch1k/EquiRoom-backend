package com.klasavchik.modelHorseProject.repository;

import com.klasavchik.modelHorseProject.dto.UserSearchDTO;
import com.klasavchik.modelHorseProject.entity.user.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followed.id = :userId")
    long countFollowers(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    long countFollowing(@Param("userId") Long userId);

    @Query("SELECT new com.klasavchik.modelHorseProject.dto.UserSearchDTO(" +
            "u.id, p.firstName, p.lastName, p.nickname, p.avatar, COUNT(m.id)) " +
            "FROM Follow f JOIN f.follower u JOIN u.profile p LEFT JOIN Model m ON m.owner = u " +
            "WHERE f.followed.id = :userId AND (LOWER(p.firstName) LIKE :search OR LOWER(p.lastName) LIKE :search OR LOWER(p.nickname) LIKE :search) " +
            "GROUP BY u.id, p.firstName, p.lastName, p.nickname, p.avatar, f.followedAt " +
            "ORDER BY f.followedAt DESC")
    Page<UserSearchDTO> findFollowers(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

    @Query("SELECT new com.klasavchik.modelHorseProject.dto.UserSearchDTO(" +
            "u.id, p.firstName, p.lastName, p.nickname, p.avatar, COUNT(m.id)) " +
            "FROM Follow f JOIN f.followed u JOIN u.profile p LEFT JOIN Model m ON m.owner = u " +
            "WHERE f.follower.id = :userId AND (LOWER(p.firstName) LIKE :search OR LOWER(p.lastName) LIKE :search OR LOWER(p.nickname) LIKE :search) " +
            "GROUP BY u.id, p.firstName, p.lastName, p.nickname, p.avatar, f.followedAt " +
            "ORDER BY f.followedAt DESC")
    Page<UserSearchDTO> findFollowing(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);
}