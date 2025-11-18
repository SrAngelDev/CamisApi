package srangeldev.camisapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de recursos estáticos para servir reportes de test, documentación y cobertura
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Reportes de test
        registry.addResourceHandler("/test/**")
                .addResourceLocations("file:./reports/test/", "classpath:/static/reports/test/");

        // Documentación Dokka
        registry.addResourceHandler("/doc/**")
                .addResourceLocations("file:./reports/doc/", "classpath:/static/reports/doc/");

        // Cobertura de código
        registry.addResourceHandler("/coverage/**")
                .addResourceLocations("file:./reports/coverage/", "classpath:/static/reports/coverage/");
    }
}
