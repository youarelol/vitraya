package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.dto.response.UserDto;
import com.vitraya.adjudication.engine.mysql.entity.Users;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<Users, Long> {
    Optional<Users> findByUsername(String username);

    @Query("SELECT * FROM users WHERE token = :token")
    Users findByToken(@Param("token") String token);

    @Query("SELECT * FROM users WHERE refresh_token = :refreshToken")
    Users findByRefreshToken(@Param("refreshToken") String refreshToken);

    @Query(value = """
    SELECT 
        CASE 
            WHEN r.role_name = 'ADMIN' THEN true
            ELSE false
        END AS is_admin_insurance_user
    FROM users u
    JOIN roles r ON u.role = r.id
    WHERE u.id = :userId""")
    boolean isAuthorizedForUserDashboard(@Param("userId") long userId);



    @Query("SELECT u.username, r.role_name AS role, u.date_created, u.date_updated " +
            "FROM users u " +
            "LEFT JOIN roles r ON u.role = r.id " +
            "WHERE u.corporate_id = :corporate_id")
    List<UserDto> findAllUserForDashboard(@Param("corporate_id") int corporateId);


}
