package com.adroit.hotlistmicroservice.repo;

import com.adroit.hotlistmicroservice.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDetailsRepository extends JpaRepository<UserDetails,String> {

    @Query("SELECT u.userName FROM UserDetails u WHERE u.userId = :userId")
    String findUserNameByUserId(@Param("userId") String userId);

    @Query("""
    SELECT u.userId
    FROM UserDetails u
    WHERE LOWER(u.userName) LIKE LOWER(CONCAT(:userName, '%'))
""")
    List<String> findUserIdsByUserName(@Param("userName") String userName);

}
