package requester;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Tiago on 01/10/2015.
 * Trabalho: Interesacao de Sistemas
 */
public class MainRequester implements MessageListener{

    private ConnectionFactory cf;
    private Destination d;
    private Scanner reader;

    public MainRequester() throws NamingException {

        this.cf = InitialContext.doLookup("jms/RemoteConnectionFactory");
        this.d = InitialContext.doLookup("jms/queue/PlayQueue");
        reader = new Scanner(System.in);
    }


    @SuppressWarnings("all")
    public void launch_and_wait() {

        System.out.println("Escreva o que quer pesquisar: (para sair escreva 'q')");
        String lido = reader.nextLine();
        do{
            if(!lido.equals("q")) {
                waitResponse(lido);
                lido = reader.nextLine();
            }
        }while(!lido.equals("q"));

        System.out.println("Bye bye.");

    }

    @SuppressWarnings("all")
    public void waitResponse(String texto){
        try (JMSContext jcontext = cf.createContext("tiago", "12")) {
            Connection connection = cf.createConnection("tiago", "12");
            connection.start();
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            TemporaryQueue tq = session.createTemporaryQueue();
            TextMessage message = session.createTextMessage();
            message.setText(texto);
            message.setJMSReplyTo(tq);
            JMSProducer mp = jcontext.createProducer();
            mp.send(d, message);

            MessageConsumer consumer = session.createConsumer(tq);

            consumer.setMessageListener(this);
            System.in.read();

            consumer.close();
            tq.delete();
            connection.stop();
            connection.close();
        } catch (JMSRuntimeException | JMSException | IOException re) {
            re.printStackTrace();
        }

    }

    @Override
    public void onMessage(Message message) {
        //TextMessage tmsg = (TextMessage) message;
        try {
            System.out.println("Messagem: " + ((TextMessage) message).getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args)throws NamingException{

        System.out.print("Sou um requester\n");
        MainRequester r = new MainRequester();
        r.launch_and_wait();
    }

}
