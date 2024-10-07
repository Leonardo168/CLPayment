package webClient.mercadoPago.teste.dto;

public record PagamentoRecordDTO(AdditionalInfoRecordDTO additional_info, String external_reference, String status, double transaction_amount) {

}
