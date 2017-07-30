package socket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * TCP协议通信客户端
 * @author Administrator
 *
 */
public class Client
{
    private Socket socket;

    public Client()
    {
        try
        {
            socket = new Socket("127.0.0.1", 8088);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        try
        {
            GetServerMessageHandler handler = new GetServerMessageHandler();
            Thread t = new Thread(handler);
            t.start();
            
            OutputStream out = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(out,"UTF-8");
            PrintWriter write = new PrintWriter(osw,true);
            Scanner scan = new Scanner(System.in);
            while(true)
            {
                write.println(scan.nextLine());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        Client client = new Client();
        client.start();
    }

    private class GetServerMessageHandler implements Runnable
    {
        public void run()
        {
            try
            {
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);
                String message = null;
                while((message = br.readLine()) != null)
                {
                    System.out.println("服务端说：" + message);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
