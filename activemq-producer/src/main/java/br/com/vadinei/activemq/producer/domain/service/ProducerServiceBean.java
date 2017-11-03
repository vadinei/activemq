package br.com.vadinei.activemq.producer.domain.service;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ProducerServiceBean implements ProducerService {

	public static final String CNS_HOST = "127.0.0.1";
	public static final String CNS_DEFAULT_PORT = "61616";
	public static final String CNS_OPCIONAL_PORT = "61617";
	public static final String CNS_QUEUEE_NAME = "SERVER.TEST";
	public static final String CNS_TEXT_MESSAGE = "address:127.0.0.1";
	public static final Format CNS_TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

	private static String stime = null;

	private static final long serialVersionUID = 7971916024871611544L;

	public static class StartBroker implements Runnable {

		public StartBroker() {

		}

		@Override
		public void run() {
			try {
				// Configura o broker
				// final BrokerService broker = new BrokerService();
				// broker.addConnector(ProducerServiceBean.getUri(ProducerServiceBean.CNS_DEFAULT_PORT));
				// broker.start();
			} catch (final Exception e) {
				System.out.println("Caught: " + e);
				e.printStackTrace();
			}
		}

	}

	public static class StartProducer implements Runnable {

		@Override
		public void run() {
			try {
				// Cria uma ConnectionFactory
				final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
						ProducerServiceBean.getUri(ProducerServiceBean.CNS_DEFAULT_PORT));

				// Cria e inicializando uma Connection
				final Connection connection = connectionFactory.createConnection();
				connection.start();

				// Cria uma Session
				final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				// Cria um Destination (Topic or Queue), no caso uma Queue
				final Destination destination = session.createQueue(ProducerServiceBean.CNS_QUEUEE_NAME);

				// Cria uma MessageProducer ao Destination acima (Queue)
				final MessageProducer messageProducer = session.createProducer(destination);
				messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

				// Cria uma mensagem a ser enviada ao MessageProducer
				final String text = ProducerServiceBean.CNS_TEXT_MESSAGE;
				final TextMessage message = session.createTextMessage(text);
				final Date now = new Date();

				ProducerServiceBean.stime = ProducerServiceBean.CNS_TIME_FORMATTER.format(now);

				// Exibe o Status e encaminha a TextMessage ao MessageProducer
				final StringBuilder sbStatus = new StringBuilder();

				sbStatus.append("Mensagem enviada: ");
				sbStatus.append(text);
				sbStatus.append("[");
				sbStatus.append(ProducerServiceBean.stime);
				sbStatus.append("]");

				System.out.println(sbStatus.toString());
				messageProducer.send(message);

				// Fecha a Session e a Connection
				session.close();
				connection.close();
			} catch (final Exception e) {
				System.out.println("Caught: " + e);
				e.printStackTrace();
			}
		}

	}

	public static String getUri(final String port) {
		final StringBuilder sbUri = new StringBuilder();

		sbUri.append("tcp://");
		sbUri.append(ProducerServiceBean.CNS_HOST);
		sbUri.append(":");
		sbUri.append(port);
		sbUri.append("");

		return sbUri.toString();
	}

	public static void thread(final Runnable runnable, final boolean daemon) {
		final Thread brokerThread = new Thread(runnable);
		brokerThread.setDaemon(daemon);
		brokerThread.start();
	}

	public static void main(final String[] args) throws Exception {
		// ProducerServiceBean.thread(new StartBroker(), true);
		ProducerServiceBean.thread(new StartProducer(), false);
	}

}
