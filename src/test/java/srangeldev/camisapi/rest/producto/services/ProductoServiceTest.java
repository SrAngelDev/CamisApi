package srangeldev.camisapi.rest.producto.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import srangeldev.camisapi.rest.productos.dto.ProductoRequestDTO;
import srangeldev.camisapi.rest.productos.dto.ProductoResponseDTO;
import srangeldev.camisapi.rest.productos.mapper.ProductoMapper;
import srangeldev.camisapi.rest.productos.models.EstadoProducto;
import srangeldev.camisapi.rest.productos.models.Producto;
import srangeldev.camisapi.rest.productos.repository.ProductoRepository;
import srangeldev.camisapi.rest.productos.exceptions.ProductoNotFound;
import srangeldev.camisapi.rest.productos.service.ProductoService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests de ProductoService")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private ProductoRequestDTO requestDTO;
    private ProductoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        producto = Producto.builder()
                .id(1L)
                .nombre("Camiseta Real Madrid")
                .equipo("Real Madrid")
                .talla("M")
                .descripcion("Camiseta oficial")
                .precio(50.0)
                .estado(EstadoProducto.DISPONIBLE)
                .build();

        requestDTO = new ProductoRequestDTO();
        requestDTO.setNombre("Camiseta Real Madrid");
        requestDTO.setEquipo("Real Madrid");
        requestDTO.setTalla("M");
        requestDTO.setDescripcion("Camiseta oficial");
        requestDTO.setPrecio(50.0);
        requestDTO.setEstado(EstadoProducto.DISPONIBLE);
        requestDTO.setImageUrl("url");

        responseDTO = new ProductoResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNombre("Camiseta Real Madrid");
        responseDTO.setEquipo("Real Madrid");
        responseDTO.setTalla("M");
        responseDTO.setDescripcion("Camiseta oficial");
        responseDTO.setPrecio(50.0);
        responseDTO.setEstado(EstadoProducto.DISPONIBLE);
        responseDTO.setImageUrl("url");
    }

    @Nested
    @DisplayName("crearProducto")
    class CrearProducto {

        @Test
        @DisplayName("Debería crear un producto correctamente")
        void crearProducto_ok() {
            when(productoMapper.toEntity(requestDTO)).thenReturn(producto);
            when(productoRepository.save(producto)).thenReturn(producto);
            when(productoMapper.toDTO(producto)).thenReturn(responseDTO);

            ProductoResponseDTO resultado = productoService.crearProducto(requestDTO);

            assertEquals(responseDTO.getNombre(), resultado.getNombre());
            verify(productoRepository, times(1)).save(producto);
        }
    }

    @Nested
    @DisplayName("listarProductos")
    class ListarProductos {

        @Test
        @DisplayName("Debería devolver todos los productos")
        void listarProductos_ok() {
            when(productoRepository.findAll()).thenReturn(List.of(producto));
            when(productoMapper.toDTO(producto)).thenReturn(responseDTO);

            List<ProductoResponseDTO> resultados = productoService.listarProductos();

            assertEquals(1, resultados.size());
            assertEquals("Camiseta Real Madrid", resultados.get(0).getNombre());
        }
    }

    @Nested
    @DisplayName("obtenerPorId")
    class ObtenerPorId {

        @Test
        @DisplayName("Debería devolver producto si existe")
        void obtenerPorId_ok() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
            when(productoMapper.toDTO(producto)).thenReturn(responseDTO);

            ProductoResponseDTO resultado = productoService.obtenerPorId(1L);

            assertEquals("Camiseta Real Madrid", resultado.getNombre());
        }

        @Test
        @DisplayName("Debería lanzar excepción si no existe")
        void obtenerPorId_notFound() {
            when(productoRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(ProductoNotFound.class, () -> productoService.obtenerPorId(2L));
        }
    }

    @Nested
    @DisplayName("actualizarProducto")
    class ActualizarProducto {

        @Test
        @DisplayName("Debería actualizar un producto existente")
        void actualizarProducto_ok() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
            when(productoRepository.save(producto)).thenReturn(producto);
            when(productoMapper.toDTO(producto)).thenReturn(responseDTO);

            ProductoResponseDTO resultado = productoService.actualizarProducto(1L, requestDTO);

            assertEquals("Camiseta Real Madrid", resultado.getNombre());
            verify(productoRepository, times(1)).save(producto);
        }

        @Test
        @DisplayName("Debería lanzar excepción si el producto no existe")
        void actualizarProducto_notFound() {
            when(productoRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(ProductoNotFound.class, () -> productoService.actualizarProducto(2L, requestDTO));
        }
    }

    @Nested
    @DisplayName("eliminarProducto")
    class EliminarProducto {

        @Test
        @DisplayName("Debería eliminar producto si existe")
        void eliminarProducto_ok() {
            when(productoRepository.existsById(1L)).thenReturn(true);

            productoService.eliminarProducto(1L);

            verify(productoRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Debería lanzar excepción si no existe")
        void eliminarProducto_notFound() {
            when(productoRepository.existsById(2L)).thenReturn(false);

            assertThrows(ProductoNotFound.class, () -> productoService.eliminarProducto(2L));
        }
    }

    @Nested
    @DisplayName("buscarPorNombre")
    class BuscarPorNombre {

        @Test
        @DisplayName("Debería devolver productos por nombre")
        void buscarPorNombre_ok() {
            when(productoRepository.findByNombreIgnoreCase("Camiseta Barça")).thenReturn(List.of(producto));
            when(productoMapper.toDTO(producto)).thenReturn(responseDTO);

            List<ProductoResponseDTO> resultados = productoService.buscarPorNombre("Camiseta Barça");

            assertEquals(1, resultados.size());
        }
    }

    @Nested
    @DisplayName("buscarPorEquipo")
    class BuscarPorEquipo {

        @Test
        @DisplayName("Debería devolver productos por equipo")
        void buscarPorEquipo_ok() {
            when(productoRepository.findByEquipoIgnoreCase("Barcelona")).thenReturn(List.of(producto));
            when(productoMapper.toDTO(producto)).thenReturn(responseDTO);

            List<ProductoResponseDTO> resultados = productoService.buscarPorEquipo("Barcelona");

            assertEquals(1, resultados.size());
        }
    }

    @Nested
    @DisplayName("buscarPorEstado")
    class BuscarPorEstado {

        @Test
        @DisplayName("Debería devolver productos por estado")
        void buscarPorEstado_ok() {
            when(productoRepository.findByEstado(EstadoProducto.DISPONIBLE)).thenReturn(List.of(producto));
            when(productoMapper.toDTO(producto)).thenReturn(responseDTO);

            List<ProductoResponseDTO> resultados = productoService.buscarPorEstado(EstadoProducto.DISPONIBLE);

            assertEquals(1, resultados.size());
        }
    }
}

