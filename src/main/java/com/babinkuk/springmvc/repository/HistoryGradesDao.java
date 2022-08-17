package com.babinkuk.springmvc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.babinkuk.springmvc.models.HistoryGrade;

@Repository
public interface HistoryGradesDao extends CrudRepository<HistoryGrade, Integer> {

    public Iterable<HistoryGrade> findGradeByStudentId (int id);

    public void deleteByStudentId(int id);
}
