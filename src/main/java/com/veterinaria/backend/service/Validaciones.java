package com.veterinaria.backend.service;

import com.veterinaria.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Reglas de validación que comparten los servicios de citas y fichas.
// Son las mismas que ya revisa el frontend: así los datos también se revisan cuando alguien
// usa el API directamente (Swagger, un script, etc.) y no solo cuando se usa la pantalla.
final class Validaciones {

    // Correo con "@" que termina en .cl o .com
    private static final Pattern CORREO = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(cl|com)$", Pattern.CASE_INSENSITIVE);

    // Celular chileno: +56 9 y 8 dígitos (con o sin espacios)
    private static final Pattern TELEFONO = Pattern.compile("^\\+56\\s?9\\s?\\d{4}\\s?\\d{4}$|^\\+56\\s?9\\s?\\d{8}$");

    // Hora de atención: "HH:mm" (solicitud del cliente) o "HH:mm - HH:mm" (bloque que asigna Recepción)
    private static final Pattern HORA = Pattern.compile("^(\\d{2}):(\\d{2})(?: - (\\d{2}):(\\d{2}))?$");

    // La clínica atiende de 09:00 a 19:00 (la última hora de inicio es 18:30)
    private static final int INICIO_ATENCION = 9 * 60;
    private static final int ULTIMO_INICIO = 18 * 60 + 30;
    private static final int FIN_ATENCION = 19 * 60;

    private Validaciones() {
    }

    // El correo es obligatorio y debe tener un formato válido
    static void correo(String valor) {
        if (valor == null || !CORREO.matcher(valor.trim()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Correo inválido (debe incluir \"@\" y terminar en \".cl\" o \".com\").");
        }
    }

    // El teléfono debe ser un celular chileno (ej: +56 9 1234 5678)
    static void telefono(String valor) {
        if (valor == null || !TELEFONO.matcher(valor.trim()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Teléfono chileno inválido (ej: +56 9 1234 5678).");
        }
    }

    // La hora debe tener formato válido y estar dentro del horario de atención (09:00 a 19:00)
    static void horaDeAtencion(String valor) {
        Matcher m = valor == null ? null : HORA.matcher(valor.trim());
        if (m == null || !m.matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La hora no tiene un formato válido (ej: 10:00 o 10:00 - 10:30).");
        }
        int inicio = Integer.parseInt(m.group(1)) * 60 + Integer.parseInt(m.group(2));
        boolean fueraDeHorario = inicio < INICIO_ATENCION || inicio > ULTIMO_INICIO;
        if (m.group(3) != null) {
            int fin = Integer.parseInt(m.group(3)) * 60 + Integer.parseInt(m.group(4));
            fueraDeHorario = fueraDeHorario || fin <= inicio || fin > FIN_ATENCION;
        }
        // Minutos inválidos (ej: 10:75) también quedan fuera
        if (Integer.parseInt(m.group(2)) > 59 || (m.group(4) != null && Integer.parseInt(m.group(4)) > 59)) fueraDeHorario = true;
        if (fueraDeHorario) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Horario fuera de atención (de 09:00 a 19:00 hrs, la última hora de inicio es 18:30).");
        }
    }

    // El peso viene como texto ("8.5", "12,5 kg"). Debe ser un número mayor a 0.
    static void peso(String valor) {
        String limpio = valor == null ? "" : valor.toLowerCase().replace("kg", "").replace(',', '.').trim();
        double numero;
        try {
            numero = Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El peso debe ser un número mayor a 0 (ej: 4.5 o 12.5 kg).");
        }
        if (Double.isNaN(numero) || numero <= 0 || numero >= 500) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El peso debe ser un número mayor a 0 (ej: 4.5 o 12.5 kg).");
        }
    }
}
