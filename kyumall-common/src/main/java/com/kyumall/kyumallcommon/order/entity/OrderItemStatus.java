package com.kyumall.kyumallcommon.order.entity;

public enum OrderItemStatus {
  BEFORE_PAY      // 결제 전
  , PAY_COMPLETE  // 결재 완료
  , PREPARING_FOR_SHIPMENT // 상품 준비중
  , DELIVERING    // 배송 중
  , DELIVER_COMPLETE  // 배송 완료
  , PURCHASE_CONFIRM  // 구매 확정
  , ORDER_CANCELED // 주문 취소
  , REFUND_COMPLETE // 환불 완료
}
