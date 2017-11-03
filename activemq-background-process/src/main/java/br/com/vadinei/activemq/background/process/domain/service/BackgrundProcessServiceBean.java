package br.com.vadinei.activemq.background.process.domain.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.Format;
import java.util.Date;

import br.com.vadinei.activemq.producer.domain.service.ProducerServiceBean;

public class BackgrundProcessServiceBean implements BackgroundProcessService {

	private static final long serialVersionUID = 1033017783064740525L;

	private static String host = null;
	private static String stime;

	private static final Format formatter = ProducerServiceBean.CNS_TIME_FORMATTER;

	public BackgrundProcessServiceBean(final String hostSource) {
		BackgrundProcessServiceBean.host = hostSource;
	}

	public void StartBackgrundProcess() {
		final Runnable tr = new BackgrundProcessServiceThread();
		int i = 0;
		while (i < 6) {
			final Thread thread = new Thread(tr);
			thread.start();
			try {
				Thread.sleep(3000);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
	}

	public static class BackgrundProcessServiceThread implements Runnable {
		public BackgrundProcessServiceThread() {
			final Date date = new Date();
			BackgrundProcessServiceBean.stime = BackgrundProcessServiceBean.formatter.format(date);
			System.out.println("Objeto instanciado [" + BackgrundProcessServiceBean.stime + "]");
		}

		@Override
		public void run() {
			final Date date = new Date();
			BackgrundProcessServiceBean.stime = BackgrundProcessServiceBean.formatter.format(date);
			System.out.println("Host testado: " + BackgrundProcessServiceBean.host + " ["
					+ BackgrundProcessServiceBean.stime + "]");

			try {
				final InetAddress address = InetAddress.getByName(BackgrundProcessServiceBean.host);
				address.isReachable(2000);
			} catch (final UnknownHostException uhe) {
				uhe.printStackTrace();
			} catch (final IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}
