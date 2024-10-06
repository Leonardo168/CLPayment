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
    String id_preferencia_mp;
    @Column
    String user_id;
    @Column
    String id_ficha;
    @Column
    int qtde_ficha;
    @Column
    double valorTotal;
    @Column
    String created_by;
    @Column
    String updated_by;
    @Column
    String expiration_date;
    @Column
    String status;
}
