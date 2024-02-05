package com.kyumall.kyumalladmin.member;

import com.kyumall.kyumalladmin.member.dto.SaveTermDetailRequest;
import com.kyumall.kyumalladmin.member.dto.SaveTermRequest;
import com.kyumall.kyumalladmin.member.dto.TermDetailDto;
import com.kyumall.kyumalladmin.member.dto.TermDto;
import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("http://localhost:3000")
@RequiredArgsConstructor
@RequestMapping("/terms")
@RestController
public class TermController {
  private final TermService termService;

  /**
   * 약관을 추가합니다.
   * @param request
   * @return
   */
  @PostMapping
  public ResponseWrapper<CreatedIdDto> createTerm(@Valid @RequestBody SaveTermRequest request) {
    return ResponseWrapper.ok(CreatedIdDto.of(termService.createTerm(request)));
  }

  /**
   * 약관을 수정합니다.
   * @param id 약관 ID
   * @param request
   * @return
   */
  @PutMapping("/{id}")
  public ResponseWrapper<Void> updateTerm(@PathVariable Long id, @Valid @RequestBody SaveTermRequest request) {
    termService.updateTerm(id, request);
    return ResponseWrapper.ok();
  }

  /**
   *  약관명으로 약관을 검색합니다.
   * @param termName 약관명
   * @return
   */
  @GetMapping
  public ResponseWrapper<List<TermDto>> searchTerms(@RequestParam String termName) {
    return ResponseWrapper.ok(termService.searchTerms(termName));
  }

  /**
   * 약관 상세를 생성합니다.
   * @param termId 약관 ID
   * @param request
   * @return
   */
  @PostMapping("/{termId}/details")
  public ResponseWrapper<CreatedIdDto> createTermDetail(@PathVariable Long termId, @Valid @RequestBody
      SaveTermDetailRequest request) {
    return ResponseWrapper.ok(CreatedIdDto.of(termService.createTermDetail(termId, request)));
  }

  /**
   * 약관 상세를 수정합니다.
   * @param termId
   * @param termDetailId
   * @param request
   * @return
   */
  @PutMapping("/{termId}/details/{termDetailId}")
  public ResponseWrapper<Void> updateTermDetail(@PathVariable Long termId, @PathVariable Long termDetailId
      , @Valid @RequestBody SaveTermDetailRequest request) {
    termService.updateTermDetail(termId, termDetailId, request);
    return ResponseWrapper.ok();
  }

  /**
   * 약관에 해당하는 약관 상세를 조회합니다.
   * @param termId
   * @return
   */
  @GetMapping("/{termId}/details")
  public ResponseWrapper<List<TermDetailDto>> getTermDetailsByTerm(@PathVariable Long termId) {
    return ResponseWrapper.ok(termService.getTermDetailsByTermId(termId));
  }
}
