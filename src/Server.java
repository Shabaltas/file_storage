import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Server {

    private static String IP_ADDRESS = "localhost";
    private static String dir = Paths.get("").toAbsolutePath().toString() + "\\filesstorage\\";
    private static int status = 200;
    private static byte[] response;

    static {
        try {
            IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        boolean ok = false;
        while (!ok){
            try {
                System.out.println("Enter port number");
                Scanner in = new Scanner(System.in);
                HttpServer server = HttpServer.create(new InetSocketAddress(in.nextInt()), 5);
                ok = true;
                System.out.println(IP_ADDRESS + ":" + server.getAddress().getPort());
                server.createContext("/", new MyHandler());
                server.setExecutor(null); // creates a default executor
                server.start();
            }catch (InputMismatchException e){
                System.out.println("Wrong input");
            }catch (BindException e){
                System.out.println("Port is already in use");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private static class MyHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {

            response = new byte[0];
            String requestURI = exchange.getRequestURI().toString();
            System.out.println(exchange.getRequestMethod() + " " + exchange.getRequestURI() + " " + exchange.getProtocol());
            Headers headers = exchange.getRequestHeaders();
            for (String key : headers.keySet()){
                System.out.println(key + " : " + headers.get(key));
            }

            String filename = requestURI.substring(requestURI.lastIndexOf('/') + 1);
            try {
                switch (exchange.getRequestMethod()) {

                    //чтение файла
                    case "GET":
                        doGet(filename);
                        break;

                    //добавление в конец файла
                    case "POST":
                        doPost(exchange, filename);
                        break;

                    //перезапись файла
                    case "PUT":
                        doPut(exchange, filename);
                        break;

                    //удаление файла
                    case "DELETE":
                        doDelete(filename);
                        break;

                    //перемещение файла
                    case "MOVE":
                        doMove(exchange, filename);
                        break;

                    //копирование файла
                    case "COPY":
                        doCopy(exchange, filename);
                        break;
                    default:
                        status = 405;
                }
            }catch (FileNotFoundException | NoSuchFileException e) {
                status = 404;
            } catch (IOException e){
                status = 400;
            }
            exchange.sendResponseHeaders(status, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

        private static void doGet(String filename) throws  IOException{
            response = Files.readAllBytes(Paths.get(dir + filename));
        }

        private static void doPost(HttpExchange exchange, String filename) throws IOException{
            Path path = Paths.get(dir + filename);
            if (!Files.exists(path)){
                Files.createFile(path);
            }
            Files.write(path, getParams(exchange.getRequestBody(), new String[]{"content"})[0].getBytes(), StandardOpenOption.APPEND);
        }

        private static void doPut(HttpExchange exchange, String filename) throws IOException{
            Path path = Paths.get(dir + filename);
            if (!Files.exists(path)){
                Files.createFile(path);
            }
            Files.write(path, getParams(exchange.getRequestBody(), new String[]{"content"})[0].getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        }

        private static void doDelete(String filename) throws IOException{
            Files.delete(Paths.get(dir + filename));
        }

        private static void doMove(HttpExchange exchange, String filename) throws IOException{
            Path path = Paths.get(getParams(exchange.getRequestBody(), new String[]{"newPath"})[0] + filename);
            if (Files.exists(path)){
                exchange.getResponseHeaders().put("message", new ArrayList<>(Arrays.asList("File with new name is already exists ")));
                throw new IOException();
            }
            Files.move(Paths.get(dir + filename), path);
        }

        private static void doCopy(HttpExchange exchange, String filename) throws IOException{
            String[] params = getParams(exchange.getRequestBody(), new String[]{"newPath", "newFilename"});
            Path path = Paths.get(params[0] + params[1]);
            if (Files.exists(path)){
                exchange.getResponseHeaders().put("message", new ArrayList<>(Arrays.asList("File with new name is already exists ")));
                throw new IOException();
            }
            Files.copy(Paths.get(dir + filename), path);//, StandardCopyOption.REPLACE_EXISTING);
        }

        private static String[] getParams(InputStream body, String[] params) throws IOException{
            StringBuilder bodystr = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(body));
            String line;
            while ((line = in.readLine()) != null) {
                bodystr.append(URLDecoder.decode(line));
            }

            String[] result = new String[params.length];
            int i = 0;
            for (String param : params) {
                int start = bodystr.indexOf(param + "=") + param.length() + 1;
                if (start > bodystr.lastIndexOf("&")) {
                    result[i++] = bodystr.substring(start);
                } else {
                    int end = bodystr.substring(start).indexOf("&");
                    result[i++] = bodystr.substring(start, end + start);
                }
            }
            in.close();
            return result;
        }
    }
}
