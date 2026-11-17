package com.eduneko.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ValidadorContenidoIAService {

    private final GeneradorContenidoIAService generadorIA;
    private final ObjectMapper objectMapper;

    public ValidadorContenidoIAService(
            GeneradorContenidoIAService generadorIA,
            ObjectMapper objectMapper) {

        this.generadorIA = generadorIA;
        this.objectMapper = objectMapper;
    }

    public ResultadoValidacion validar(
            Map<String, Object> contexto,
            JsonNode leccion) {

        String instrucciones = """
            Eres un revisor especializado en Matemáticas
            de secundaria y didáctica educativa en México.

            Evalúa críticamente la lección proporcionada.

            CRITERIOS:
            1. Verifica cálculos, fórmulas y resultados.
            2. Detecta errores conceptuales matemáticos.
            3. Comprueba que los procedimientos sean completos.
            4. Revisa que no se confundan conjuntos numéricos.
            5. Verifica correspondencia con grado, objetivo y PDA.
            6. Evalúa si la explicación es apropiada para
               la prioridad y preferencias del estudiante.
            7. Detecta contradicciones entre explicación y ejemplo.
            8. No apruebes contenido con errores matemáticos.
            9. No rechaces contenido correcto por diferencias
               de redacción o estilo.
            10. Si existe incertidumbre sobre la corrección
                matemática, indica que requiere revisión.
                
            VALIDACIÓN DE RECURSOS VISUALES:

            11. La lección puede incluir el campo recursoVisual.
                Si es null o no existe, no rechaces la lección
                únicamente por la ausencia de un diagrama.

            12. En la generación inicial se admiten:
                - "barra_fraccionaria"
                - "recta_numerica"
                - "construccion_geometrica" (resultado de mediatriz
                  o bisectriz, no solo el paso inicial)

                Si existe un recurso visual, verifica que
                corresponda al concepto matemático explicado.

            13. Para "barra_fraccionaria":
                - Se permiten fracciones propias, impropias
                  y equivalentes a números enteros.
                - Los números mixtos positivos pueden
                  representarse mediante su fracción impropia.
                - numerador y denominador deben ser enteros.
                - denominador debe estar entre 1 y 12.
                - numerador debe estar entre 0 y
                  denominador multiplicado por 4.
                - Se permiten hasta cuatro barras.
                - No rechaces una fracción impropia solamente
                  porque su numerador supere al denominador.

            14. Para "recta_numerica":
                - minimo, maximo, intervalo y valorDestacado
                  deben ser números finitos.
                - minimo debe ser menor que maximo.
                - intervalo debe ser mayor o igual
                  a 0.00000001.
                - valorDestacado debe encontrarse entre
                  minimo y maximo, inclusive.
                - (maximo - minimo) / intervalo
                  no debe superar 20.
                - El valor destacado debe corresponder
                  al concepto explicado o al ejemplo resuelto.
                - Se permiten números enteros y decimales,
                  tanto positivos como negativos.
                - El valor destacado puede encontrarse
                  entre dos marcas de la recta.

                No apruebes recursos que intenten representar
                desigualdades, intervalos abiertos o cerrados,
                ni múltiples puntos simultáneos.

            14a. Para "construccion_geometrica":
                 - "mediatriz" debe mostrar el punto medio y la recta
                   perpendicular construida con arcos de compás.
                 - "bisectriz" debe mostrar el ángulo dividido en
                   dos ángulos iguales por la semirrecta final.
                 - Rechaza usar solo un segmento o ángulo inicial
                   cuando el ejemplo exige el trazo terminado.

            15. Comprueba la coherencia matemática entre
                el recurso visual, la explicación y
                el ejemplo resuelto.

                Ejemplos:
                - Una lección sobre 7/4 puede representar
                  un entero completo y otros 3/4.
                - Una lección sobre números negativos puede
                  destacar -2 en una recta de -5 a 5.
                - Una lección sobre decimales puede destacar
                  1.5 en una recta de 0 a 3.

                No apruebes recursos con valores inventados
                o sin relación pedagógica con la lección.

            16. Comprueba que el recurso visual sea apropiado
                para el grado escolar, objetivo y PDA.

                Si contiene errores matemáticos o contradice
                la explicación, devuelve aprobada=false
                e identifica concretamente el problema.

            17. No rechaces una lección matemáticamente
                correcta únicamente porque no incluya
                un recurso visual.


            Devuelve exclusivamente un JSON válido:
            {
              "aprobada": true,
              "observaciones": "Sin errores detectados"
            }

            Si detectas errores, devuelve aprobada=false
            y describe concretamente los problemas encontrados.

            No incluyas Markdown ni texto fuera del JSON.
            """;

        try {
            String contenido = objectMapper.writeValueAsString(
                    Map.of(
                            "contexto", contexto,
                            "leccion", leccion
                    )
            );

            String respuesta = generadorIA.generarTexto(
                    instrucciones,
                    contenido
            );

            JsonNode resultado = objectMapper.readTree(respuesta);

            JsonNode aprobada = resultado.path("aprobada");
            JsonNode observaciones = resultado.path("observaciones");

            if (!aprobada.isBoolean()
                    || !observaciones.isTextual()
                    || observaciones.asText().isBlank()) {

                throw new IllegalStateException(
                        "La validacion de IA devolvio un formato invalido."
                );
            }

            return new ResultadoValidacion(
                    aprobada.asBoolean(),
                    observaciones.asText().trim()
            );

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(
                    "No fue posible interpretar la validacion de IA.",
                    e
            );
        }
    }

        /**
         * Valida la estructura de los recursos visuales admitidos.
         *
         * Tipos soportados:
         * - barra_fraccionaria
         * - recta_numerica
         *
         * No realiza llamadas a OpenAI ni modifica PostgreSQL.
         */


/**
 * Evalúa expresiones algebraicas sencillas:
 * x, x + n, x - n.
 *
 * Devuelve null cuando la expresión no está soportada.
 */
private Double evaluarExpresionRectangulo(
        String expresion,
        double valorX) {

    if (expresion == null || !Double.isFinite(valorX)) {
        return null;
    }

    // Eliminar espacios para normalizar la expresión.
    String normalizada = expresion.replaceAll("\\s+", "");

    // Caso: x
    if (normalizada.equals("x")) {
        return valorX;
    }

    // Admitir únicamente x+n o x-n.
    if (!normalizada.matches("x[+-]\\d+(\\.\\d+)?")) {
        return null;
    }

    try {
        // Extraer el desplazamiento con su signo.
        double desplazamiento =
                Double.parseDouble(normalizada.substring(1));

        double resultado = valorX + desplazamiento;

        return Double.isFinite(resultado) ? resultado : null;

    } catch (NumberFormatException ex) {
        return null;
    }
}

/**
 * Comprueba que las dimensiones numéricas correspondan
 * a las expresiones algebraicas con el mismo valor de x.
 *
 * Para V1 se requiere al menos una expresión igual a x.
 */
private boolean validarDimensionesAlgebraicasRectangulo(
        String expresionBase,
        String expresionAltura,
        double base,
        double altura) {

    String baseNormalizada =
            expresionBase.replaceAll("\\s+", "");

    String alturaNormalizada =
            expresionAltura.replaceAll("\\s+", "");

    // Obtener x desde la dimensión que lo representa directamente.
    double valorX;

    if (baseNormalizada.equals("x")) {
        valorX = base;
    } else if (alturaNormalizada.equals("x")) {
        valorX = altura;
    } else {
        return false;
    }

    Double baseEvaluada =
            evaluarExpresionRectangulo(expresionBase, valorX);

    Double alturaEvaluada =
            evaluarExpresionRectangulo(expresionAltura, valorX);

    if (baseEvaluada == null || alturaEvaluada == null) {
        return false;
    }

    // Comparar resultados con tolerancia numérica.
    double toleranciaBase =
            Math.max(1.0, Math.abs(base)) * 1e-8;

    double toleranciaAltura =
            Math.max(1.0, Math.abs(altura)) * 1e-8;

    return Math.abs(baseEvaluada - base) <= toleranciaBase
            && Math.abs(alturaEvaluada - altura) <= toleranciaAltura;
}

        public boolean validarEstructuraRecursoVisual(
                JsonNode recursoVisual) {

        // Verifica que el recurso sea un objeto JSON.
        if (recursoVisual == null || !recursoVisual.isObject()) {
                return false;
        }

        // Recupera los campos comunes a todos los recursos.
        JsonNode tipo = recursoVisual.path("tipo");
        JsonNode titulo = recursoVisual.path("titulo");
        JsonNode datos = recursoVisual.path("datos");

        if (!tipo.isTextual()
                || !titulo.isTextual()
                || titulo.asText().isBlank()
                || !datos.isObject()) {
                return false;
        }

        // Cada componente tiene sus propias reglas.
        switch (tipo.asText()) {

                case "barra_fraccionaria" -> {

                JsonNode numerador = datos.path("numerador");
                JsonNode denominador = datos.path("denominador");

                // Solo se permiten enteros compatibles con int.
                if (!numerador.isIntegralNumber()
                        || !denominador.isIntegralNumber()
                        || !numerador.canConvertToInt()
                        || !denominador.canConvertToInt()) {
                        return false;
                }

                int n = numerador.intValue();
                int d = denominador.intValue();

                // Denominador de 1 a 12 y máximo cuatro barras.
                return d >= 1
                        && d <= 12
                        && n >= 0
                        && n <= d * 4;
                }

                case "recta_numerica" -> {

                JsonNode minimo = datos.path("minimo");
                JsonNode maximo = datos.path("maximo");
                JsonNode intervalo = datos.path("intervalo");
                JsonNode valorDestacado =
                        datos.path("valorDestacado");

                // Los cuatro campos deben contener números.
                if (!minimo.isNumber()
                        || !maximo.isNumber()
                        || !intervalo.isNumber()
                        || !valorDestacado.isNumber()) {
                        return false;
                }

                double min = minimo.doubleValue();
                double max = maximo.doubleValue();
                double paso = intervalo.doubleValue();
                double punto = valorDestacado.doubleValue();

                // Evita números no finitos y rangos inválidos.
                if (!Double.isFinite(min)
                        || !Double.isFinite(max)
                        || !Double.isFinite(paso)
                        || !Double.isFinite(punto)
                        || min >= max
                        || paso < 0.00000001
                        || punto < min
                        || punto > max) {
                        return false;
                }

                // La diferencia debe poder representarse.
                double rango = max - min;

                if (!Double.isFinite(rango)) {
                        return false;
                }

                // Angular admite como máximo 20 intervalos,
                // equivalentes a un máximo de 21 marcas.
                double cantidadIntervalos = rango / paso;

                return Double.isFinite(cantidadIntervalos)
                        && cantidadIntervalos <= 20;
                }

                case "figura_geometrica_2d" -> {

                // Recupera el tipo de figura y su unidad de medida.
                JsonNode figura = datos.path("figura");
                JsonNode unidad = datos.path("unidad");

                // Ambos campos son obligatorios.
                if (!figura.isTextual()
                        || !unidad.isTextual()) {
                        return false;
                }

                // Solo se permiten unidades conocidas.
                String unidadMedida = unidad.asText();

                if (!unidadMedida.equals("mm")
                        && !unidadMedida.equals("cm")
                        && !unidadMedida.equals("m")
                        && !unidadMedida.equals("km")) {
                        return false;
                }

                // Validación específica según la figura.
                switch (figura.asText()) {

                        case "cuadrado" -> {

                        // Un cuadrado requiere un lado positivo.
                        return dimensionValida(datos.path("lado"));
                        }
                        
                        case "rectangulo" -> {

                        // ============================================
                        // VALIDACIÓN DE RECTÁNGULOS
                        // ============================================

                        // Las dimensiones deben ser positivas y válidas.
                        if (!dimensionValida(datos.path("base"))
                                || !dimensionValida(datos.path("altura"))) {
                                return false;
                        }

                        double base = datos.path("base").doubleValue();
                        double altura = datos.path("altura").doubleValue();

                        // Evitar figuras excesivamente alargadas
                        // que puedan confundirse con segmentos.
                        if (Math.max(base, altura) /
                                Math.min(base, altura) > 20) {
                                return false;
                        }

                        // ============================================
                        // ÁLGEBRA OPCIONAL
                        // ============================================

                        // Los rectángulos normales siguen siendo válidos.
                        if (!datos.has("algebra")) {
                                return true;
                        }

                        JsonNode algebra = datos.path("algebra");

                        // Cuando existe álgebra, debe ser un objeto.
                        if (!algebra.isObject()) {
                                return false;
                        }

                        JsonNode incognita = algebra.path("incognita");
                        JsonNode expresionBase = algebra.path("expresionBase");
                        JsonNode expresionAltura = algebra.path("expresionAltura");
                        JsonNode areaConocida = algebra.path("areaConocida");

                        // ============================================
                        // VALIDAR EL NUEVO CONTRATO
                        // ============================================

                        // La variable algebraica admitida inicialmente es x.
                        if (!incognita.isTextual()
                                || !incognita.asText().equals("x")) {
                                return false;
                        }

                        // Ambas expresiones deben existir y contener texto.
                        if (!expresionBase.isTextual()
                                || expresionBase.asText().isBlank()
                                || !expresionAltura.isTextual()
                                || expresionAltura.asText().isBlank()) {
                                return false;
                        }

                        // Limitar la longitud de las expresiones.
                        // Evita recibir cadenas excesivamente grandes.
                        if (expresionBase.asText().length() > 100
                                || expresionAltura.asText().length() > 100) {
                                return false;
                        }

                        // El área conocida debe ser numérica.
                        if (!areaConocida.isNumber()) {
                                return false;
                        }

                        double area = areaConocida.doubleValue();
                        double areaCalculada = base * altura;

                        // Verificar valores finitos y positivos.
                        if (!Double.isFinite(area)
                                || !Double.isFinite(areaCalculada)
                                || area <= 0) {
                                return false;
                        }

                        // ============================================
                        // VERIFICACIÓN MATEMÁTICA DEL ÁREA
                        // ============================================

                        // Comprobar que las dimensiones numéricas
                        // correspondan al área conocida.
                        double tolerancia =
        Math.max(1.0, Math.abs(areaCalculada)) * 1e-8;

if (Math.abs(area - areaCalculada) > tolerancia) {
    return false;
}

// ============================================
// VERIFICACIÓN DE EXPRESIONES ALGEBRAICAS
// ============================================

// Comprobar que ambas expresiones produzcan
// las dimensiones numéricas usando el mismo x.
return validarDimensionesAlgebraicasRectangulo(
        expresionBase.asText(),
        expresionAltura.asText(),
        base,
        altura
);
                        }

                        case "triangulo" -> {
                        return dimensionValida(datos.path("base"))
                                && dimensionValida(datos.path("altura"));
                        }

                        case "circulo" -> {

                        // Un círculo requiere un radio positivo.
                        return dimensionValida(datos.path("radio"));
                        }

                        default -> {

                        // Rechaza figuras todavía no implementadas.
                        return false;
                        }
                    }
                }

                case "patron_figuras_2d" -> {

                // Recuperar los campos principales del patrón.
                JsonNode modalidad = datos.path("modalidad");
                JsonNode unidad = datos.path("unidad");
                JsonNode regla = datos.path("regla");
                JsonNode elementos = datos.path("elementos");

                // Validar los campos obligatorios.
                if (!modalidad.isTextual()
                        || !unidad.isTextual()
                        || !regla.isTextual()
                        || regla.asText().isBlank()
                        || !elementos.isArray()) {
                        return false;
                }

                // Por ahora, Angular solo representa patrones de crecimiento.
                if (!modalidad.asText().equals("crecimiento")) {
                        return false;
                }

                // Validar unidades de medida compatibles.
                String unidadMedida = unidad.asText();

                if (!unidadMedida.equals("mm")
                        && !unidadMedida.equals("cm")
                        && !unidadMedida.equals("m")
                        && !unidadMedida.equals("km")) {
                        return false;
                }

                // Entre dos y seis figuras por sucesión.
                if (elementos.size() < 2 || elementos.size() > 6) {
                        return false;
                }

                String tipoFiguraAnterior = null;
                double dimensionAnterior = 0;

                // Validar cada figura de la sucesión.
                for (int i = 0; i < elementos.size(); i++) {

                        JsonNode elemento = elementos.get(i);

                        if (!elemento.isObject()) {
                        return false;
                        }

                        JsonNode posicion = elemento.path("posicion");
                        JsonNode figura = elemento.path("figura");
                        JsonNode perimetro = elemento.path("perimetro");

                        // Las posiciones deben comenzar en 1 y ser consecutivas.
                        if (!posicion.isIntegralNumber()
                                || !posicion.canConvertToInt()
                                || posicion.intValue() != i + 1
                                || !figura.isTextual()) {
                        return false;
                        }

                        String tipoFigura = figura.asText();

                        // La primera versión solo admite cuadrados y rectángulos.
                        if (!tipoFigura.equals("cuadrado")
                                && !tipoFigura.equals("rectangulo")) {
                        return false;
                        }

                        // En un patrón de crecimiento mantenemos
                        // el mismo tipo de figura en toda la sucesión.
                        if (tipoFiguraAnterior != null
                                && !tipoFiguraAnterior.equals(tipoFigura)) {
                        return false;
                        }

                        tipoFiguraAnterior = tipoFigura;

                        double perimetroEsperado;
                        double dimensionActual;

                        if (tipoFigura.equals("cuadrado")) {

                        JsonNode lado = elemento.path("lado");

                        if (!dimensionValida(lado)) {
                                return false;
                        }

                        double longitud = lado.doubleValue();

                        // Perímetro matemático del cuadrado.
                        perimetroEsperado = 4 * longitud;
                        dimensionActual = longitud;

                        } else {

                        JsonNode base = elemento.path("base");
                        JsonNode altura = elemento.path("altura");

                        if (!dimensionValida(base)
                                || !dimensionValida(altura)) {
                                return false;
                        }

                        double b = base.doubleValue();
                        double h = altura.doubleValue();

                        // Perímetro matemático del rectángulo.
                        perimetroEsperado = 2 * (b + h);

                        // Para rectángulos comprobamos que
                        // la dimensión mayor vaya aumentando.
                        dimensionActual = Math.max(b, h);
                        }

                        // La sucesión debe mostrar crecimiento estricto.
                        if (i > 0 && dimensionActual <= dimensionAnterior) {
                        return false;
                        }

                        dimensionAnterior = dimensionActual;

                        // Si la IA proporciona un perímetro,
                        // debe coincidir con el cálculo matemático.
                        if (!perimetro.isMissingNode()
                                && !perimetro.isNull()) {

                        if (!perimetro.isNumber()) {
                                return false;
                        }

                        double valorPerimetro = perimetro.doubleValue();

                        if (!Double.isFinite(valorPerimetro)
                                || Math.abs(valorPerimetro - perimetroEsperado)
                                        > 0.000001 * Math.max(1, perimetroEsperado)) {
                                return false;
                        }
                        }

                        // Validar la etiqueta opcional.
                        JsonNode etiqueta = elemento.path("etiqueta");

                        if (!etiqueta.isMissingNode()
                                && !etiqueta.isNull()
                                && !etiqueta.isTextual()) {
                        return false;
                        }
                }

                // Todos los elementos cumplen las reglas estructurales
                // y los perímetros proporcionados son correctos.
                return true;
                }

                case "plano_cartesiano" -> {

                        // Recuperar los límites y el intervalo del plano.
                        JsonNode minimoX = datos.path("minimoX");
                        JsonNode maximoX = datos.path("maximoX");
                        JsonNode minimoY = datos.path("minimoY");
                        JsonNode maximoY = datos.path("maximoY");
                        JsonNode intervalo = datos.path("intervalo");
                        JsonNode puntos = datos.path("puntos");

                        // Comprobar que todos los campos obligatorios existan.
                        if (!minimoX.isNumber()
                                || !maximoX.isNumber()
                                || !minimoY.isNumber()
                                || !maximoY.isNumber()
                                || !intervalo.isNumber()
                                || !puntos.isArray()) {
                                return false;
                        }

                        double minX = minimoX.doubleValue();
                        double maxX = maximoX.doubleValue();
                        double minY = minimoY.doubleValue();
                        double maxY = maximoY.doubleValue();
                        double paso = intervalo.doubleValue();

                        // Rechazar números no finitos y rangos inválidos.
                        if (!Double.isFinite(minX)
                                || !Double.isFinite(maxX)
                                || !Double.isFinite(minY)
                                || !Double.isFinite(maxY)
                                || !Double.isFinite(paso)
                                || minX >= maxX
                                || minY >= maxY
                                || paso < 0.00000001) {
                                return false;
                        }

                        // Calcular la cantidad de intervalos por eje.
                        double intervalosX = (maxX - minX) / paso;
                        double intervalosY = (maxY - minY) / paso;

                        // Angular admite como máximo 20 intervalos por eje.
                        if (!Double.isFinite(intervalosX)
                                || !Double.isFinite(intervalosY)
                                || intervalosX > 20
                                || intervalosY > 20) {
                                return false;
                        }

                        // Permitir entre 1 y 10 puntos por plano.
                        if (puntos.size() < 1 || puntos.size() > 10) {
                                return false;
                        }

                        // Validar individualmente las coordenadas y etiquetas.
                        for (JsonNode punto : puntos) {

                                if (!punto.isObject()) {
                                return false;
                                }

                                JsonNode x = punto.path("x");
                                JsonNode y = punto.path("y");
                                JsonNode etiqueta = punto.path("etiqueta");

                                if (!x.isNumber()
                                        || !y.isNumber()
                                        || !etiqueta.isTextual()
                                        || etiqueta.asText().isBlank()) {
                                return false;
                                }

                                double coordenadaX = x.doubleValue();
                                double coordenadaY = y.doubleValue();

                                // Verificar que el punto pertenezca al plano.
                                if (!Double.isFinite(coordenadaX)
                                        || !Double.isFinite(coordenadaY)
                                        || coordenadaX < minX
                                        || coordenadaX > maxX
                                        || coordenadaY < minY
                                        || coordenadaY > maxY) {
                                return false;
                                }

                                // Evitar etiquetas excesivamente largas.
                                if (etiqueta.asText().length() > 12) {
                                return false;
                                }
                        }

                        // El recurso cumple las reglas estructurales.
                        return true;
                        }

                        case "grafica_estadistica" -> {

                                // Recuperar los campos principales de la gráfica.
                                JsonNode modalidad = datos.path("modalidad");
                                JsonNode unidad = datos.path("unidad");
                                JsonNode elementos = datos.path("elementos");

                                // Comprobar que los campos obligatorios tengan
                                // los tipos de datos esperados.
                                if (!modalidad.isTextual()
                                        || !unidad.isTextual()
                                        || !elementos.isArray()) {
                                        return false;
                                }

                                // Solo se admiten las modalidades que Angular
                                // ya tiene implementadas.
                                String tipoGrafica = modalidad.asText();

                                if (!tipoGrafica.equals("barras")
                                        && !tipoGrafica.equals("lineas")) {
                                        return false;
                                }

                                // La unidad puede ser una palabra o una descripción
                                // breve, por ejemplo: alumnos, libros o °C.
                                String unidadMedida = unidad.asText();

                                if (unidadMedida.isBlank()
                                        || unidadMedida.length() > 20) {
                                        return false;
                                }

                                // Evitar gráficas demasiado grandes para el SVG.
                                if (elementos.size() < 1 || elementos.size() > 8) {
                                        return false;
                                }

                                // Una gráfica de líneas necesita al menos dos puntos
                                // para representar un cambio entre valores.
                                if (tipoGrafica.equals("lineas")
                                        && elementos.size() < 2) {
                                        return false;
                                }

                                // Revisar cada categoría y su valor numérico.
                                for (JsonNode elemento : elementos) {

                                        if (!elemento.isObject()) {
                                        return false;
                                        }

                                        JsonNode etiqueta = elemento.path("etiqueta");
                                        JsonNode valor = elemento.path("valor");

                                        // Cada elemento debe contener una etiqueta
                                        // y un valor numérico.
                                        if (!etiqueta.isTextual()
                                                || !valor.isNumber()) {
                                        return false;
                                        }

                                        String nombreCategoria = etiqueta.asText();

                                        // Limitar las etiquetas para reducir el riesgo
                                        // de superposición en el SVG.
                                        if (nombreCategoria.isBlank()
                                                || nombreCategoria.length() > 8) {
                                        return false;
                                        }

                                        double cantidad = valor.doubleValue();

                                        // Evitar valores no finitos o negativos.
                                        if (!Double.isFinite(cantidad)
                                                || cantidad < 0) {
                                        return false;
                                        }

                                        // Limitar la magnitud para que las etiquetas
                                        // numéricas sigan siendo legibles.
                                        if (cantidad > 9999) {
                                        return false;
                                        }
                                }

                                // Todos los datos cumplen las reglas estructurales.
                                return true;
                                }

                        // Gráfica de y = ax² + bx + c, con dominio y valores acotados.
                        case "grafica_funcion" -> {
                                JsonNode a = datos.path("a");
                                JsonNode b = datos.path("b");
                                JsonNode c = datos.path("c");
                                JsonNode minX = datos.path("minimoX");
                                JsonNode maxX = datos.path("maximoX");
                                JsonNode valorX = datos.path("valorX");
                                if (!a.isNumber() || !b.isNumber() || !c.isNumber() ||
                                        !minX.isNumber() || !maxX.isNumber() || !valorX.isNumber()) {
                                        return false;
                                }
                                double va = a.doubleValue();
                                double vb = b.doubleValue();
                                double vc = c.doubleValue();
                                double min = minX.doubleValue();
                                double max = maxX.doubleValue();
                                double seleccionado = valorX.doubleValue();
                                if (!Double.isFinite(va) || !Double.isFinite(vb) || !Double.isFinite(vc) ||
                                        !Double.isFinite(min) || !Double.isFinite(max) ||
                                        !Double.isFinite(seleccionado) ||
                                        Math.abs(va) > 10 || Math.abs(vb) > 10 || Math.abs(vc) > 20 ||
                                        min < -10 || max > 10 || min >= max || max - min < 1 ||
                                        seleccionado < min || seleccionado > max) {
                                        return false;
                                }
                                // El SVG admite valores y entre -1000 y 1000.
                                for (int i = 0; i <= 80; i++) {
                                        double x = min + (max - min) * i / 80;
                                        double y = va * x * x + vb * x + vc;
                                        if (!Double.isFinite(y) || Math.abs(y) > 1000) {
                                                return false;
                                        }
                                }
                                return true;
                        }

                        // Sistemas 2×2 con intersección única y visible.
                        case "sistema_lineal" -> {
                                String[] campos = {"a1","b1","c1","a2","b2","c2",
                                        "minimoX","maximoX","minimoY","maximoY"};
                                for (String nombre : campos) {
                                        JsonNode valor = datos.path(nombre);
                                        if (!valor.isNumber() || !Double.isFinite(valor.doubleValue()))
                                                return false;
                                }
                                double a1=datos.path("a1").doubleValue(), b1=datos.path("b1").doubleValue();
                                double c1=datos.path("c1").doubleValue(), a2=datos.path("a2").doubleValue();
                                double b2=datos.path("b2").doubleValue(), c2=datos.path("c2").doubleValue();
                                double minX=datos.path("minimoX").doubleValue(), maxX=datos.path("maximoX").doubleValue();
                                double minY=datos.path("minimoY").doubleValue(), maxY=datos.path("maximoY").doubleValue();
                                double determinante=a1*b2-a2*b1;
                                if (Math.abs(determinante) < 1e-6
                                        || Math.hypot(a1,b1)<1e-6 || Math.hypot(a2,b2)<1e-6
                                        || Math.abs(a1)>20 || Math.abs(a2)>20
                                        || Math.abs(b1)>20 || Math.abs(b2)>20
                                        || Math.abs(c1)>100 || Math.abs(c2)>100
                                        || minX < -10 || maxX > 10 || minY < -10 || maxY > 10
                                        || minX >= maxX || minY >= maxY || maxX-minX < 1 || maxY-minY < 1)
                                        return false;
                                double x=(c1*b2-c2*b1)/determinante;
                                double y=(a1*c2-a2*c1)/determinante;
                                return Double.isFinite(x) && Double.isFinite(y)
                                        && x>=minX && x<=maxX && y>=minY && y<=maxY;
                        }

                        // Volúmenes de cilindros, conos y esferas: datos explícitos.
                        case "solido_revolucion" -> {
                                String forma = datos.path("forma").asText("");
                                JsonNode radio = datos.path("radio");
                                JsonNode altura = datos.path("altura");
                                JsonNode unidad = datos.path("unidad");
                                if (!("cilindro".equals(forma) || "cono".equals(forma)
                                        || "esfera".equals(forma)) || !radio.isNumber()
                                        || !unidad.isTextual() || unidad.asText().isBlank()
                                        || unidad.asText().length() > 12) {
                                        return false;
                                }
                                double r = radio.doubleValue();
                                if (!Double.isFinite(r) || r <= 0 || r > 50) return false;
                                if ("esfera".equals(forma)) {
                                        return altura.isMissingNode() || altura.isNull();
                                }
                                double h = altura.asDouble(Double.NaN);
                                return altura.isNumber() && Double.isFinite(h)
                                        && h > 0 && h <= 50;
                        }

                        // Proporcionalidad directa con cantidades positivas y etiquetas legibles.
                        case "modelo_proporcionalidad" -> {
                                JsonNode base = datos.path("cantidadBase");
                                JsonNode correspondiente = datos.path("cantidadCorrespondiente");
                                JsonNode unidadBase = datos.path("unidadBase");
                                JsonNode unidadCorrespondiente = datos.path("unidadCorrespondiente");
                                if (!base.isNumber() || !correspondiente.isNumber() ||
                                        !unidadBase.isTextual() || !unidadCorrespondiente.isTextual()) {
                                        return false;
                                }
                                double x = base.doubleValue();
                                double y = correspondiente.doubleValue();
                                return Double.isFinite(x) && Double.isFinite(y) &&
                                        x > 0 && x <= 10000 && y > 0 && y <= 10000 &&
                                        !unidadBase.asText().isBlank() &&
                                        unidadBase.asText().length() <= 20 &&
                                        !unidadCorrespondiente.asText().isBlank() &&
                                        unidadCorrespondiente.asText().length() <= 20;
                        }

                        // Representación geométrica de Pitágoras y triángulos semejantes.
                        case "triangulo_rectangulo" -> {
                                JsonNode a = datos.path("catetoA");
                                JsonNode b = datos.path("catetoB");
                                JsonNode unidad = datos.path("unidad");
                                if (!a.isNumber() || !b.isNumber() || !unidad.isTextual()) {
                                        return false;
                                }
                                double va = a.doubleValue();
                                double vb = b.doubleValue();
                                return Double.isFinite(va) && Double.isFinite(vb) &&
                                        va >= 0.1 && va <= 1000 && vb >= 0.1 && vb <= 1000 &&
                                        Math.max(va, vb) / Math.min(va, vb) <= 20 &&
                                        !unidad.asText().isBlank() && unidad.asText().length() <= 12;
                        }

                        // Probabilidad clásica: un conjunto finito de casos equiprobables.
                        case "modelo_probabilidad" -> {
                                JsonNode favorables = datos.path("favorables");
                                JsonNode total = datos.path("total");
                                JsonNode evento = datos.path("evento");
                                if (!favorables.isIntegralNumber() || !total.isIntegralNumber() ||
                                        !favorables.canConvertToInt() || !total.canConvertToInt() ||
                                        !evento.isTextual()) {
                                        return false;
                                }
                                int f = favorables.intValue();
                                int t = total.intValue();
                                return t >= 1 && t <= 24 && f >= 0 && f <= t &&
                                        !evento.asText().isBlank() && evento.asText().length() <= 80;
                        }

                        // Prisma rectangular: valores acotados para su dibujo proporcionado.
                        case "prisma_rectangular" -> {
                                JsonNode largo = datos.path("largo");
                                JsonNode ancho = datos.path("ancho");
                                JsonNode alto = datos.path("alto");
                                JsonNode unidad = datos.path("unidad");
                                if (!largo.isNumber() || !ancho.isNumber() || !alto.isNumber() ||
                                        !unidad.isTextual()) {
                                        return false;
                                }
                                double l = largo.doubleValue();
                                double a = ancho.doubleValue();
                                double h = alto.doubleValue();
                                return Double.isFinite(l) && Double.isFinite(a) && Double.isFinite(h) &&
                                        l >= 0.5 && l <= 50 && a >= 0.5 && a <= 50 &&
                                        h >= 0.5 && h <= 50 &&
                                        !unidad.asText().isBlank() && unidad.asText().length() <= 12;
                        }

                        // Segmento 1D: evita simular longitudes con rectángulos diminutos.
                        case "segmento_geometrico" -> {
                                JsonNode longitud = datos.path("longitud");
                                JsonNode unidad = datos.path("unidad");
                                return longitud.isNumber() && unidad.isTextual() &&
                                        Double.isFinite(longitud.doubleValue()) &&
                                        longitud.doubleValue() > 0 && longitud.doubleValue() <= 10000 &&
                                        !unidad.asText().isBlank() && unidad.asText().length() <= 12;
                        }

                        // Ax + b = c, con solución única dentro del rango explorable.
                        case "modelo_ecuacion" -> {
                                JsonNode a = datos.path("a"), b = datos.path("b"), c = datos.path("c");
                                if (!a.isNumber() || !b.isNumber() || !c.isNumber()) return false;
                                double va = a.doubleValue(), vb = b.doubleValue(), vc = c.doubleValue();
                                if (!Double.isFinite(va) || !Double.isFinite(vb) || !Double.isFinite(vc) ||
                                        Math.abs(va) < 0.001 || Math.abs(va) > 20 ||
                                        Math.abs(vb) > 100 || Math.abs(vc) > 100) return false;
                                double solucion = (vc - vb) / va;
                                return Double.isFinite(solucion) && solucion >= -20 && solucion <= 20;
                        }

                        // Tabla de pares: datos numéricos finitos y encabezados claros.
                        case "tabla_valores" -> {
                                JsonNode pares = datos.path("pares");
                                JsonNode x = datos.path("encabezadoX"), y = datos.path("encabezadoY");
                                if (!x.isTextual() || !y.isTextual() ||
                                        x.asText().isBlank() || y.asText().isBlank() ||
                                        x.asText().length() > 35 || y.asText().length() > 35 ||
                                        !pares.isArray() || pares.size() < 2 || pares.size() > 10) return false;
                                for (JsonNode par : pares) {
                                        JsonNode vx = par.path("x"), vy = par.path("y");
                                        if (!vx.isNumber() || !vy.isNumber() ||
                                                !Double.isFinite(vx.doubleValue()) ||
                                                !Double.isFinite(vy.doubleValue()) ||
                                                Math.abs(vx.doubleValue()) > 100000 ||
                                                Math.abs(vy.doubleValue()) > 100000) return false;
                                }
                                return true;
                        }

                        // Mapa conceptual: no inventa geometría para temas abstractos.
                        case "mapa_conceptual" -> {
                                JsonNode conceptos = datos.path("conceptos");
                                if (!conceptos.isArray() || conceptos.size() < 2 || conceptos.size() > 5) return false;
                                for (JsonNode item : conceptos) {
                                        JsonNode etiqueta = item.path("etiqueta"), detalle = item.path("detalle");
                                        if (!etiqueta.isTextual() || !detalle.isTextual() ||
                                                etiqueta.asText().isBlank() || detalle.asText().isBlank() ||
                                                etiqueta.asText().length() > 80 ||
                                                detalle.asText().length() > 240) return false;
                                }
                                return true;
                        }

                        // Ángulos sexagesimales con dos semirrectas precisas.
                        case "angulo_geometrico" -> {
                                JsonNode grados = datos.path("grados");
                                return grados.isNumber() && Double.isFinite(grados.doubleValue()) &&
                                        grados.doubleValue() >= 1 && grados.doubleValue() <= 359;
                        }

                        // Sectores circulares: proporciones no negativas y total del 100%.
                        case "grafica_circular" -> {
                                JsonNode segmentos = datos.path("segmentos");
                                if (!segmentos.isArray() || segmentos.size() < 2 || segmentos.size() > 8) return false;
                                double suma = 0;
                                for (JsonNode segmento : segmentos) {
                                        JsonNode etiqueta = segmento.path("etiqueta");
                                        JsonNode porcentaje = segmento.path("porcentaje");
                                        if (!etiqueta.isTextual() || etiqueta.asText().isBlank() ||
                                                etiqueta.asText().length() > 35 || !porcentaje.isNumber() ||
                                                !Double.isFinite(porcentaje.doubleValue()) ||
                                                porcentaje.doubleValue() <= 0 || porcentaje.doubleValue() >= 100) return false;
                                        suma += porcentaje.doubleValue();
                                }
                                return Math.abs(suma - 100) <= 0.01;
                        }

                        // Construcción completa: no requiere inventar una medida.
                        // Si la lección proporciona longitud/grados, se validan;
                        // si no, Angular dibuja un esquema sin medidas etiquetadas.
                        case "construccion_geometrica" -> {
                                String construccion = datos.path("construccion").asText("");
                                boolean conMediatriz = "mediatriz".equals(construccion)
                                        || "mediatriz_y_bisectriz".equals(construccion);
                                boolean conBisectriz = "bisectriz".equals(construccion)
                                        || "mediatriz_y_bisectriz".equals(construccion);
                                if (!conMediatriz && !conBisectriz) return false;

                                JsonNode longitud = datos.path("longitud");
                                JsonNode unidad = datos.path("unidad");
                                if (conMediatriz && !longitud.isMissingNode() && !longitud.isNull()) {
                                        if (!longitud.isNumber() || !unidad.isTextual()
                                                || !Double.isFinite(longitud.doubleValue())
                                                || longitud.doubleValue() <= 0 || longitud.doubleValue() > 10000
                                                || unidad.asText().isBlank() || unidad.asText().length() > 12) return false;
                                } else if (conMediatriz && !unidad.isMissingNode() && !unidad.isNull()) {
                                        // Una unidad sin longitud induce una medición falsa.
                                        return false;
                                }
                                JsonNode grados = datos.path("grados");
                                if (conBisectriz && !grados.isMissingNode() && !grados.isNull()) {
                                        if (!grados.isNumber() || !Double.isFinite(grados.doubleValue())
                                                || grados.doubleValue() < 30 || grados.doubleValue() > 150) return false;
                                }
                                // Rechazar campos ajenos a un subtipo, incluso si el JSON es válido.
                                if (!conMediatriz && (!longitud.isMissingNode() || !unidad.isMissingNode())) return false;
                                if (!conBisectriz && !grados.isMissingNode()) return false;
                                return true;
                        }

                                /**
                                 * RECURSO: TRIÁNGULOS CONGRUENTES
                                 *
                                 * Valida el criterio Lado-Lado-Lado (LLL).
                                 *
                                 * Angular representará dos triángulos con las
                                 * mismas longitudes correspondientes.
                                 *
                                 * No modifica ni afecta otros recursos visuales.
                                 */
                                case "triangulos_congruentes" -> {

                                // Validar el criterio de congruencia.
                                String criterio = datos.path("criterio").asText("");

                                if (!"LLL".equals(criterio)) {
                                        return false;
                                }

                                // Recuperar los tres lados del primer triángulo.
                                // El segundo tendrá las mismas medidas.
                                JsonNode ladoAB = datos.path("ladoAB");
                                JsonNode ladoBC = datos.path("ladoBC");
                                JsonNode ladoAC = datos.path("ladoAC");

                                // Las tres longitudes deben ser números positivos.
                                if (!dimensionValida(ladoAB)
                                        || !dimensionValida(ladoBC)
                                        || !dimensionValida(ladoAC)) {
                                        return false;
                                }

                                double ab = ladoAB.doubleValue();
                                double bc = ladoBC.doubleValue();
                                double ac = ladoAC.doubleValue();

                                // Comprobar la desigualdad triangular.
                                // La suma de dos lados debe superar al tercero.
                                if (ab + bc <= ac
                                        || ab + ac <= bc
                                        || bc + ac <= ab) {
                                        return false;
                                }

                                // Validar la unidad de medida.
                                String unidad = datos.path("unidad").asText("");

                                return unidad.equals("mm")
                                        || unidad.equals("cm")
                                        || unidad.equals("m")
                                        || unidad.equals("km");
                                }


                        // Rechaza tipos todavía no implementados.
                        default -> {
                        return false;
                        }
                }
                }

        /**
         * Protección adicional para una ecuación cuadrática explícita del
         * ejemplo, como x² - 5x + 6 = 0. Se omite si el enunciado no tiene
         * una ecuación reconocible o incluye varias distintas.
         *
         * No pretende interpretar todo el álgebra: el revisor de IA sigue
         * encargado de los demás casos y del contexto curricular.
         */
        String verificarCorrespondenciaCuadratica(
                Map<String, Object> leccion, JsonNode recurso) {

                if (recurso == null || !recurso.isObject()) return null;
                String tipo = recurso.path("tipo").asText("");
                if (!"grafica_funcion".equals(tipo)
                        && !"recta_numerica".equals(tipo)) return null;

                String texto = String.valueOf(leccion.getOrDefault("ejemplo_resuelto", ""));
                String normalizado = texto.replace('−', '-')
                        .replace('–', '-').replaceAll("\\s+", "");

                // a x² + b x + c = 0, con coeficientes numéricos opcionales.
                var expresion = java.util.regex.Pattern.compile(
                        "(?<![\\p{Alnum}])([+-]?(?:\\d+(?:\\.\\d+)?)?)x(?:²|\\^2)"
                        + "([+-](?:\\d+(?:\\.\\d+)?)?x)?"
                        + "([+-]\\d+(?:\\.\\d+)?)?=0(?![0-9])");
                var coincidencias = expresion.matcher(normalizado);
                double[] coeficientes = null;
                while (coincidencias.find()) {
                        double a = coeficienteExplicito(coincidencias.group(1));
                        String terminoX = coincidencias.group(2);
                        double b = terminoX == null ? 0
                                : coeficienteExplicito(terminoX.substring(0, terminoX.length() - 1));
                        double c = coincidencias.group(3) == null ? 0
                                : Double.parseDouble(coincidencias.group(3));
                        if (coeficientes != null) {
                                if (Math.abs(coeficientes[0] - a) > 1e-8
                                        || Math.abs(coeficientes[1] - b) > 1e-8
                                        || Math.abs(coeficientes[2] - c) > 1e-8) {
                                        return null; // Varios ejercicios: decidir con el revisor de IA.
                                }
                        }
                        coeficientes = new double[]{a, b, c};
                }
                if (coeficientes == null || coeficientes[0] == 0) return null;

                double a = coeficientes[0], b = coeficientes[1], c = coeficientes[2];
                double discriminante = b*b - 4*a*c;
                if (!Double.isFinite(discriminante) || discriminante < 0) return null;
                double raiz1 = (-b - Math.sqrt(discriminante)) / (2*a);
                double raiz2 = (-b + Math.sqrt(discriminante)) / (2*a);
                if (!Double.isFinite(raiz1) || !Double.isFinite(raiz2)) return null;

                if ("recta_numerica".equals(tipo) && Math.abs(raiz1 - raiz2) > 1e-8) {
                        return "La recta numérica solo destaca un punto y no muestra "
                                + "las dos raíces reales del ejemplo. Usa grafica_funcion.";
                }
                if (!"grafica_funcion".equals(tipo)) return null;

                JsonNode datos = recurso.path("datos");
                if (Math.abs(datos.path("a").asDouble(Double.NaN) - a) > 1e-8
                        || Math.abs(datos.path("b").asDouble(Double.NaN) - b) > 1e-8
                        || Math.abs(datos.path("c").asDouble(Double.NaN) - c) > 1e-8) {
                        return "Los coeficientes de la gráfica no coinciden con la cuadrática resuelta.";
                }
                double min = datos.path("minimoX").asDouble(Double.NaN);
                double max = datos.path("maximoX").asDouble(Double.NaN);
                if (!(min <= raiz1 && raiz1 <= max && min <= raiz2 && raiz2 <= max)) {
                        return "La gráfica debe incluir todas las raíces reales del ejemplo resuelto.";
                }
                return null;
        }

        private double coeficienteExplicito(String texto) {
                if (texto == null || texto.isEmpty() || "+".equals(texto)) return 1;
                if ("-".equals(texto)) return -1;
                return Double.parseDouble(texto);
        }

        /**
         * Comprueba si un recurso visual es pedagógicamente
         * adecuado para una lección existente.
         *
         * Primero verifica su estructura matemática.
         * Después solicita a OpenAI una revisión de coherencia
         * entre el diagrama y el contenido educativo.
         *
         * No modifica la lección ni guarda información.
         */
        public ResultadoValidacion validarRecursoVisualExistente(
                Map<String, Object> leccion,
                JsonNode recursoVisual
        ) {

        // Rechazar inmediatamente recursos con estructura inválida.
        if (!validarEstructuraRecursoVisual(recursoVisual)) {
                return new ResultadoValidacion(
                        false,
                        "El recurso visual tiene una estructura inválida."
                );
        }

        // Validación determinista adicional para cuadráticas explícitas.
        // Reduce confusiones de signos entre los factores y las raíces.
        String errorCuadratica = verificarCorrespondenciaCuadratica(leccion, recursoVisual);
        if (errorCuadratica != null) {
                return new ResultadoValidacion(false, errorCuadratica);
        }

        // Una construcción debe mostrar el resultado terminado.
        // Este filtro determinista evita aprobar un segmento inicial
        // en una lección cuyo ejemplo resuelto enseña la mediatriz.
        // También protege las lecciones que enseñan ambas construcciones.
        String ejemplo = String.valueOf(
                leccion.getOrDefault("ejemplo_resuelto", ""))
                .toLowerCase(java.util.Locale.ROOT);
        boolean requiereMediatriz = ejemplo.contains("mediatr");
        boolean requiereBisectriz = ejemplo.contains("bisectr");
        if (requiereMediatriz || requiereBisectriz) {
                if (!"construccion_geometrica".equals(
                        recursoVisual.path("tipo").asText(""))) {
                        return new ResultadoValidacion(false,
                                "El ejemplo exige el trazo final de la mediatriz o bisectriz; "
                                + "una figura inicial no es suficiente.");
                }
                String subtipo = recursoVisual.path("datos")
                        .path("construccion").asText("");
                boolean muestraMediatriz = "mediatriz".equals(subtipo)
                        || "mediatriz_y_bisectriz".equals(subtipo);
                boolean muestraBisectriz = "bisectriz".equals(subtipo)
                        || "mediatriz_y_bisectriz".equals(subtipo);
                if ((requiereMediatriz && !muestraMediatriz)
                        || (requiereBisectriz && !muestraBisectriz)) {
                        return new ResultadoValidacion(false,
                                "El recurso no muestra todas las construcciones finales "
                                + "del ejemplo resuelto.");
                }
        }

        // Instrucciones específicas para revisar el diagrama.
        
        String instrucciones = """
        Eres un especialista en Matemáticas de secundaria
        y didáctica educativa en México.

        Evalúa si el recurso visual propuesto representa
        correctamente un concepto de la lección existente.

        REGLAS GENERALES:

        1. Comprueba que los valores del recurso visual
        sean matemáticamente correctos.

        2. Los únicos tipos permitidos son:
        - "barra_fraccionaria"
        - "recta_numerica"
        - "figura_geometrica_2d"
        - "patron_figuras_2d"
        - "plano_cartesiano"
        - "grafica_estadistica"
        - "grafica_funcion" (y = ax² + bx + c)
        - "modelo_proporcionalidad" (relación directa)
        - "triangulo_rectangulo" (catetos y Pitágoras)
        - "modelo_probabilidad" (casos equiprobables)
        - "prisma_rectangular" (volumen, aristas)
        - "solido_revolucion" (cilindro, cono o esfera con medidas explícitas)
        - "sistema_lineal" (dos rectas y una intersección única)
        - "segmento_geometrico" (segmentos de una sola dimensión)
        - "modelo_ecuacion" (ecuación lineal ax+b=c)
        - "tabla_valores" (pares numéricos, sucesiones, funciones)
        - "mapa_conceptual" (conceptos extraídos de la lección)
        - "angulo_geometrico" (grados sexagesimales)
        - "grafica_circular" (sectores que suman 100%)
        - "construccion_geometrica" (mediatriz, bisectriz o ambas TERMINADAS)
        - "triangulos_congruentes" (comparación de dos triángulos mediante el criterio LLL)

        3. Comprueba que el recurso ayude a comprender
        un concepto central de la lección.

        4. No apruebes diagramas que contradigan
        la explicación o el ejemplo resuelto.

        5. No inventes información curricular,
        ejemplos, dimensiones ni valores que no
        estén presentes en la lección.

        6. Verifica que el recurso sea apropiado
        para el grado escolar, objetivo y PDA,
        cuando esa información esté disponible.

        6a. Un segmento es un objeto unidimensional. Rechaza cualquier
        "rectangulo" usado para simularlo con altura artificial (por ejemplo, 0.1 cm).
        Los porcentajes de grafica_circular deben coincidir con los valores reales.
        Los conceptos del mapa deben aparecer y ser correctos en la lección.

        6d. Para "sistema_lineal", comprueba que los coeficientes
        y constantes correspondan exactamente a las DOS ecuaciones
        del ejemplo, que la intersección satisface ambas y que
        no se confunda con un par ordenado inventado.

        6c. Para "solido_revolucion", comprueba si el ejemplo trata
        realmente un cilindro, cono o esfera y respeta radio, altura
        y unidad indicados. No inventes medidas ni confundas
        volumen (unidades cúbicas) con área (unidades cuadradas).
        Para una cuadrática, la gráfica de la función debe usar
        los coeficientes exactos del ejemplo y mostrar sus raíces
        reales como intersecciones con y=0, no como términos de factores.

        6b. Si el ejemplo enseña a CONSTRUIR una mediatriz o bisectriz,
        exige que el recurso muestre el trazo final: mediatriz
        perpendicular por el punto medio, o semirrecta bisectriz con
        ambas mitades del ángulo iguales. Un segmento o ángulo aislado
        es únicamente el paso inicial y NO representa el resultado.
        Para construccion_geometrica comprueba que cualquier longitud
        o ángulo inicial indicado corresponda al ejemplo; si NO aparecen
        medidas explícitas, el esquema NO debe afirmar una medida real.
        Para temas que construyen ambas, exige mediatriz_y_bisectriz
        con ambos resultados terminados.
        Para mediatriz, el recurso debe mostrar las dos intersecciones
        de los arcos de compás y la perpendicular. Para bisectriz, la
        semirrecta final debe dividir exactamente el ángulo en dos.

        6e. VALIDACIÓN DE TRIÁNGULOS CONGRUENTES:

        Si el recurso es "triangulos_congruentes":

        - Comprueba que la lección realmente explique
        congruencia de triángulos mediante el criterio
        Lado-Lado-Lado (LLL).

        - Verifica que ladoAB, ladoBC y ladoAC coincidan
        con las longitudes indicadas en el ejemplo resuelto.

        - No apruebes medidas inventadas, aproximadas
        arbitrariamente o tomadas de otro ejercicio.

        - Comprueba que los tres lados formen un
        triángulo geométricamente posible.

        - El triángulo ABC y el triángulo DEF deben
        tener los lados correspondientes iguales:

                AB = DE
                BC = EF
                AC = DF

        - Verifica que la correspondencia entre vértices
        y lados sea coherente con el ejemplo.

        - Los triángulos pueden tener orientaciones
        diferentes sin perder la congruencia.

        - No confundas congruencia con semejanza:
        en la congruencia los lados correspondientes
        tienen la misma longitud.

        - Si el ejemplo utiliza LAL o ALA, no apruebes
        un recurso que afirme demostrar ese criterio
        utilizando únicamente datos LLL.

        - Si las medidas no aparecen en el ejemplo
        y no pueden deducirse con certeza,
        devuelve aprobada=false.

        - El recurso debe permitir comprender por qué
        los triángulos son congruentes, no limitarse
        a mostrar dos figuras sin correspondencias.

        7. No modifiques ni regeneres la lección.

        8. Si no existe información suficiente
        para verificar la correspondencia
        matemática, devuelve aprobada=false.

        ============================================
        RECURSO 1: BARRA FRACCIONARIA
        ============================================

        Comprueba que la fracción representada
        corresponda a la explicación o al ejemplo.

        Se permiten:
        - Fracciones propias.
        - Fracciones impropias.
        - Fracciones aparentes.
        - Números mixtos positivos convertidos
          a fracciones impropias equivalentes.

        No apruebes fracciones inventadas
        ni valores sin relación con la lección.

        ============================================
        RECURSO 2: RECTA NUMÉRICA
        ============================================

        Comprueba que el número destacado
        corresponda al contenido educativo.

        Verifica:
        - minimo menor que maximo.
        - intervalo positivo.
        - valorDestacado dentro del rango.
        - Rango y marcas adecuados al concepto.

        Se permiten números positivos,
        negativos, cero y decimales.

        El valor destacado puede encontrarse
        entre dos marcas de la recta.

        No apruebes representaciones de
        desigualdades, intervalos abiertos
        o cerrados ni múltiples puntos.

        ============================================
        RECURSO 3: FIGURA GEOMÉTRICA 2D
        ============================================

        Comprueba que la figura represente
        correctamente el concepto matemático
        explicado en la lección.

        Figuras permitidas:
        - cuadrado
        - rectangulo
        - triangulo
        - circulo

        Verifica que las dimensiones correspondan
        a valores explícitos de la lección.

        Para cuadrados:
        - Comprueba la longitud del lado.

        Para rectángulos:
        - Comprueba la base y la altura.

        VALIDACIÓN DE RECTÁNGULOS ALGEBRAICOS:

        Si el recurso es "figura_geometrica_2d",
        la figura es "rectangulo" y contiene
        el objeto "algebra":

        1. Comprueba que el ejemplo resuelto realmente
        utilice un rectángulo con dimensiones
        expresadas algebraicamente.

        2. Verifica que "expresionBase" y
        "expresionAltura" correspondan exactamente
        a las expresiones del ejemplo resuelto.

        3. Comprueba que la incógnita sea "x".

        4. Verifica que las dimensiones numéricas
        "base" y "altura" sean las obtenidas
        al resolver el problema original.

        5. Comprueba que "areaConocida" coincida
        con el área indicada en la lección.

        6. Verifica que:
        base * altura = areaConocida.

        7. No apruebes expresiones, dimensiones
        ni áreas que no estén justificadas
        por el ejemplo resuelto.

        8. Si la lección utiliza únicamente
        dimensiones numéricas, el recurso
        no debe contener el objeto "algebra".

        9. Si el recurso tiene cálculos correctos,
        pero representa un problema diferente
        al de la lección, devuelve aprobada=false.

        10. Si no existe información suficiente
        para comprobar las expresiones y
        dimensiones, devuelve aprobada=false.

        Para rectángulos sin "algebra", conserva
        la validación normal de base y altura.

        Para triángulos:
        - Comprueba la base y la altura.

        Para círculos:
        - Comprueba el radio.

        No apruebes figuras que requieran
        representar propiedades que el
        componente actual no dibuja.

        No apruebes representaciones que
        pretendan demostrar ángulos,
        diagonales, semejanza, congruencia
        o el teorema de Pitágoras mediante
        dibujos geométricos insuficientes.

        No apruebes una figura aislada
        cuando la lección necesite representar
        varias etapas de una sucesión.

        ============================================
        RECURSO 4: PATRONES DE FIGURAS 2D
        ============================================

        Comprueba que el patrón represente
        correctamente una sucesión geométrica
        explicada en la lección.

        CAPACIDADES ACTUALES:

        - modalidad: "crecimiento".
        - Entre 2 y 6 figuras.
        - Figuras: cuadrado o rectangulo.
        - Todas las figuras del mismo tipo.
        - Posiciones consecutivas desde 1.
        - Dimensiones crecientes.

        No apruebes la modalidad "repetitivo"
        ni figuras que el componente actual
        no pueda representar.

        CORRESPONDENCIA CON LA LECCIÓN:

        1. Comprueba que las figuras utilizadas
        aparezcan en el contenido educativo.

        2. Verifica que las dimensiones de
        cada figura correspondan a los valores
        presentados en la lección.

        3. No apruebes figuras adicionales
        cuyos valores hayan sido inventados.

        4. Comprueba que el orden de la sucesión
        coincida con el ejemplo resuelto.

        VALIDACIÓN MATEMÁTICA:

        Para cuadrados:
        P = 4 * lado.

        Para rectángulos:
        P = 2 * (base + altura).

        Comprueba que los perímetros sean
        correctos y correspondan a la unidad
        de medida utilizada.

        VALIDACIÓN DE LA REGLA:

        Verifica que el campo "regla"
        describa correctamente la relación
        matemática de la sucesión.

        Si la lección presenta una expresión
        algebraica, comprueba que la regla
        del recurso sea equivalente.

        Por ejemplo, si la lección establece
        P(x) = 4x para cuadrados cuyos lados
        son 1, 2, 3 y 4 unidades, comprueba
        que los perímetros correspondientes
        sean 4, 8, 12 y 16 unidades.

        Este ejemplo es ilustrativo.
        No lo utilices como justificación
        para aprobar valores que no aparezcan
        en la lección evaluada.

        RECHAZA EL RECURSO SI:

        - La regla contradice los valores.
        - Los perímetros son incorrectos.
        - Las dimensiones no corresponden
          a la lección.
        - Se inventan términos de la sucesión.
        - Se mezclan figuras incompatibles.
        - El patrón no representa un concepto
          central del contenido educativo.

        ============================================
        RECURSO 5: PLANO CARTESIANO
        ============================================

        Comprueba que el plano cartesiano represente
        correctamente las coordenadas y conceptos
        matemáticos explicados en la lección.

        CAPACIDADES ACTUALES:

        - Representación de ejes X e Y.
        - Coordenadas positivas y negativas.
        - Representación del origen (0, 0).
        - Cuadrícula con intervalos uniformes.
        - Entre 1 y 10 puntos con etiquetas.
        - Ubicación de puntos en los cuatro cuadrantes.

        El componente actual representa puntos
        individuales, pero no dibuja segmentos,
        rectas, funciones ni polígonos.

        CORRESPONDENCIA CON LA LECCIÓN:

        1. Comprueba que el contenido educativo
        explique la ubicación de puntos o el uso
        del sistema de coordenadas cartesianas.

        2. Verifica que las coordenadas propuestas
        correspondan a valores presentes
        explícitamente en la lección.

        3. No apruebes puntos adicionales
        cuyas coordenadas hayan sido inventadas.

        4. Comprueba que las etiquetas de los puntos
        sean coherentes con el ejemplo resuelto.

        5. Verifica que el plano represente
        un concepto central de la lección.

        VALIDACIÓN MATEMÁTICA:

        - minimoX debe ser menor que maximoX.
        - minimoY debe ser menor que maximoY.
        - intervalo debe ser positivo.
        - Cada coordenada debe encontrarse
        dentro de los límites establecidos.
        - Los valores del eje X aumentan
        de izquierda a derecha.
        - Los valores del eje Y aumentan
        de abajo hacia arriba.
        - Las coordenadas se interpretan
        en el orden (x, y).

        Si la lección presenta el punto A(2, 3),
        el recurso debe ubicarlo en x=2, y=3.

        Este ejemplo es ilustrativo.
        No lo utilices para justificar puntos
        que no aparezcan en la lección evaluada.

        RECHAZA EL RECURSO SI:

        - Se intercambian las coordenadas X e Y.
        - Se representan puntos incorrectos.
        - Las coordenadas contradicen la lección.
        - Se inventan puntos o etiquetas.
        - Se intenta representar una función,
        una recta o una figura que el
        componente todavía no puede dibujar.
        - El plano no aporta valor pedagógico
        al contenido educativo.
        
        ============================================
        RECURSO 6: GRÁFICA ESTADÍSTICA
        ============================================

        Comprueba que la gráfica represente
        correctamente datos estadísticos presentes
        en el contenido educativo de la lección.

        CAPACIDADES ACTUALES:

        - tipo: "grafica_estadistica".
        - modalidad: "barras" o "lineas".
        - Entre 1 y 8 categorías para barras.
        - Entre 2 y 8 puntos para líneas.
        - Valores numéricos finitos y no negativos.
        - Etiquetas breves para cada categoría.
        - Una unidad de medida común.
        - Escala vertical que comienza en cero.

        El componente actual no representa
        gráficas circulares, histogramas,
        gráficas de dispersión ni múltiples
        series de datos.

        CORRESPONDENCIA CON LA LECCIÓN:

        1. Comprueba que las categorías y los
        valores correspondan a información
        explícita de la lección.

        2. Verifica que las cantidades coincidan
        con las tablas, ejemplos o problemas
        presentados en el contenido educativo.

        3. No apruebes categorías adicionales
        ni cantidades inventadas.

        4. Comprueba que la unidad de medida
        sea coherente con los datos representados.

        5. Verifica que la gráfica contribuya
        a comprender un concepto central
        de la lección.

        VALIDACIÓN ESTADÍSTICA:

        Para gráficas de barras:

        - Comprueba que los datos representen
        categorías comparables.
        - Verifica que cada barra corresponda
        al valor correcto.
        - No apruebes categorías duplicadas
        que deban combinarse.
        - No apruebes datos que requieran
        representar varias series.

        Para gráficas de líneas:

        - Comprueba que los puntos tengan
        un orden significativo.
        - Prioriza datos que representen cambios
        a través del tiempo o una secuencia.
        - Verifica que el orden de los puntos
        coincida con la lección.
        - No apruebes conexiones entre categorías
        que no tengan una continuidad lógica.
        - No apruebes interpretaciones que
        impliquen valores intermedios no
        justificados por el contenido.

        INTERPRETACIÓN DE RESULTADOS:

        - Comprueba que las comparaciones
        entre cantidades sean correctas.
        - Verifica que las tendencias descritas
        coincidan con los valores.
        - No apruebes conclusiones estadísticas
        que contradigan los datos.
        - No inventes frecuencias, porcentajes
        ni resultados adicionales.

        RECHAZA EL RECURSO SI:

        - Las cantidades no coinciden
        con la lección.
        - Se inventan categorías o valores.
        - La unidad de medida es incorrecta.
        - Se utiliza una modalidad inadecuada.
        - El orden de los puntos es incorrecto.
        - La gráfica sugiere conclusiones
        que los datos no respaldan.
        - El componente visual no puede
        representar fielmente la información.
        - La gráfica no aporta valor pedagógico
        al contenido educativo.

        ============================================
        DECISIÓN PEDAGÓGICA
        ============================================

        Aprueba únicamente cuando:

        - El recurso tenga correspondencia
          matemática con la lección.
        - La representación sea adecuada
          para el grado y objetivo.
        - Los valores sean correctos.
        - El componente visual pueda mostrar
          fielmente el concepto.

        Si existe una contradicción o no
        puedes comprobar la correspondencia,
        devuelve aprobada=false.

        FORMATO:

        Devuelve exclusivamente un objeto JSON válido:

        {
          "aprobada": true,
          "observaciones": "El recurso corresponde a la lección."
        }

        Si detectas errores, devuelve aprobada=false
        y explica concretamente el problema.

        No incluyas Markdown ni texto fuera del JSON.
        """;

        try {

                // Enviar a la IA solo el contenido existente
                // y el recurso visual que necesitamos revisar.
                String contenido = objectMapper.writeValueAsString(
                        Map.of(
                                "leccion", leccion,
                                "recursoVisual", recursoVisual
                        )
                );

                String respuesta = generadorIA.generarTexto(
                        instrucciones,
                        contenido
                );

                // Interpretar la respuesta estructurada.
                JsonNode resultado = objectMapper.readTree(respuesta);

                JsonNode aprobada = resultado.path("aprobada");
                JsonNode observaciones = resultado.path("observaciones");

                // Rechazar respuestas que no cumplan el formato.
                if (!aprobada.isBoolean()
                        || !observaciones.isTextual()
                        || observaciones.asText().isBlank()) {

                throw new IllegalStateException(
                        "La validación pedagógica devolvió un formato inválido."
                );
                }

                return new ResultadoValidacion(
                        aprobada.asBoolean(),
                        observaciones.asText().trim()
                );

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new IllegalStateException(
                        "No fue posible interpretar la validación pedagógica.",
                        e
                );
        }
        }

        /**
         * Verifica que una dimensión geométrica sea válida.
         *
         * Reglas:
         * - Debe ser un número.
         * - Debe ser finito.
         * - Debe ser mayor que cero.
         * - Se limita a 10000 unidades para evitar
         *   valores excesivos generados por IA.
         */
        private boolean dimensionValida(JsonNode dimension) {

        if (dimension == null || !dimension.isNumber()) {
                return false;
        }

        double valor = dimension.doubleValue();

        return Double.isFinite(valor)
                && valor > 0
                && valor <= 10000;
        }

        public record ResultadoValidacion(
                boolean aprobada,
                String observaciones
        ) {}
}