package com.taller.backend.service;

import com.taller.backend.model.Ausencia;
import com.taller.backend.model.Notificacion;
import com.taller.backend.model.Usuario;
import com.taller.backend.repository.AusenciaRepository;
import com.taller.backend.repository.NotificacionRepository;
import com.taller.backend.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AusenciaScheduler {

    private final AusenciaRepository ausenciaRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public AusenciaScheduler(AusenciaRepository ausenciaRepository,
                             NotificacionRepository notificacionRepository,
                             UsuarioRepository usuarioRepository) {
        this.ausenciaRepository = ausenciaRepository;
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Se ejecuta cada día a las 8:00
    @Scheduled(cron = "0 0 8 * * *")
    public void avisarAusenciasPróximas() {
        LocalDate manana = LocalDate.now().plusDays(1);
        LocalDate pasadoManana = LocalDate.now().plusDays(2);

        List<Ausencia> ausencias = ausenciaRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getAprobada()))
                .filter(a -> a.getFechaInicio().equals(manana) || a.getFechaInicio().equals(pasadoManana))
                .toList();

        List<Usuario> admins = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Usuario.Rol.ADMIN)
                .toList();

        for (Ausencia ausencia : ausencias) {
            String mecanico = ausencia.getMecanico().getUsuario().getNombre()
                    + " " + ausencia.getMecanico().getUsuario().getApellidos();
            String msg = "⚠️ " + mecanico + " tiene una ausencia aprobada que comienza el "
                    + ausencia.getFechaInicio() + " y termina el " + ausencia.getFechaFin()
                    + " (" + ausencia.getTipo() + ")";

            for (Usuario admin : admins) {
                Notificacion n = new Notificacion();
                n.setUsuario(admin);
                n.setTitulo("Ausencia próxima — " + mecanico);
                n.setMensaje(msg);
                n.setTipo(Notificacion.Tipo.AUSENCIA);
                n.setLeida(false);
                notificacionRepository.save(n);
            }
        }
    }
}