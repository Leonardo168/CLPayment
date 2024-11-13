package com.CLPayment.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMethod;

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
@Table(name = "requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(length = 36)
    private String id;
    @Column()
    private String endpoint;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestMethod method;
    @Column(length = 1000)
    private String body;
    @Column
    private int httpStatus;
    @Column
    private LocalDateTime responseDate;
    @Column(length = 1000)
    private String response;

    public RequestEntity(String endpoint, RequestMethod method, String body, int httpStatus, LocalDateTime responseDate) {
	this.id = UUID.randomUUID().toString();
	this.endpoint = endpoint;
	this.method = method;
	this.body = body;
	this.httpStatus = httpStatus;
	this.responseDate = responseDate;
	this.response = null;
    }
}
