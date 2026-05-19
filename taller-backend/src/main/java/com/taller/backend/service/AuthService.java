package com.taller.backend.service;

import com.taller.backend.dto.LoginRequest;
import com.taller.backend.dto.LoginResponse;
import com.taller.backend.dto.RegisterRequest;
import com.taller.backend.exception.BadRequestException;
import com.taller.backend.model.Cliente;
import com.taller.backend.model.Mecanico;
import com.taller.backend.model.Usuario;
import com.taller.backend.repository.ClienteRepository;
import com.taller.backend.repository.MecanicoRepository;
import com.taller.backend.repository.UsuarioRepository;
import com.taller.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final MecanicoRepository mecanicoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(UsuarioRepository usuarioRepository,
                       ClienteRepository clienteRepository,
                       MecanicoRepository mecanicoRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.mecanicoRepository = mecanicoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Ya existe un usuario con el email: " + request.email());
        }

        Usuario.Rol rol;
        try {
            rol = Usuario.Rol.valueOf(request.rol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Rol no válido: " + request.rol());
        }

        if (rol == Usuario.Rol.ADMIN) {
            throw new BadRequestException("No se puede registrar un usuario con rol ADMIN");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setApellidos(request.apellidos());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setTelefono(request.telefono());
        usuario.setActivo(true);
        usuario.setRol(rol);

        usuarioRepository.save(usuario);

        if (rol == Usuario.Rol.CLIENTE) {
            Cliente cliente = new Cliente();
            cliente.setUsuario(usuario);
            clienteRepository.save(cliente);
        } else if (rol == Usuario.Rol.MECANICO) {
            Mecanico mecanico = new Mecanico();
            mecanico.setUsuario(usuario);
            mecanico.setNumEmpleado("EMP-" + System.currentTimeMillis());
            mecanicoRepository.save(mecanico);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.generateToken(userDetails, rol.name());

        return new LoginResponse(token, usuario.getEmail(), usuario.getNombre(), usuario.getApellidos(), rol.name(), usuario.getId(), null, null);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (!usuario.getActivo()) {
            throw new BadRequestException("Usuario desactivado");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.generateToken(userDetails, usuario.getRol().name());

        Integer mecanicoId = null;
        Integer clienteId = null;

        if (usuario.getRol() == Usuario.Rol.MECANICO) {
            mecanicoId = mecanicoRepository.findByUsuarioId(usuario.getId())
                    .map(m -> m.getId()).orElse(null);
        } else if (usuario.getRol() == Usuario.Rol.CLIENTE) {
            clienteId = clienteRepository.findByUsuarioId(usuario.getId())
                    .map(c -> c.getId()).orElse(null);
        }

        return new LoginResponse(token, usuario.getEmail(), usuario.getNombre(),
                usuario.getApellidos(), usuario.getRol().name(), usuario.getId(),
                mecanicoId, clienteId);
    }
}