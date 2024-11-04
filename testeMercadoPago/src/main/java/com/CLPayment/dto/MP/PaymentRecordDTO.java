package com.CLPayment.dto.MP;

import java.util.List;

public record PaymentRecordDTO(AdditionalInfoRecordDTO additional_info, String external_reference, String status, double transaction_amount) {

    public record AdditionalInfoRecordDTO(List<ItemRecordDTO> items) {}
}
