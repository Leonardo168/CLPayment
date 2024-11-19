package com.CLPayment.dto.Efi;

public record PixRecordDTO(String valor, PagadorRecordDTO pagador, FavorecidoRecordDTO favorecido) {

    public record PagadorRecordDTO(String chave, String infoPagador) {}
    
    public record FavorecidoRecordDTO(String chave) {}
}
