package nju.cs.timingservice;



import nju.cs.SocketUtil;

import nju.cs.timingservice.message.*;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Time polling service provider.
 * @author hengxin
 * @date Jul 18, 2014
 */
public enum TimingService
{
	INSTANCE;
	
	/**
	 * Socket connecting Android device and PC host
	 */
	private Socket host_socket = null;
	public ObjectInputStream inputStream = null;
	public ObjectOutputStream outputStream = null;

	
	public void setHostSocket(final Socket host_socket)
	{
		this.host_socket = host_socket;
	}

	public Socket getHostSocket()
	{
		return host_socket;
	}
	
	/**
	 * Receive {@link AuthMsg} from PC; Enable the time-polling functionality.
	 */
	public void receiveAuthMsg() throws IOException
	{
		System.out.println(SocketUtil.INSTANCE.receiveMsg(host_socket));

	}
	
	/**
	 * Wait for and receive {@link ResponseTimeMsg} from PC.
	 *
	 * @return {@link ResponseTimeMsg} from PC
	 */
	public ResponseTimeMsg receiveResponseTimeMsgInNewThread() throws Throwable
	{
		Message msg = SocketUtil.INSTANCE.receiveMsgInNewThread(host_socket, inputStream);
		assert msg.getType() == Message.RESPONSE_TIME_MSG;
		return (ResponseTimeMsg) msg;
	}
	
	/**
	 * Poll system time of PC
	 * @return system time of PC
	 */
	public long pollingTime() throws Throwable
	{
		/**
		 * Send {@link RequestTimeMsg} to PC in a new thread.
		 * You cannot use network connection on the Main UI thread.
		 * Otherwise you will get {@link NetworkOnMainThreadException}
		 */
//		SocketUtil.INSTANCE.sendMsgInNewThread(new RequestTimeMsg(), host_socket, outputStream);

		PrintWriter writer = new PrintWriter(host_socket.getOutputStream(), true);
		writer.println("current time");
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(host_socket.getInputStream()));
		String time = bufferedReader.readLine();
		System.out.println("polling time: " + time);
		return Long.parseLong(time);

		
//		ResponseTimeMsg responseTimeMsg = this.receiveResponseTimeMsgInNewThread();
//		return responseTimeMsg.getHostPCTime();
	}
}
