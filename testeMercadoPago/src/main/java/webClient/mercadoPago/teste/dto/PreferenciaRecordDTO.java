package webClient.mercadoPago.teste.dto;

public record PreferenciaRecordDTO(ItemRecordDTO[] items, PayerRecordDTO payer, BackUrlRecordDTO back_urls,
				   String notification_url, String id, String sandbox_init_point) {

    public PreferenciaRecordDTO(ItemRecordDTO[] itemRecordDTOs, PayerRecordDTO payerRecordDTO,
				BackUrlRecordDTO backUrlRecordDTO, String notification_url) {
	this(itemRecordDTOs, payerRecordDTO, backUrlRecordDTO, notification_url, null, null);
    }
}