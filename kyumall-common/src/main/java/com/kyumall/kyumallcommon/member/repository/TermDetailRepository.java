package com.kyumall.kyumallcommon.member.repository;

import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.TermDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermDetailRepository extends JpaRepository<TermDetail, Long> {
  List<TermDetail> findByTerm(Term term);
}
