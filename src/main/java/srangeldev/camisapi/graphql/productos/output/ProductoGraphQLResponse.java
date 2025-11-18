package srangeldev.camisapi.graphql.productos.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Output GraphQL simple para producto (DAW)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoGraphQLResponse {

    private String id;
    private String nombre;
    private String equipo;
    private String descripcion;
    private Double precio;
    private String imageUrl;
    private String estado;
    private String talla;
    private String fechaCreacion;
    private Boolean esDisponible;

    // Método para calcular disponibilidad
    public Boolean getEsDisponible() {
        return "DISPONIBLE".equals(this.estado);
    }
}
