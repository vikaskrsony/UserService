package com.vikas.UserService.Repository;

import com.vikas.UserService.Models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByTokenAndUser_id(String token, Long userID);

    @Query("SELECT s FROM Session s WHERE s.user.id = :userId AND s.sessionStatus = com.vikas.UserService.Models.SessionStatus.ACTIVE")
    Optional<Session> findSessionWhereSessionStatusIsActive(@Param("userId") Long userId);
}
