package com.denkitronik.clienteservice.delivery.rest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteRestControllerTest {

    private static final String BASE = "/api/v1/cliente-service/clientes";
    private static final String CLIENTE_VALIDO =
            "{\"nombre\":\"Ana\",\"apellido\":\"Perez\",\"email\":\"ana@mail.com\"}";

    @Autowired
    private MockMvc mockMvc;

    private long crearCliente() throws Exception {
        String body = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CLIENTE_VALIDO))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CLIENTE_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createAt").exists())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void crear_conDatosInvalidos_devuelve400ConMapaDeCampos() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"apellido\":\"Perez\",\"email\":\"no-es-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").exists())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void buscarPorId_existente_devuelve200() throws Exception {
        long id = crearCliente();
        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("ana@mail.com"));
    }

    @Test
    void buscarPorId_inexistente_devuelve404ConApiErrorResponse() throws Exception {
        mockMvc.perform(get(BASE + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("Cliente con id 999999 no encontrado"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void actualizar_existente_devuelve201ConDatosActualizados() throws Exception {
        long id = crearCliente();
        mockMvc.perform(put(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Maria\",\"apellido\":\"Lopez\",\"email\":\"maria@mail.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Maria"))
                .andExpect(jsonPath("$.createAt").exists());
    }

    @Test
    void actualizar_inexistente_devuelve404() throws Exception {
        mockMvc.perform(put(BASE + "/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CLIENTE_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Cliente con id 999999 no encontrado"));
    }

    @Test
    void eliminar_existente_devuelve204YLuegoEs404() throws Exception {
        long id = crearCliente();
        mockMvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_inexistente_devuelve404() throws Exception {
        mockMvc.perform(delete(BASE + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
