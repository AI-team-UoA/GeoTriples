package be.ugent.mmlab.rml.tools;

public class CriticalSection {
	int readers;

	public CriticalSection() {
		readers = 0;
	}

	public synchronized void enter_read() throws InterruptedException {
		while (readers == -1)
			wait();
		readers++;
	}

	public synchronized void exit_read() {
		readers--;
		if (readers == 0)
			notify();
	}

	public synchronized void enter_write() throws InterruptedException {
		while (readers != 0)
			wait();
		readers = -1;
	}

	public synchronized void exit_write() {
		readers = 0;
		notifyAll();
	}
}
