package servicios;

import entidades.Documento;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ServicioDocumento {

    private static List<Documento> documentos = new ArrayList<>();

    public static void cargar(String nombreArchivo) {
        var br = Archivo.abrirArchivo(nombreArchivo);
        if (br != null) {
            try {
                var linea = br.readLine();
                linea = br.readLine();
                while (linea != null) {
                    var textos = linea.split(";");
                    var documento = new Documento(textos[0], textos[1], textos[2], textos[3]);
                    documentos.add(documento);
                    linea = br.readLine();
                }
            } catch (Exception ex) {

            }
        }
    }

    public static void mostrar(JTable tbl) {
        String[] encabezados = new String[] { "#", "Primer Apellido", "Segundo Apellido", "Nombres", "Documento" };
        String[][] datos = new String[documentos.size()][encabezados.length];

        int fila = 0;
        for (var documento : documentos) {
            datos[fila][0] = String.valueOf(fila + 1);
            datos[fila][1] = documento.getApellido1();
            datos[fila][2] = documento.getApellido2();
            datos[fila][3] = documento.getNombre();
            datos[fila][4] = documento.getDocumento();
            fila++;
        }

        var dtm = new DefaultTableModel(datos, encabezados);
        tbl.setModel(dtm);
    }

    private static boolean esMayor(Documento d1, Documento d2, int criterio) {
        if (criterio == 0) {
            return d1.getNombreCompleto().compareTo(d2.getNombreCompleto()) > 0 ||
                    (d1.getNombreCompleto().equals(d2.getNombreCompleto()) &&
                            d1.getDocumento().compareTo(d2.getDocumento()) > 0);
        } else {
            return d1.getDocumento().compareTo(d2.getDocumento()) > 0 ||
                    (d1.getDocumento().equals(d2.getDocumento()) &&
                            d1.getNombreCompleto().compareTo(d2.getNombreCompleto()) > 0);
        }
    }

    private static void intercambiar(int origen, int destino) {
        if (0 <= origen && origen < documentos.size() &&
                0 <= destino && destino < documentos.size()) {
            var temporal = documentos.get(origen);
            documentos.set(origen, documentos.get(destino));
            documentos.set(destino, temporal);
        }
    }

    public static void ordenarBurbuja(int criterio) {
        for (int i = 0; i < documentos.size() - 1; i++) {
            for (int j = i + 1; j < documentos.size(); j++) {
                System.out.println(
                        "d[i]=" + documentos.get(i).getNombreCompleto() + " " + documentos.get(i).getDocumento());
                System.out.println(
                        "d[j]=" + documentos.get(j).getNombreCompleto() + " " + documentos.get(j).getDocumento());

                if (esMayor(documentos.get(i), documentos.get(j), criterio)) {
                    intercambiar(i, j);
                }
            }
        }
    }

    private static int getPivote(int inicio, int fin, int criterio) {
        var pivote = inicio;
        var documentoPivote = documentos.get(pivote);

        for (int i = inicio + 1; i <= fin; i++) {
            if (esMayor(documentoPivote, documentos.get(i), criterio)) {
                pivote++;
                if (i != pivote) {
                    intercambiar(i, pivote);
                }
            }
        }
        if (inicio != pivote) {
            intercambiar(inicio, pivote);
        }

        return pivote;
    }

    private static void ordenarRapido(int inicio, int fin, int criterio) {
        if (fin > inicio) {
            var pivote = getPivote(inicio, fin, criterio);
            ordenarRapido(inicio, pivote - 1, criterio);
            ordenarRapido(pivote + 1, fin, criterio);
        }
    }

    public static void ordenarRapido(int criterio) {
        ordenarRapido(0, documentos.size() - 1, criterio);
    }

    public static void ordenarInsercion(int criterio) {
        for (int i = 1; i < documentos.size(); i++) {
            var documentoActual = documentos.get(i);
            // mover los documentos mayores que el actual
            int j = i - 1;
            while (j >= 0 && esMayor(documentos.get(j), documentoActual, criterio)) {
                documentos.set(j + 1, documentos.get(j));
                j--;
            }
            // insertar el documento
            documentos.set(j + 1, documentoActual);
        }
    }

    private static void ordenarInsercionRecursivo(int posicion, int criterio) {
        if (posicion == 0) {
            return;
        }
        ordenarInsercionRecursivo(posicion - 1, criterio);

        var documentoActual = documentos.get(posicion);
        // System.out.println(documentoActual.getNombreCompleto());
        // mover los documentos mayores que el actual
        int j = posicion - 1;
        while (j >= 0 && esMayor(documentos.get(j), documentoActual, criterio)) {
            // System.out.println(documentos.get(j));
            documentos.set(j + 1, documentos.get(j));
            j--;
        }
        // insertar el documento
        documentos.set(j + 1, documentoActual);
    }

    public static void ordenarInsercionRecursivo(int criterio) {
        ordenarInsercionRecursivo(documentos.size() - 1, criterio);
    }

    // Busqueda binaria
    private static List<Integer> resultadosBusqueda = new ArrayList<>();
    private static int indiceResultadoActual = -1;
    private static int indiceActual = -1;

    public static int buscarCoincidencia(String texto) {
        resultadosBusqueda.clear(); // Limpiar resultados anteriores
        indiceResultadoActual = -1; // Reiniciar posición

        if (documentos.isEmpty()) return -1;
        texto = texto.toLowerCase().trim();

        // Buscar coincidencias en todos los campos y guardar sus índices
        for (int i = 0; i < documentos.size(); i++) {
            Documento doc = documentos.get(i);
            if (doc.getApellido1().toLowerCase().contains(texto) ||
                doc.getApellido2().toLowerCase().contains(texto) ||
                doc.getNombre().toLowerCase().contains(texto)) {
                resultadosBusqueda.add(i); // Guardar índice del documento coincidente
            }
        }

        if (!resultadosBusqueda.isEmpty()) {
            indiceResultadoActual = 0; // Empezar en el primer resultado
            indiceActual = resultadosBusqueda.get(0); // Actualizar índice actual
            return indiceActual;
        }

        return -1; // No se encontraron coincidencias
    }

    public static int getSiguienteCoincidencia() {
        if (resultadosBusqueda.isEmpty() || indiceResultadoActual == -1) return -1;

        indiceResultadoActual++;
        if (indiceResultadoActual >= resultadosBusqueda.size()) {
            indiceResultadoActual = 0; // Volver al inicio si llegamos al final
        }

        indiceActual = resultadosBusqueda.get(indiceResultadoActual);
        return indiceActual;
    }

    public static int getAnteriorCoincidencia() {
        if (resultadosBusqueda.isEmpty() || indiceResultadoActual == -1) return -1;

        indiceResultadoActual--;
        if (indiceResultadoActual < 0) {
            indiceResultadoActual = resultadosBusqueda.size() - 1; // Ir al final si estamos al inicio
        }

        indiceActual = resultadosBusqueda.get(indiceResultadoActual);
        return indiceActual;
    }

    public static Documento getDocumento(int index) {
        if (index >= 0 && index < documentos.size()) {
            return documentos.get(index);
        }
        return null;
    }

    public static int getIndiceActual() {
        return indiceActual;
    }

    public static void setIndiceActual(int index) {
        indiceActual = index;
    }

}