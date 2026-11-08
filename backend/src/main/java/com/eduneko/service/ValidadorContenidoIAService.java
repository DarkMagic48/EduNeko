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

            12. Los únicos tipos de recursos visuales
                admitidos actualmente son:
                - "barra_fraccionaria"
                - "recta_numerica"

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

                        case "rectangulo", "triangulo" -> {

                        // Ambas figuras requieren base y altura positivas.
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

                        // Rechaza tipos todavía no implementados.
                        default -> {
                        return false;
                        }
                }
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