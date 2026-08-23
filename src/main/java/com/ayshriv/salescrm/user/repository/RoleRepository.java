package com.ayshriv.salescrm.user.repository;

import com.ayshriv.salescrm.user.entity.ERole;
import com.ayshriv.salescrm.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);

    Optional<Role> findByNameAndIsDeletedFalse(ERole name);
}
