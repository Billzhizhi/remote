package Client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Client.Define;

public class CFile extends Thread
{
	private Socket client=null;
	private DataInputStream in =null;
	private DataOutputStream out=null;
	private boolean up_down;
	private String clientFile;
	private String serverFile;
	public CFile(String ip,boolean up_down,String client_file,String server_file) throws IOException 
	{
		this.up_down=up_down;
		clientFile=client_file;
		serverFile=server_file;
		client=new Socket(ip,Define.FilePort);
		System.out.println("CFile connect to server success.");
		in = new DataInputStream(client.getInputStream()); 
		out = new DataOutputStream(client.getOutputStream());
		int rInt=in.readInt();
		if(rInt==Define.OK)
		{
			this.start();
		}
	}
	public void run()
	{
		try
		{
			/***************************
			 * true for download file;
			 * false for upload file;
			 ***************************/
			out.writeUTF(serverFile);
			out.writeBoolean(up_down);
			out.flush();
			if(up_down==Define.Download)
			{
				download(clientFile);
			}
			else 
			{
				upload(clientFile);
			}
			in.close();
			out.close();
			client.close();
			System.out.println("CFile connection end.");
		}
		catch(IOException ex){ex.printStackTrace();}
	}
	private void download(String fileAdress)
	{
		try
		{
			if(in.readInt()==Define.OK)
			{
				String file;
				if(fileAdress.length()>0)file=fileAdress+File.separator;
				else file=fileAdress;
				file+=in.readUTF();
				File dir=new File(fileAdress);
				if(!dir.exists())dir.mkdirs();
				System.out.println(file.toString());
				DataOutputStream fos = new DataOutputStream(new FileOutputStream(file)); 
				long len=in.readLong();
				int bufferSize = Define.BufferSize; 
				byte[] buf = new byte[bufferSize]; 
				long passedlen = 0; 
				System.out.println("Download "+file+"..."); 
				int counter=0;
				int nowInt=0;
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
					nowInt=(int)(passedlen * 100/ len);
					if(nowInt!=counter)
					{
						counter=nowInt;
						System.out.println("get:" +nowInt + "%"); 
					}
					
				}
				fos.close();
			}
		}catch(IOException ex){ex.printStackTrace();}
	}
	
	
	/*******  Upload File *********/
	private void upload(String fileAdress)
	{
		try
		{
			File fi = new File(fileAdress);
			DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(fi))); 
			out.writeInt(Define.OK);
			out.writeUTF(getFileName(fileAdress));
			out.flush(); 
			int bufferSize = Define.BufferSize; 
			byte[] buf = new byte[bufferSize]; 
			System.out.println("Upload "+fi+"..."); 
			long len=fi.length();
			long passedlen = 0; 
			int counter=0;
			int nowInt=0;
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
				passedlen += read; 
				nowInt=(int)(passedlen * 100/ len);
				if(nowInt!=counter)
				{
					counter=nowInt;
					System.out.println("send:" +nowInt + "%"); 
				}
			}
			out.flush(); 
			// ע��ر�socket����Ŷ����Ȼ�ͻ��˻�ȴ�server�����ݹ�����
			// ֱ��socket��ʱ���������ݲ������� 
			fis.close(); 
		}
		catch(IOException ex){ex.printStackTrace();}
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

}
