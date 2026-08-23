package com.ayshriv.salescrm.user.repository;

import com.ayshriv.salescrm.user.entity.EUserType;
import com.ayshriv.salescrm.user.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserTypeRepository extends JpaRepository<UserType, Long> {

    Optional<UserType> findByName(EUserType name);

    Optional<UserType> findByNameAndIsDeletedFalse(EUserType name);
}
