package webClient.mercadoPago.teste.dto;

public record PreferenciaRecordDTO(ItemRecordDTO[] items, PayerRecordDTO payer, BackUrlRecordDTO back_urls,
				   String notification_url, String sandbox_init_point) {

}
