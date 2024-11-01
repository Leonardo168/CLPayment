package com.CLPayment.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.CLPayment.enums.TransactionStatus;
import com.CLPayment.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String transaction_id;
    @Column
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @Column
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    @Column
    private String inventory_id;
    @Column
    private int chips_qty;
    @Column
    private String tournment_id_riot;
    @Column
    private String preference_id_mp;
    @Column
    private String payment_id_mp;
    @Column
    private LocalDateTime creation_date;
    @Column
    private LocalDateTime update_date;
    @Column
    private LocalDateTime expiration_date;

    public TransactionModel(String transaction_id, TransactionType type, TransactionStatus status, String inventory_id,
			    int chips_qty, String preference_id_mp, LocalDateTime creation_date,
			    LocalDateTime update_date, LocalDateTime expiration_date) {
	this.transaction_id = transaction_id;
	this.type = type;
	this.status = status;
	this.inventory_id = inventory_id;
	this.chips_qty = chips_qty;
	this.tournment_id_riot = null;
	this.preference_id_mp = preference_id_mp;
	this.payment_id_mp = null;
	this.creation_date = creation_date;
	this.update_date = update_date;
	this.expiration_date = expiration_date;
    }
    
    public TransactionModel(String transaction_id, TransactionType type, TransactionStatus status, String inventory_id,
			    int chips_qty, LocalDateTime creation_date,
			    LocalDateTime update_date, LocalDateTime expiration_date) {
	this.transaction_id = transaction_id;
	this.type = type;
	this.status = status;
	this.inventory_id = inventory_id;
	this.chips_qty = chips_qty;
	this.tournment_id_riot = null;
	this.preference_id_mp = null;
	this.payment_id_mp = null;
	this.creation_date = creation_date;
	this.update_date = update_date;
	this.expiration_date = expiration_date;
    }

}
