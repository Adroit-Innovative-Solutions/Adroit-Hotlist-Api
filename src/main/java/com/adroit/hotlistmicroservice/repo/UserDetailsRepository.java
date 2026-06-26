package com.adroit.hotlistmicroservice.repo;

import com.adroit.hotlistmicroservice.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDetailsRepository extends JpaRepository<UserDetails,Integer> {

    @Query("SELECT u.userName FROM UserDetails u WHERE u.userId = :userId")
    String findUserNameByUserId(@Param("userId") String userId);

}
