package com.kyumall.kyumallcommon.member.repository;

import com.kyumall.kyumallcommon.member.entity.Term;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TermRepository extends JpaRepository<Term, Long> {
  @Query("select t from Term t "
      + "where t.id in :termIds "
      + "and t.status = 'INUSE'")
  List<Term> findAllByIdIn(List<Long> termIds);

  @Query("select t from Term t "
      + "where t.status = 'INUSE'")
  List<Term> findAllTermsInUse();
}
