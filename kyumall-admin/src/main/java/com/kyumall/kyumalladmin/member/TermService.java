package com.kyumall.kyumalladmin.member;

import com.kyumall.kyumalladmin.member.dto.SaveTermDetailRequest;
import com.kyumall.kyumalladmin.member.dto.SaveTermRequest;
import com.kyumall.kyumalladmin.member.dto.TermDetailDto;
import com.kyumall.kyumalladmin.member.dto.TermDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.TermDetail;
import com.kyumall.kyumallcommon.member.repository.TermDetailRepository;
import com.kyumall.kyumallcommon.member.repository.TermRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TermService {
  private final TermRepository termRepository;
  private final TermDetailRepository termDetailRepository;

  public Long createTerm(SaveTermRequest request) {
    return termRepository.save(request.toEntity()).getId();
  }

  @Transactional
  public void updateTerm(Long id, SaveTermRequest request) {
    Term term = findTermById(id);
    term.update(request.getName(), request.getOrdering(), request.getType(), request.getStatus());
  }

  private Term findTermById(Long id) {
    return termRepository.findById(id)
        .orElseThrow(() -> new KyumallException(ErrorCode.TERM_NOT_EXISTS));
  }

  public List<TermDto> searchTerms(String termName) {
    return termRepository.findByNameContainsOrderByOrdering(termName)
        .stream().map(TermDto::from).toList();
  }

  public Long createTermDetail(Long termId, SaveTermDetailRequest request) {
    Term term = findTermById(termId);
    TermDetail termDetail = request.toEntity(term);
    TermDetail saved = termDetailRepository.save(termDetail);
    return saved.getId();
  }

  @Transactional
  public void updateTermDetail(Long termId, Long termDetailId, SaveTermDetailRequest request) {
    TermDetail detail = termDetailRepository.findById(termDetailId)
        .orElseThrow(() -> new KyumallException(ErrorCode.TERM_DETAIL_NOT_EXISTS));

    if (!Objects.equals(detail.getTerm().getId(), termId)) {
      throw new KyumallException(ErrorCode.TERM_NOT_EXISTS);
    }

    detail.update(request.getTitle(), request.getContent(), request.getVersion());
  }

  public List<TermDetailDto> getTermDetailsByTermId(Long termId) {
    Term term = findTermById(termId);
    return termDetailRepository.findByTerm(term)
        .stream().map(TermDetailDto::from).toList();
  }
}
