package srangeldev.camisapi.rest.pedidos.services;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srangeldev.camisapi.rest.carrito.models.Carrito;
import srangeldev.camisapi.rest.carrito.repository.CarritoRepository;
import srangeldev.camisapi.rest.carrito.Exceptions.CarritoNotFound;
import srangeldev.camisapi.rest.pedidos.dto.PedidoRequestDto;
import srangeldev.camisapi.rest.pedidos.dto.PedidoResponseDto;
import srangeldev.camisapi.rest.pedidos.exceptions.PedidoConflictException;
import srangeldev.camisapi.rest.pedidos.exceptions.PedidoNotFoundException;
import srangeldev.camisapi.rest.pedidos.exceptions.PedidoBadRequestException;
import srangeldev.camisapi.rest.pedidos.mappers.PedidoMappers;
import srangeldev.camisapi.rest.pedidos.models.DetallePedido;
import srangeldev.camisapi.rest.pedidos.models.EstadoPedido;
import srangeldev.camisapi.rest.pedidos.models.Pedido;
import srangeldev.camisapi.rest.pedidos.repository.PedidoRepository;
import srangeldev.camisapi.rest.productos.models.Producto;
import srangeldev.camisapi.rest.productos.repository.ProductoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio
 *
 * Utiliza PedidoRepository para interactuar con la base de datos
 * y PedidoMappers para transformar entre entidades y DTOs.
 */
@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMappers pedidoMapper;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, PedidoMappers pedidoMapper, 
                             CarritoRepository carritoRepository, ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
    }

    // Creamos un nuevo pedido a partir de un carrito
    @Override
    @Transactional //Así si algo falla se revierte todo
    public PedidoResponseDto crearPedido(PedidoRequestDto pedidoRequest) {
        // 1. Obtener el carrito
        Carrito carrito = carritoRepository.findById(pedidoRequest.getCarritoId())
                .orElseThrow(() -> new CarritoNotFound(pedidoRequest.getCarritoId()));
        
        // 2. Validar que el carrito tenga productos
        if (carrito.getProductosIds() == null || carrito.getProductosIds().isEmpty()) {
            throw new PedidoBadRequestException("El carrito está vacío");
        }
        
        // 3. Obtener los productos del carrito y crear detalles
        List<DetallePedido> detalles = new ArrayList<>();
        double total = 0.0;
        
        for (String productoId : carrito.getProductosIds()) {
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new PedidoBadRequestException("Producto no encontrado: " + productoId));
            
            // Crear detalle del pedido con snapshot del producto
            DetallePedido detalle = DetallePedido.builder()
                    .productoId(producto.getId())
                    .nombre(producto.getNombre())
                    .talla(producto.getTalla())
                    .equipo(producto.getEquipo())
                    .precioPagado(producto.getPrecio())
                    .imageUrl(producto.getImageUrl())
                    .build();
            
            detalles.add(detalle);
            total += producto.getPrecio();
        }
        
        // 4. Crear el pedido
        Pedido pedido = pedidoMapper.toPedido(
                carrito.getUserId(),
                carrito.getId(),
                pedidoRequest.getDireccionEnvio(),
                total,
                detalles
        );
        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        pedido.setCreatedAt(LocalDateTime.now());
        
        // 5. Guardar el pedido
        Pedido saved = pedidoRepository.save(pedido);
        
        // 6. Vaciar el carrito después de crear el pedido
        carrito.getProductosIds().clear();
        carritoRepository.save(carrito);
        
        return pedidoMapper.toResponseDto(saved);
    }

    // Devuelve todos los pedidos
    @Override
    public List<PedidoResponseDto> listarPedidos() {
        return pedidoMapper.toResponseList(pedidoRepository.findAll());
    }

    // Obtiene los pedidos del usuario específico
    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDto> findByUsuario(Long userId) {
        List<Pedido> pedidos = pedidoRepository.findByUserId(userId);
        return pedidoMapper.toResponseList(pedidos);
    }

    // Buscamos el pedido por su Id
    @Override
    public PedidoResponseDto obtenerPorId(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNotFoundException("Pedido con ID " + pedidoId + " no encontrado"));
                return pedidoMapper.toResponseDto(pedido);
    }

    // Actualizamos el estado del pedido y registra fechas de pago y envío
    @Override
    @Transactional
    public PedidoResponseDto actualizarEstado(Long pedidoId, EstadoPedido estado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNotFoundException("Pedido con ID " + pedidoId + " no encontrado"));

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new PedidoConflictException("No es posible modificar un pedido cancelado");
        }
        if (!cambiosDeEstado(pedido.getEstado(), estado)) {
            throw new PedidoConflictException("No puedes cambiar del estado " + pedido.getEstado() + " a " + estado);
        }
        pedido.setEstado(estado);
        switch (estado) {
            case PAGADO -> pedido.setFechaPago(LocalDateTime.now());
            case ENVIADO -> pedido.setFechaEnvio(LocalDateTime.now());
            default -> {
            }
        }
        Pedido actualizado = pedidoRepository.save(pedido);
        return pedidoMapper.toResponseDto(actualizado);
    }
    //Posibles opcones en la actualizacion de los estados
    private boolean cambiosDeEstado(EstadoPedido estadoActual, EstadoPedido nuevoEstado) {
                return switch (estadoActual){
                    case PENDIENTE_PAGO -> nuevoEstado == EstadoPedido.PAGADO || nuevoEstado == EstadoPedido.CANCELADO;
                    case PAGADO -> nuevoEstado == EstadoPedido.ENVIADO || nuevoEstado == EstadoPedido.CANCELADO;
                    case ENVIADO -> nuevoEstado == EstadoPedido.ENTREGADO;

                    default -> false;
                };
    }

    // Pedidos filtrados por estado
    @Override
    public List<PedidoResponseDto> buscarPorEstado(EstadoPedido estado) {
        return pedidoMapper.toResponseList(pedidoRepository.findByEstado(estado));
    }

    //Eliminamos el pedido correspondiente
    @Override
    public void eliminarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id) //validamos se esta el pedido
                .orElseThrow(() -> new PedidoNotFoundException("Pedido con ID " + id + " no encontrado"));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) { // Si El pedido esta Enviado, Pagado u otro que no este en proceso. NO SE PUEDE ELIMINAR
            throw new PedidoConflictException("No se puede eliminar un pedido procesado");
        }
        pedidoRepository.delete(pedido);
    }
}
