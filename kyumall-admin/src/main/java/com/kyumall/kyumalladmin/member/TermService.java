package com.kyumall.kyumalladmin.member;

import com.kyumall.kyumalladmin.member.dto.SaveTermRequest;
import com.kyumall.kyumalladmin.member.dto.TermDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.repository.TermRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TermService {
  private final TermRepository termRepository;

  public Long createTerm(SaveTermRequest request) {
    return termRepository.save(request.toEntity()).getId();
  }

  @Transactional
  public void updateTerm(Long id, SaveTermRequest request) {
    Term term = termRepository.findById(id)
        .orElseThrow(() -> new KyumallException(ErrorCode.TERM_NOT_EXISTS));
    term.update(request.getName(), request.getOrdering(), request.getType(), request.getStatus());
  }

  public List<TermDto> searchTerms(String termName) {
    return termRepository.findByNameContainsOrderByOrdering(termName)
        .stream().map(TermDto::from).toList();
  }
}
