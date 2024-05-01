package com.kyumall.kyumallcommon.order.vo;

public enum OrderStatus {
  BEFORE_PAY      // 결제 전
  , PAY_COMPLETE  // 결재 완료
  , DELIVERING    // 배송 중
  , DELIVER_COMPLETE  // 배송 완료
  , PURCHASE_CONFIRM  // 구매 확정
  // 배송지연
  // 취소
  // 환불
}
