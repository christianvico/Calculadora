import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Clase principal Ap que implementa un servidor HTTP para resolver
 * operaciones matematicas basicas con enteros de hasta 5 digitos.
 */
public class Ap implements HttpHandler {

    /** Primer operando entero global [-99999, 99999]. */
    public static int n1;

    /** Segundo operando entero global [-99999, 99999]. */
    public static int n2;

    /** Codigo de la operacion solicitada (dv, sm, rt, mt, pt). */
    public static String op;

    /** Resultado de la operacion o mensaje de error. */
    public static String rs;

    /** Instancia global del servidor HTTP embebido. */
    public static HttpServer sv;

    /** Cadena de consulta (query string) extraida de la URL. */
    public static String qr;

    /** Arreglo global de parametros separados por ampersand. */
    public static String[] pr;

    /** Arreglo global temporal para separar clave y valor. */
    public static String[] kv;

    /** Indice global para iteraciones en arreglos. */
    public static int i;

    /** Arreglo de bytes global para enviar la respuesta HTTP. */
    public static byte[] rp;

    /**
     * Primera funcion declarada: realiza la division entre n1 y n2.
     * Valida que el divisor no sea cero antes de operar.
     */
    public static void dv() {
        if (n2 == 0) {
            rs = "Error: No se puede dividir entre cero.";
        } else {
            rs = String.valueOf((double) n1 / n2);
        }
    }

    /**
     * Realiza la suma aritmetica entre los operandos globales n1 y n2.
     */
    public static void sm() {
        rs = String.valueOf((long) n1 + n2);
    }

    /**
     * Realiza la resta aritmetica entre los operandos globales n1 y n2.
     */
    public static void rt() {
        rs = String.valueOf((long) n1 - n2);
    }

    /**
     * Realiza la multiplicacion entre n1 y n2 evitando desbordamiento.
     */
    public static void mt() {
        rs = String.valueOf((long) n1 * n2);
    }

    /**
     * Calcula la potencia de n1 elevado al exponente n2.
     */
    public static void pt() {
        rs = String.valueOf(Math.pow(n1, n2));
    }

    /**
     * Comprueba que ambos operandos esten en el rango [-99999, 99999].
     * @return true si ambos numeros son validos; false en caso contrario.
     */
    public static boolean vl() {
        return n1 >= -99999 && n1 <= 99999 && n2 >= -99999 && n2 <= 99999;
    }

    /**
     * Selecciona y ejecuta la operacion matematica correspondiente a op.
     */
    public static void ej() {
        if (!vl()) {
            rs = "Error: Los numeros deben ser enteros entre -99999 y 99999.";
            return;
        }
        switch (op) {
            case "sm":
                sm();
                break;
            case "rt":
                rt();
                break;
            case "mt":
                mt();
                break;
            case "pt":
                pt();
                break;
            case "dv":
                dv();
                break;
            default:
                rs = "Error: Operacion no permitida.";
        }
    }

    /**
     * Gestiona las peticiones HTTP entrantes desde el frontend HTML.
     * @param ex Objeto de intercambio HTTP con la peticion y respuesta.
     * @throws IOException Si ocurre un error en el flujo de salida.
     */
    @Override
    public void handle(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        qr = ex.getRequestURI().getQuery();
        n1 = 0;
        n2 = 0;
        op = "";

        try {
            if (qr != null) {
                pr = qr.split("&");
                for (i = 0; i < pr.length; i++) {
                    kv = pr[i].split("=");
                    if (kv.length == 2) {
                        if (kv[0].equals("n1")) {
                            n1 = Integer.parseInt(kv[1]);
                        } else if (kv[0].equals("n2")) {
                            n2 = Integer.parseInt(kv[1]);
                        } else if (kv[0].equals("op")) {
                            op = kv[1];
                        }
                    }
                }
                ej();
            } else {
                rs = "Error: Faltan parametros.";
            }
        } catch (NumberFormatException e) {
            rs = "Error: Solo se permiten numeros enteros de hasta 5 digitos.";
        }

        rp = rs.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(200, rp.length);
        OutputStream os = ex.getResponseBody();
        os.write(rp);
        os.close();
    }

    /**
     * Punto de entrada de la aplicacion Java. Inicia el servidor en el puerto 8080.
     * @param ar Argumentos de linea de comandos.
     * @throws IOException Si el puerto 8080 esta ocupado.
     */
    public static void main(String[] ar) throws IOException {
        sv = HttpServer.create(new InetSocketAddress(8080), 0);
        sv.createContext("/op", new Ap());
        sv.setExecutor(null);
        sv.start();
        System.out.println("Servidor iniciado en http://localhost:8080/op");
    }
}