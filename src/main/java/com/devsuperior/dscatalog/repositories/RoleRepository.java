package com.devsuperior.dscatalog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devsuperior.dscatalog.entities.Role;

//@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

}
