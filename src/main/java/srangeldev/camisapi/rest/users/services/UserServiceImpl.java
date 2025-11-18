package srangeldev.camisapi.rest.users.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import srangeldev.camisapi.rest.users.dto.UserCreateRequestDto;
import srangeldev.camisapi.rest.users.dto.UserResponseDto;
import srangeldev.camisapi.rest.users.dto.UserUpdateRequestDto;
import srangeldev.camisapi.rest.users.exceptions.UserNotFound;
import srangeldev.camisapi.rest.users.mappers.UserMapper;
import srangeldev.camisapi.rest.users.models.User;
import srangeldev.camisapi.rest.users.repositories.UserRepository;
import srangeldev.camisapi.rest.users.exceptions.UserBadRequest;
import srangeldev.camisapi.websocket.config.MyWebSocketHandler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de usuarios
 * Siguiendo el patrón establecido por el profesor con MongoDB
 */
@Service
@Slf4j
@CacheConfig(cacheNames = {"users"})
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MyWebSocketHandler myWebSocketHandler;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, MyWebSocketHandler myWebSocketHandler) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.myWebSocketHandler = myWebSocketHandler;
    }

    @Override
    public List<UserResponseDto> findAll() {
        log.info("Buscando todos los usuarios");

        List<UserResponseDto> usuarios = userRepository.findAll().stream()
                .map(userMapper::toUsuarioResponseDto)
                .toList();

        myWebSocketHandler.enviarMensajeATodos("Usuarios listados correctamente");

        return usuarios;
    }

    @Override
    @Cacheable(key = "#id")
    public UserResponseDto findById(Long id) {
        log.info("Buscando usuario por id: {}", id);
        User user = userRepository.findByIdUsuario(id)
                .orElseThrow(() -> new UserNotFound("Usuario con id " + id + " no encontrado", true));

        myWebSocketHandler.enviarMensajeATodos("Usuario encontrado con id:" +id);
        return userMapper.toUsuarioResponseDto(user);
    }

    @Override
    public List<UserResponseDto> findByNombre(String nombre) {
        log.info("Buscando usuarios por nombre: {}", nombre);
        return userRepository.findByNombre(nombre).stream()
                .map(userMapper::toUsuarioResponseDto)
                .toList();
    }

    @Override
    @CachePut(key = "#result.id")
    public UserResponseDto save(UserCreateRequestDto userCreateRequestDto) {
        log.info("Guardando usuario: {}", userCreateRequestDto);
        
        // Verificar que no exista un usuario con el mismo username
        userRepository.findByUsername(userCreateRequestDto.getUsername()).ifPresent(u -> {
            throw new UserBadRequest("Ya existe un usuario con el username " + userCreateRequestDto.getUsername());
        });

        // Generar el siguiente ID de usuario automáticamente
        Long nextId = userRepository.findFirstByOrderByIdUsuarioDesc()
                .map(user -> user.getIdUsuario() + 1)
                .orElse(1L);
        
        log.info("Generando ID de usuario automático: {}", nextId);

        // Crear nuevo usuario
        User user = userMapper.toUsuario(userCreateRequestDto);
        user.setIdUsuario(nextId);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Guardar en MongoDB
        User savedUser = userRepository.save(user);
        
        myWebSocketHandler.enviarMensajeATodos("Nuevo usuario creado con id: " + nextId);
        
        return userMapper.toUsuarioResponseDto(savedUser);
    }

    @Override
    @CachePut(key = "#id")
    public UserResponseDto update(Long id, UserUpdateRequestDto userUpdateRequestDto) {
        log.info("Actualizando usuario con id {}: {}", id, userUpdateRequestDto);
        
        // Buscar usuario existente
        User existingUser = userRepository.findByIdUsuario(id)
                .orElseThrow(() -> new UserNotFound("Usuario con id " + id + " no encontrado", true));

        // Verificar username si se está cambiando
        if (userUpdateRequestDto.getUsername() != null && 
            !userUpdateRequestDto.getUsername().isEmpty() && 
            !userUpdateRequestDto.getUsername().equals(existingUser.getUsername())) {
            
            userRepository.findByUsername(userUpdateRequestDto.getUsername()).ifPresent(u -> {
                throw new UserBadRequest("Ya existe un usuario con el username " + userUpdateRequestDto.getUsername());
            });
            existingUser.setUsername(userUpdateRequestDto.getUsername());
        }

        // Actualizar campos si vienen en el DTO
        if (userUpdateRequestDto.getNombre() != null && !userUpdateRequestDto.getNombre().isEmpty()) {
            existingUser.setNombre(userUpdateRequestDto.getNombre());
        }
        if (userUpdateRequestDto.getRoles() != null && !userUpdateRequestDto.getRoles().isEmpty()) {
            existingUser.setRoles(userUpdateRequestDto.getRoles());
        }
        
        // Actualizar fecha de modificación
        existingUser.setUpdatedAt(LocalDateTime.now());

        // Guardar cambios en MongoDB
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toUsuarioResponseDto(updatedUser);
    }

    @Override
    @CacheEvict(key = "#id")
    public void deleteById(Long id) {
        log.info("Borrando usuario por id: {}", id);
        
        // Verificar que existe y obtener el usuario
        User user = userRepository.findByIdUsuario(id)
                .orElseThrow(() -> new UserNotFound("Usuario con id " + id + " no encontrado", true));
        
        // Borrado físico en MongoDB usando el ObjectId
        userRepository.deleteById(user.get_id());

        myWebSocketHandler.enviarMensajeATodos("Usuario eliminado con id:" +id);
    }
}
