package srangeldev.camisapi.graphql.pedidos.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Output GraphQL simple para pedido (DAW)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoGraphQLResponse {

    private String id;
    private String usuario;
    private String fechaPedido;
    private Double total;
    private String estado;
    private List<String> detalles;
    private String fechaPago;
    private String fechaEnvio;
    private Boolean esPagado;
    private Boolean esEnviado;

    // Método para verificar si está pagado
    public Boolean getEsPagado() {
        return "PAGADO".equals(this.estado) || "ENVIADO".equals(this.estado) || "ENTREGADO".equals(this.estado);
    }

    // Método para verificar si está enviado
    public Boolean getEsEnviado() {
        return "ENVIADO".equals(this.estado) || "ENTREGADO".equals(this.estado);
    }
}
