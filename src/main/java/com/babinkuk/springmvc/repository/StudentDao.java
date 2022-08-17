package com.babinkuk.springmvc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.babinkuk.springmvc.models.CollegeStudent;

@Repository
public interface StudentDao extends CrudRepository<CollegeStudent, Integer> {

    public CollegeStudent findByEmailAddress(String emailAddress);
}
