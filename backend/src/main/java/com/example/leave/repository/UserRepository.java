package com.example.leave.repository;

import com.example.leave.model.User;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query(value = "SELECT u.id FROM users u WHERE u.department_id IN (SELECT d.id FROM departments d WHERE d.manager_id = :managerId)",
           nativeQuery = true)
    List<Long> findSubordinateIdsByManagerId(@Param("managerId") Long managerId);
}
