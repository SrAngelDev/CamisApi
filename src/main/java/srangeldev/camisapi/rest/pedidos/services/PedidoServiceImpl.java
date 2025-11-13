package srangeldev.camisapi.rest.pedidos.services;

import org.springframework.stereotype.Service;
import srangeldev.camisapi.rest.pedidos.dto.PedidoRequestDto;
import srangeldev.camisapi.rest.pedidos.dto.PedidoResponseDto;
import srangeldev.camisapi.rest.pedidos.mappers.PedidoMappers;
import srangeldev.camisapi.rest.pedidos.models.EstadoPedido;
import srangeldev.camisapi.rest.pedidos.models.Pedido;
import srangeldev.camisapi.rest.pedidos.repository.PedidoRepository;

import java.time.LocalDateTime;
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

    public PedidoServiceImpl(PedidoRepository pedidoRepository, PedidoMappers pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
    }
    // Creamos un nuevo pedido con estado PENDIENTE_PAGO
    @Override
    public PedidoResponseDto crearPedido(PedidoRequestDto pedidoRequest) {
        Pedido pedido = pedidoMapper.toPedido(pedidoRequest);
        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        pedido.setCreatedAt(LocalDateTime.now());
        Pedido saved = pedidoRepository.save(pedido);
        return pedidoMapper.toResponseDto(saved);
    }
    // Devuelve todos los pedidos
    @Override
    public List<PedidoResponseDto> listarPedidos() {
        return pedidoMapper.toResponseList(pedidoRepository.findAll());
    }

    // Obtiene los pedidos del usuario específico
    @Override
    public List<PedidoResponseDto> obtenerPorUsuario(String userId) {
        List<Pedido> pedidos = pedidoRepository.findByUserId(userId);
        return pedidoMapper.toResponseList(pedidos);
    }
    // Buscamos el pedido por su Id
    @Override
    public Optional<PedidoResponseDto> obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .map(pedidoMapper::toResponseDto);
    }
    // Actualizamos el estado del pedido y registra fechas de pago y envío
    @Override
    public Optional<PedidoResponseDto> actualizarEstado(Long pedidoId, EstadoPedido estado) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isEmpty()) return Optional.empty();
        Pedido pedido = pedidoOpt.get();
        pedido.setEstado(estado);
        if (estado == EstadoPedido.PAGADO) {
            pedido.setFechaPago(LocalDateTime.now());
        } else if (estado == EstadoPedido.ENVIADO) {
            pedido.setFechaEnvio(LocalDateTime.now());
        }

        Pedido actualizado = pedidoRepository.save(pedido);
        return Optional.of(pedidoMapper.toResponseDto(actualizado));
    }
    // Pedidos filtrados por estado
    @Override
    public List<PedidoResponseDto> obtenerPorEstado(EstadoPedido estado) {
        return pedidoMapper.toResponseList(pedidoRepository.pedidosPorEstado(estado));
    }
    //Eliminamos el pedido correspondiente al Id
    @Override
    public void eliminarPedido(Long id) {
        pedidoRepository.deleteById(id);
    }
}
