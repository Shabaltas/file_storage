package myHTTP;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.file.*;

public class Server {

    private static String IP_ADDRESS = "localhost";
    private static String dir = Paths.get("").toAbsolutePath().toString() + "\\src\\";
    private static int status;
    private static byte[] response;

    static {
        try {
            IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("http://" + IP_ADDRESS + ":8080/");
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 5);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
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
            switch (exchange.getRequestMethod()){

                //чтение файла
                case "GET":
                    doGet(filename);
                    //doGet(requestURI.substring(requestURI.lastIndexOf("/") + 1));
                    break;

                //добавление в конец файла
                case "POST":
                    doPost(filename, exchange.getRequestBody());
                    break;

                //перезапись файла
                case "PUT":
                    doPut(filename, exchange.getRequestBody());
                    break;

                //удаление файла
                case "DELETE":
                     doDelete(filename);
                    break;

                //перемещение файла
                case "MOVE":
                    doMove(filename, exchange.getRequestBody());
                    break;

                //копирование файла
                case "COPY":
                    doCopy(filename, exchange.getRequestBody());
                    break;
                default:
                    status = 405;
                    return;
            }
            exchange.sendResponseHeaders(status, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

        private static void doGet(String filename){
            try {
                response = Files.readAllBytes(Paths.get(dir + filename));
                status = 200;
                //response = new BufferedReader(new InputStreamReader(new FileInputStream(filename))).readLine();
            } catch (FileNotFoundException | NoSuchFileException e) {
                status = 404;
            } catch (IOException e){
                status = 400;
            }
        }

        private static void doPost(String filename, InputStream body){
            try {
                Files.write(Paths.get(dir + filename), getParams(body, new String[]{"content"})[0].getBytes(), StandardOpenOption.APPEND);
                status = 200;
            }catch (FileNotFoundException | NoSuchFileException e) {
                status = 404;
            }catch (IOException e){
                status = 400;
            }
        }

        private static void doPut(String filename, InputStream body){
            StringBuilder bodystr = new StringBuilder();
            try{
                Files.write(Paths.get(dir + filename), getParams(body, new String[]{"content"})[0].getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                status = 200;
            }catch (FileNotFoundException | NoSuchFileException e) {
                status = 404;
            }catch (IOException e){
                status = 400;
            }
        }

        private static void doDelete(String filename){
            try {
                Files.deleteIfExists(Paths.get(dir + filename));
                status = 200;
            } catch (FileNotFoundException | NoSuchFileException e) {
                status = 404;
            } catch (IOException e){
                status = 400;
            }
        }

        private static void doMove(String filename, InputStream body){
            try {
                Files.move(Paths.get(dir + filename), Paths.get(getParams(body, new String[]{"newPath"})[0] + filename));
                status = 200;
            }catch (FileNotFoundException | NoSuchFileException e){
                status = 404;
            }catch (IOException e) {
                status = 400;
            }
        }

        private static void doCopy(String filename, InputStream body){
            try {
                String[] params = getParams(body, new String[]{"newPath", "newFilename"});
                Files.copy(Paths.get(dir + filename), Paths.get(params[0] + params[1]), StandardCopyOption.REPLACE_EXISTING);
                status = 200;
            }catch (FileNotFoundException | NoSuchFileException e){
                status = 404;
            }catch (IOException e) {
                status = 400;
            }
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
