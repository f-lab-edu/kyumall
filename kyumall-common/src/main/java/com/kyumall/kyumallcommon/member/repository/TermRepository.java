package com.kyumall.kyumallcommon.member.repository;

import com.kyumall.kyumallcommon.member.entity.Term;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {
  List<Term> findAllByIdIn(List<Long> termIds);
}
