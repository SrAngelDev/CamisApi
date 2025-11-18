package srangeldev.camisapi.rest.pedidos.dto;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import srangeldev.camisapi.rest.pedidos.models.EstadoPedido;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO PedidoRequestDTO
 *
 * Utilizado para crear un nuevo pedido desde el cliente.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PedidoRequestDto {
    /**
     * ID del carrito a partir del cual se creará el pedido
     * El servicio obtendrá los productos del carrito automáticamente
     */
    @NotNull(message = "El ID del carrito no puede ser nulo")
    @Positive(message = "El ID del carrito debe ser positivo")
    private Long carritoId;

    /**
     * Dirección de envío del pedido
     */
    @NotNull(message = "La dirección de envío no puede ser nula")
    @Size(min = 10, max = 500, message = "La dirección debe tener entre 10 y 500 caracteres")
    private String direccionEnvio;
}
