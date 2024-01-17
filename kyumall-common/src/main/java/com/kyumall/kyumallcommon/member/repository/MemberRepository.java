package com.kyumall.kyumallcommon.member.repository;

import com.kyumall.kyumallcommon.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
  boolean existsByUsername(String username);
  boolean existsByEmail(String username);
}
