package br.com.vadinei.activemq.consumer.domain.service;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import br.com.vadinei.activemq.background.process.domain.service.BackgrundProcessServiceBean;
import br.com.vadinei.activemq.producer.domain.service.ProducerServiceBean;

public class ConsumerServiceBean implements ConsumerService {

	private static final long serialVersionUID = -5584847638135686772L;

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

	public static class StartConsumer implements Runnable, ExceptionListener {
		@Override
		public void run() {
			try {
				// Cria uma ConnectionFactory
				final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
						ProducerServiceBean.getUri(ProducerServiceBean.CNS_DEFAULT_PORT));

				// Cria e inicializando uma Connection
				final Connection connection = connectionFactory.createConnection();
				connection.start();
				connection.setExceptionListener(this);

				// Cria uma Session
				final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				// Cria um Destination (Topic or Queue), no caso uma Queue
				final Destination destination = session.createQueue(ProducerServiceBean.CNS_QUEUEE_NAME);

				// Cria uma MessageConsumer ao Destination acima (Queue)
				final MessageConsumer messageConsumer = session.createConsumer(destination);

				// Aguarda por uma Message
				final Message message = messageConsumer.receive(1000);
				onMessage(message);

				// Fecha a NessageConsumer, a Session e a Connection
				messageConsumer.close();
				session.close();
				connection.close();
			} catch (final Exception e) {
				System.out.println("Caught: " + e);
				e.printStackTrace();
			}
		}

		public void onMessage(final javax.jms.Message jmsMessage) {
			try {
				if (jmsMessage instanceof TextMessage) {
					final TextMessage textMessage = (TextMessage) jmsMessage;
					final String text = textMessage.getText();

					System.out.println("Recebendo: -" + text);
					if ((text != null) && text.contains("address")) {
						System.out.println(">>> Enviando...");
						final String hostSource = text.substring(8);
						final BackgrundProcessServiceBean backgroundProcessService = new BackgrundProcessServiceBean(
								hostSource);
						backgroundProcessService.StartBackgrundProcess();
					}
				}
			} catch (final javax.jms.JMSException ex) {
				ex.printStackTrace();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public synchronized void onException(final JMSException ex) {
			System.out.println("Ocorreu uma JMS Exception. Finalizando o Consumidor.");
		}
	}

	public static void thread(final Runnable runnable, final boolean daemon) {
		final Thread brokerThread = new Thread(runnable);
		brokerThread.setDaemon(daemon);
		brokerThread.start();
	}

	public static void main(final String[] args) throws Exception {
		ConsumerServiceBean.thread(new StartBroker(), true);
		while (true) {
			ConsumerServiceBean.thread(new StartConsumer(), false);
			Thread.sleep(5000);
		}
	}

}
