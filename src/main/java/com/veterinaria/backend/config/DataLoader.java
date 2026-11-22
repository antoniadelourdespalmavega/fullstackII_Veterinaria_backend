package com.veterinaria.backend.config;

import com.veterinaria.backend.model.Cita;
import com.veterinaria.backend.model.Ficha;
import com.veterinaria.backend.model.Usuario;
import com.veterinaria.backend.repository.CitaRepository;
import com.veterinaria.backend.repository.FichaRepository;
import com.veterinaria.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

// Al arrancar, si las tablas están vacías, carga los datos con los que parte el sistema
// (los mismos que antes estaban en datosBase.js del frontend). Si ya hay datos, no toca nada.
@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final CitaRepository citas;
    private final FichaRepository fichas;
    private final PasswordEncoder encoder;
    // Para que los datos iniciales se muestren en el mismo orden en que estaban antes
    private int contadorCitas = 0;
    private int contadorFichas = 0;

    public DataLoader(UsuarioRepository usuarios, CitaRepository citas, FichaRepository fichas, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.citas = citas;
        this.fichas = fichas;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() == 0) cargarUsuarios();
        if (citas.count() == 0) cargarCitas();
        if (fichas.count() == 0) cargarFichas();
    }

    private void usuario(String nombre, String correo, String clave, String rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setClave(encoder.encode(clave)); // se guarda cifrada
        u.setRol(rol);
        u.setTipo("Base");
        u.setActivo(true);
        usuarios.save(u);
    }

    private void cargarUsuarios() {
        usuario("Dr. Daniel Morales", "daniel.morales@sanmarcos.cl", "Vet2026*", Usuario.VETERINARIO);
        usuario("Dra. Karla Fuentes", "karla.fuentes@sanmarcos.cl", "Vet2026*", Usuario.VETERINARIO);
        usuario("Dr. Kevin Silva", "kevin.silva@sanmarcos.cl", "Vet2026*", Usuario.VETERINARIO);
        usuario("Dr. Kevinne Navarro", "kevinne.navarro@sanmarcos.cl", "Vet2026*", Usuario.VETERINARIO);
        usuario("Camila Morales", "camila.recepcion@sanmarcos.cl", "Recep2026*", Usuario.SECRETARIA);
        usuario("Vicente Julio Zapata", "admin@sanmarcos.cl", "Admin2026*", Usuario.ADMINISTRADOR);
        usuario("Pedro Pascal", "pedro.pascal@gmail.com", "Cliente123*", Usuario.CLIENTE);
        usuario("Camila Vallejo", "camila.vallejo@veterinaria.cl", "Cliente123*", Usuario.CLIENTE);
    }

    private void cita(String id, String fecha, String hora, String dueno, String telefono, String correo,
                      String mascota, String especie, String raza, String doctor, String motivo) {
        Cita c = new Cita();
        c.setIdCita(id);
        c.setFecha(fecha);
        c.setHora(hora);
        c.setDueno(dueno);
        c.setTelefono(telefono);
        c.setCorreo(correo);
        c.setMascota(mascota);
        c.setEspecie(especie);
        c.setRaza(raza);
        c.setDoctor(doctor);
        c.setMotivo(motivo);
        c.setEstado(Cita.CONFIRMADA);
        c.setCreadoEn(Instant.now().minusSeconds(contadorCitas++)); // la primera de la lista queda como la más reciente
        citas.save(c);
    }

    private void cargarCitas() {
        cita("CIT-101", "2026-09-07", "10:00 - 10:30", "Pedro Pascal", "+56 9 9123 4567", "pedro.pascal@gmail.com",
                "Rocky", "Canino", "Bulldog Francés", "Dra. Karla Fuentes", "Control dermatológico de alergia cutánea.");
        cita("CIT-102", "2026-09-07", "11:30 - 12:00", "Camila Vallejo", "+56 9 8765 4321", "camila.vallejo@veterinaria.cl",
                "Michi", "Felino", "Siamés", "Dr. Kevinne Navarro", "Revisión anual y refuerzo de antiparasitario.");
        cita("CIT-103", "2026-09-08", "16:00 - 16:30", "Jorge Carmona", "+56 9 5544 3322", "jorge@gm.cl",
                "Coco", "Canino", "Poodle Toy", "Dr. Daniel Morales", "Limpieza y revisión de oídos por otitis.");
    }

    private void ficha(String id, String mascota, String especie, String raza, String edad, String peso,
                       String dueno, String telefono, String correo, String condiciones, String problema,
                       String tratamiento, String estado, boolean pagado) {
        Ficha f = new Ficha();
        f.setId(id);
        f.setMascota(mascota);
        f.setEspecie(especie);
        f.setRaza(raza);
        f.setEdad(edad);
        f.setPeso(peso);
        f.setDueno(dueno);
        f.setTelefono(telefono);
        f.setCorreo(correo);
        f.setCondiciones(condiciones);
        f.setProblema(problema);
        f.setTratamiento(tratamiento);
        f.setEstado(estado);
        f.setPagado(pagado);
        f.setCreadoEn(Instant.now().minusSeconds(contadorFichas++));
        fichas.save(f);
    }

    private void cargarFichas() {
        ficha("PAC-001", "Rocky", "Canino", "Bulldog Francés", "3 años", "12.5 kg",
                "Pedro Pascal", "+56 9 9123 4567", "pedro.pascal@gmail.com",
                "Sensibilidad estomacal, intolerancia a granos.",
                "Dermatitis atópica con prurito en patas y zona abdominal.",
                "Apoquel 5.4mg cada 12 hrs por 7 días. Baño con shampoo de clorhexidina 2 veces por semana.",
                Ficha.EN_TRATAMIENTO, true);
        ficha("PAC-002", "Max", "Canino", "Golden Retriever", "5 años", "31.2 kg",
                "Pedro Pascal", "+56 9 9123 4567", "pedro.pascal@gmail.com",
                "Displasia de cadera leve en articulación izquierda.",
                "Dolor articular posterior tras ejercicio intenso.",
                "Condroprotectores Artroflex 1 comprimido al día por 30 días. Reposo moderado y paseos suaves.",
                Ficha.EN_TRATAMIENTO, false);
        ficha("PAC-003", "Mia", "Felino", "Angora Turco", "2 años", "3.9 kg",
                "Pedro Pascal", "+56 9 9123 4567", "pedro.pascal@gmail.com",
                "Esterilizada, vacunación séxtuple al día.",
                "Control de rutina y prevención de bolas de pelo.",
                "Pasta de Malta felina 2 cm vía oral 3 veces por semana. Cepillado diario de pelaje.",
                Ficha.FINALIZADO, true);
        ficha("PAC-004", "Michi", "Felino", "Siamés", "5 años", "4.2 kg",
                "Camila Vallejo", "+56 9 8765 4321", "camila.vallejo@veterinaria.cl",
                "Sin antecedentes crónicos registrados.",
                "Control de rutina y vacunación anual antirrábica.",
                "Se administra vacuna antirrábica y desparasitación interna con Pipeta Broadline.",
                Ficha.FINALIZADO, true);
        ficha("PAC-005", "Coco", "Canino", "Poodle Toy", "7 años", "5.1 kg",
                "Jorge Carmona", "+56 9 5544 3322", "jorge@gm.cl",
                "Soplo cardiaco grado I.",
                "Otitis externa bilateral con secreción ceruminosa.",
                "Limpieza ótica con Otoclean + gotas Otovet 4 gotas cada 12 hrs por 10 días.",
                Ficha.EN_TRATAMIENTO, false);
        ficha("PAC-006", "Luna", "Felino", "Común Europeo", "2 años", "3.8 kg",
                "Alex Cabeño", "+56 9 4433 2211", "poto@duocuc.cl",
                "Esterilizada, vacunas al día.",
                "Cojera leve en pata trasera derecha por salto accidental.",
                "Meloxicam gotas (0.1mg/kg) por 3 días y reposo preventivo.",
                Ficha.EN_TRATAMIENTO, false);
    }
}
