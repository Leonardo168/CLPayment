package com.CLPayment.dto.MP;

public record PaymentRecordDTO(AdditionalInfoRecordDTO additional_info, String external_reference, String status, double transaction_amount) {

}
