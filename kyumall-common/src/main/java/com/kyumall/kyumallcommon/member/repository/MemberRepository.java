package com.kyumall.kyumallcommon.member.repository;

import com.kyumall.kyumallcommon.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {
  boolean existsByUsername(String username);
  boolean existsByEmail(String username);
  Optional<Member> findByUsername(String username);

  Optional<Member> findByEmail(String email);
  Optional<Member> findByUsernameAndEmail(String username, String email);
}
