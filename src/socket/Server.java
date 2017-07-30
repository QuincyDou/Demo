package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * TCP协议通信服务端
 * @author Administrator
 *
 */
public class Server
{
    private ServerSocket server;

    private List<PrintWriter> out;

    public Server()
    {
        try
        {
            out = new ArrayList<PrintWriter>();
            server = new ServerSocket(8088);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        try
        {
            while(true)
            {
                System.out.println("等待客户端连接...");
                Socket socket = server.accept();
                System.out.println("客户端已连接");
                ClientHandler handler = new ClientHandler(socket);
                Thread t = new Thread(handler);
                t.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addOut(PrintWriter write)
    {
        out.add(write);
    }

    private void removeOut(PrintWriter write)
    {
        out.remove(write);
    }

    private void sendMessageToAllClient(String message)
    {
        for (PrintWriter write : out)
        {
            write.println(message);
        }
    }

    public static void main(String[] args)
    {
        Server server = new Server();
        server.start();
    }

    private class ClientHandler implements Runnable
    {
        private Socket socket;

        private String host;
        
        public ClientHandler(Socket socket)
        {
            this.socket = socket;
            InetAddress adress = socket.getInetAddress();
            host = adress.getHostAddress();
        }

        public void run()
        {
            PrintWriter write = null;
            try
            {
                OutputStream out = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out);
                write = new PrintWriter(osw, true);

                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in, "UTF-8");
                BufferedReader br = new BufferedReader(isr);

                String message = null;
                while((message = br.readLine()) != null)
                {
                    System.out.println("[" + host + "]" + message);
                    sendMessageToAllClient(message);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                removeOut(write);
                try
                {
                    socket.close();
                    System.out.println("["+ host +"]下线了");
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }
            }
        }
    }
}
