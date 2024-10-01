package webClient.mercadoPago.teste.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_ORDER")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderModel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    String id;
    @Column
    String identificationType;
    @Column
    String identificationNumber;
}
