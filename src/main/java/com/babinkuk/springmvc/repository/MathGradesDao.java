package com.babinkuk.springmvc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.babinkuk.springmvc.models.MathGrade;

@Repository
public interface MathGradesDao extends CrudRepository<MathGrade, Integer> {

    public Iterable<MathGrade> findGradeByStudentId (int id);

    public void deleteByStudentId(int id);
}
