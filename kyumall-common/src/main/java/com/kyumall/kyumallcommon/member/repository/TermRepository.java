package com.kyumall.kyumallcommon.member.repository;

import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {
  @EntityGraph(attributePaths = {"termDetails"})
  List<Term> findAllByStatus(TermStatus status);
}
