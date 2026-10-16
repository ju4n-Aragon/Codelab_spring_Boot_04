package com.denkitronik.clienteservice.domain.services;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.exception.ClienteNotFoundException;
import com.denkitronik.clienteservice.domain.repositories.IClienteDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private IClienteDao clienteDao;

    @InjectMocks
    private ClienteServiceImpl service;

    @Test
    void findById_inexistente_lanzaClienteNotFoundException() {
        when(clienteDao.findById(999L)).thenReturn(Optional.empty());

        ClienteNotFoundException ex =
                assertThrows(ClienteNotFoundException.class, () -> service.findById(999L));

        assertEquals("Cliente con id 999 no encontrado", ex.getMessage());
    }

    @Test
    void delete_inexistente_verificaExistsByIdYNoElimina() {
        when(clienteDao.existsById(5L)).thenReturn(false);

        assertThrows(ClienteNotFoundException.class, () -> service.delete(5L));

        verify(clienteDao, never()).deleteById(anyLong());
    }

    @Test
    void update_existente_copiaCamposYPreservaCreateAt() {
        LocalDate creado = LocalDate.of(2025, 4, 20);
        Cliente actual = new Cliente();
        actual.setId(1L);
        actual.setNombre("Viejo");
        actual.setCreateAt(creado);

        Cliente nuevo = new Cliente();
        nuevo.setNombre("Nuevo");
        nuevo.setApellido("Apellido");
        nuevo.setEmail("nuevo@mail.com");

        when(clienteDao.findById(1L)).thenReturn(Optional.of(actual));
        when(clienteDao.saveAndFlush(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente resultado = service.update(1L, nuevo);

        assertEquals(1L, resultado.getId());
        assertEquals("Nuevo", resultado.getNombre());
        assertEquals("nuevo@mail.com", resultado.getEmail());
        assertEquals(creado, resultado.getCreateAt());
    }

    @Test
    void update_inexistente_lanzaClienteNotFoundException() {
        when(clienteDao.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> service.update(42L, new Cliente()));
    }
}
