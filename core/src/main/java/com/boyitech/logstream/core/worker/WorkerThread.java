package com.boyitech.logstream.core.worker;

public class WorkerThread extends Thread {

	private boolean run = true;

    public WorkerThread() {
        super();
    }

    public WorkerThread(Runnable target) {
        super(target);
    }

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

}
