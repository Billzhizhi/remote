package Server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SFile extends Thread {
	private Socket client=null;
	ServerSocket server=null;
	private DataInputStream in =null;
	private DataOutputStream out=null;
	public SFile()throws Exception
	{
		server=new ServerSocket(Define.FilePort);
		client=server.accept();
		in = new DataInputStream(client.getInputStream()); 
		out = new DataOutputStream(client.getOutputStream());
		out.writeInt(Define.OK);
		out.flush();
		this.start();
	}
	public void run()
	{
		try
		{
			String fileName=in.readUTF();
			if(in.readBoolean()==Define.Download)
			{
				/*client want to download file*/
				download(fileName);
			}
			else 
			{
				/*client want to upload file*/
				upload(fileName);
			}
			in.close();
			out.close();
			client.close();
			server.close();
		}
		catch(IOException ex)
		{
			//ex.printStackTrace();
		}
	}

	private void download(String fileAdress)
	{
		try
		{
			File fi = new File(fileAdress);
			DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(fi))); 
			out.writeInt(Define.OK);
			out.writeUTF(getFileName(fileAdress));
			out.writeLong((long) fi.length()); 
			out.flush(); 
			int bufferSize = Define.BufferSize; 
			byte[] buf = new byte[bufferSize]; 
			while (true) 
			{
				int read = 0; 
				if (fis != null) 
				{
					read = fis.read(buf); 
				}
				if (read == -1) 
				{
					break; 
				}
				out.write(buf, 0, read); 
			}
			out.flush(); 
			// ע��ر�socket����Ŷ����Ȼ�ͻ��˻�ȴ�server�����ݹ�����
			// ֱ��socket��ʱ���������ݲ������� 
			fis.close(); 
		}
		catch(IOException ex)
		{
			//ex.printStackTrace();
		}
	}
	private String getFileName(String str)
	{
		int intt=str.lastIndexOf(Define.FileDevide);
		if(intt>=0)
		{
			return str.substring(++intt);
		}
		return "";
	}
	
	/*********************************************
	 * void upload(String fileAdress)
	 * fileAdress:adress to save the upload file;
	 * function:receive file which client send in;
	 ********************************************/
	protected void upload(String fileAdress)
	{
		try
		{
			if(in.readInt()==Define.OK)
			{
				String file;
				if(fileAdress.length()>0)file=fileAdress+Define.FileDevide;
				else file=fileAdress;
				file+=in.readUTF();
				File dir=new File(fileAdress);
				if(!dir.exists())dir.mkdirs();
				DataOutputStream fos = new DataOutputStream(new FileOutputStream(file)); 
				int bufferSize = Define.BufferSize; 
				byte[] buf = new byte[bufferSize]; 
				long passedlen = 0; 
				while (true) 
				{
					int read = 0; 
					if (in != null) 
					{
						read = in.read(buf); 
					}
					passedlen += read; 
					if (read == -1) 
					{
						break; 
					}
					//�����������Ϊͼ�ν����prograssBar���ģ���������Ǵ��ļ���
					//���ܻ��ظ���ӡ��һЩ��ͬ�İٷֱ�

					fos.write(buf, 0, read); 
				}
				fos.close();
			}
		}catch(IOException ex){ex.printStackTrace();}
	}
}
