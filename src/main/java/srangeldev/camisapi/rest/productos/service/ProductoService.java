package srangeldev.camisapi.rest.productos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHandler;
import srangeldev.camisapi.rest.productos.dto.ProductoRequestDTO;
import srangeldev.camisapi.rest.productos.dto.ProductoResponseDTO;
import srangeldev.camisapi.rest.productos.exceptions.ProductoNotFound;
import srangeldev.camisapi.rest.productos.mapper.ProductoMapper;
import srangeldev.camisapi.rest.productos.models.EstadoProducto;
import srangeldev.camisapi.rest.productos.models.Producto;
import srangeldev.camisapi.rest.productos.repository.ProductoRepository;


import java.util.List;

@Service
@CacheConfig (cacheNames = {"productos"})
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final WebSocketHandler productosWebSocketHandler;
    private final srangeldev.camisapi.websocket.config.WebSocketHandler webSocketHandler;

    @Autowired
    public ProductoService(ProductoRepository repository, ProductoMapper mapper, WebSocketHandler productosWebSocketHandler, srangeldev.camisapi.websocket.config.WebSocketHandler webSocketHandler) {
        this.productoRepository = repository;
        this.productoMapper = mapper;
        this.productosWebSocketHandler = productosWebSocketHandler;
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * Devuelve todos los productos del catálogo.
     */
    @Cacheable("productos")
    public List<ProductoResponseDTO> listarProductos() {
        List<ProductoResponseDTO> productos = productoRepository.findAll()
                .stream()
                .map(productoMapper::toDTO)
                .toList();

        webSocketHandler.enviarMensajeATodos("Productos listados correctamente");

        return productos;
    }


    /**
     * Busca un producto por su ID.
     */
    @Cacheable(key = "#id")
    public ProductoResponseDTO obtenerPorId(String id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFound("No se encontró el producto con ID: " + id));

        webSocketHandler.enviarMensajeATodos("Producto obtenido con id:" +id );
        return productoMapper.toDTO(producto);
    }

    /**
     * Crea un nuevo producto.
     * Si no se especifica fechaCreacion, se asigna la fecha actual.
     */
    @CacheEvict(key = "#result.id")
    public ProductoResponseDTO crearProducto(ProductoRequestDTO dto) {
        Producto producto = productoMapper.toEntity(dto);

        Producto guardado = productoRepository.save(producto);

        webSocketHandler.enviarMensajeATodos("Producto creado:" +producto.getNombre());
        return productoMapper.toDTO(guardado);
    }

    /**
     * Actualiza un producto existente.
     */
    @CacheEvict(key = "#id")
    public ProductoResponseDTO actualizarProducto(String id, ProductoRequestDTO dto) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFound("No se puede actualizar. No existe el producto con ID: " + id));

        existente.setNombre(dto.getNombre());
        existente.setEquipo(dto.getEquipo());
        existente.setTalla(dto.getTalla());
        existente.setDescripcion(dto.getDescripcion());
        existente.setPrecio(dto.getPrecio());
        existente.setImageUrl(dto.getImageUrl());
        existente.setEstado(dto.getEstado());

        Producto actualizado = productoRepository.save(existente);

        webSocketHandler.enviarMensajeATodos("Producto actualizado con id:" +id );
        return productoMapper.toDTO(actualizado);
    }

    /**
     * Elimina un producto por su ID.
     */
    @CacheEvict(key = "#id")
    public void eliminarProducto(String id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(()-> new ProductoNotFound("No se encontro el producto con ID: " + id));

        productoRepository.deleteById(id);
        webSocketHandler.enviarMensajeATodos("Producto eliminado con id" +id );
    }

    /**
     * Busca productos por nombre
     */
    @Cacheable(key = "#nombre")
    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreIgnoreCase(nombre)
                .stream()
                .map(productoMapper::toDTO)
                .toList();
    }

    /**
     * Busca productos por equipo.
     */
    @Cacheable(key = "#equipo")
    public List<ProductoResponseDTO> buscarPorEquipo(String equipo) {
        return productoRepository.findByEquipoIgnoreCase(equipo)
                .stream()
                .map(productoMapper::toDTO)
                .toList();
    }

    /**
     * Busca productos por estado (DISPONIBLE, RESERVADO, VENDIDO).
     */
    @Cacheable(key = "#estado")
    public List<ProductoResponseDTO> buscarPorEstado(EstadoProducto estado) {
        return productoRepository.findByEstado(estado)
                .stream()
                .map(productoMapper::toDTO)
                .toList();
    }

    /**
     * Busca productos por talla (S, X, M, L, XL, XXL)
     */
    @Cacheable(key = "#talla")
    public List<ProductoResponseDTO> buscarPorTalla(String talla) {
        return productoRepository.findByTalla(talla)
                .stream()
                .map(productoMapper::toDTO)
                .toList();
    }
}
