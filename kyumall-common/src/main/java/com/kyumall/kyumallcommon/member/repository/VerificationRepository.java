package com.kyumall.kyumallcommon.member.repository;

import com.kyumall.kyumallcommon.member.entity.Verification;
import com.kyumall.kyumallcommon.member.vo.VerificationStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
  @Query("select v "
      + "from Verification v "
      + "where v.contact = :email "
      + "and v.status = 'UNVERIFIED'")
  Optional<Verification> findUnverifiedByContact(String email);

  boolean existsByContactAndStatus(String contact, VerificationStatus status);

  Optional<Verification> findByContact(String email);
}
