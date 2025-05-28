package servicios;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import entidades.Documento;

public class ServicioDocumento {

    private static List<Documento> documentos = new ArrayList<>();
    private static List<Integer> coincidencias; // Guarda los índices de las coincidencias
    private static int posicionActual; // Índice actual en la lista de coincidencias

    // Métodos de carga y visualización (sin cambios)
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
                // Manejo de excepciones
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

    // Método de ordenación por inserción recursivo (nuevo)
    public static void ordenarInsercionRecursivo(int criterio) {
        ordenarInsercionRecursivo(documentos.size() - 1, criterio);
    }

    private static void ordenarInsercionRecursivo(int n, int criterio) {
        if (n <= 0) return;
        
        ordenarInsercionRecursivo(n - 1, criterio);
        
        Documento ultimo = documentos.get(n);
        int j = n - 1;
        
        while (j >= 0 && esMayor(documentos.get(j), ultimo, criterio)) {
            documentos.set(j + 1, documentos.get(j));
            j--;
        }
        documentos.set(j + 1, ultimo);
    }

    public static List<Documento> getDocumentos() {
        return documentos;
    }

    // Resto de los métodos (sin cambios)
    private static boolean estaOrdenadaPorNombreCompleto() {
        for (int i = 0; i < documentos.size() - 1; i++) {
            if (documentos.get(i).getNombreCompleto().compareTo(documentos.get(i + 1).getNombreCompleto()) > 0) {
                return false;
            }
        }
        return true;
    }

    public static List<Documento> buscarCoincidencias(String texto) {
        List<Documento> resultados = new ArrayList<>();
        texto = texto.toLowerCase().trim();

        if (!estaOrdenadaPorNombreCompleto()) {
            ordenarRapido(0); // Asegura que esté ordenado por nombre completo
        }

        int posAproximada = busquedaBinaria(texto, 0, documentos.size() - 1);
        
        if (posAproximada != -1) {
            int i = posAproximada;
            while (i >= 0 && documentos.get(i).getNombreCompleto().toLowerCase().contains(texto)) {
                resultados.add(0, documentos.get(i));
                i--;
            }
            
            i = posAproximada + 1;
            while (i < documentos.size() && documentos.get(i).getNombreCompleto().toLowerCase().contains(texto)) {
                resultados.add(documentos.get(i));
                i++;
            }
        }

        return resultados;
    }

    private static int busquedaBinaria(String texto, int inicio, int fin) {
        if (inicio > fin) {
            return -1;
        }

        int medio = inicio + (fin - inicio) / 2;
        String nombreMedio = documentos.get(medio).getNombreCompleto().toLowerCase();
        
        if (nombreMedio.contains(texto)) {
            return medio;
        }
        
        int comparacion = texto.compareTo(nombreMedio);
        
        if (comparacion < 0) {
            if (medio > inicio && nombreMedio.startsWith(texto.substring(0, Math.min(texto.length(), 1)))) {
                int izquierda = busquedaBinaria(texto, inicio, medio - 1);
                if (izquierda != -1) return izquierda;
            }
            return busquedaBinaria(texto, inicio, medio - 1);
        } else {
            if (medio < fin && nombreMedio.startsWith(texto.substring(0, Math.min(texto.length(), 1)))) {
                int derecha = busquedaBinaria(texto, medio + 1, fin);
                if (derecha != -1) return derecha;
            }
            return busquedaBinaria(texto, medio + 1, fin);
        }
    }

    public static void buscarTodasCoincidencias(String texto) {
        coincidencias = new ArrayList<>();
        texto = texto.toLowerCase();

        int primeraCoincidencia = busquedaBinaria(texto, 0, documentos.size() - 1);

        if (primeraCoincidencia != -1) {
            for (int i = primeraCoincidencia; i >= 0; i--) {
                if (documentos.get(i).getNombreCompleto().toLowerCase().contains(texto)) {
                    coincidencias.add(0, i);
                } else {
                    break;
                }
            }

            for (int i = primeraCoincidencia + 1; i < documentos.size(); i++) {
                if (documentos.get(i).getNombreCompleto().toLowerCase().contains(texto)) {
                    coincidencias.add(i);
                } else {
                    break;
                }
            }
        }

        posicionActual = coincidencias.isEmpty() ? -1 : 0;
    }

    public static int siguienteCoincidencia() {
        if (coincidencias == null || coincidencias.isEmpty() || posicionActual == -1) {
            return -1;
        }

        posicionActual++;
        if (posicionActual >= coincidencias.size()) {
            posicionActual = 0;
        }

        return coincidencias.get(posicionActual);
    }

    public static int anteriorCoincidencia() {
        if (coincidencias == null || coincidencias.isEmpty() || posicionActual == -1) {
            return -1;
        }

        posicionActual--;
        if (posicionActual < 0) {
            posicionActual = coincidencias.size() - 1;
        }

        return coincidencias.get(posicionActual);
    }
}
