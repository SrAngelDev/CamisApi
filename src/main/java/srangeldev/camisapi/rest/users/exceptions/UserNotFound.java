package srangeldev.camisapi.rest.users.exceptions;

public class UserNotFound extends srangeldev.camisapi.rest.users.exceptions.UserException {
    // Constructor para búsqueda por username
    public UserNotFound(String username) {
        super("Usuario con username " +  username + " no encontrado");
    }
    
    // Constructor para mensajes personalizados (como búsqueda por ID)
    public UserNotFound(String message, boolean customMessage) {
        super(message);
    }
}
