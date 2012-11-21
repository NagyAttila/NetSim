package lab3;


import netsim.protocol.*;
import java.util.Hashtable;
import java.util.PriorityQueue;

public class ResourceAllocation extends netsim.protocol.ProtocolAdapter
{
  private int clock;
  private Hashtable timestamps = new Hashtable();
  private PriorityQueue<Request> requests = new PriorityQueue<Request>();

  public String toString()
  {
    return "ResourceAllocation";
  }

  protected void receiveMessage(Message message, InLink inLink) throws Exception
  {
    MyMessage msg = (MyMessage)message;
    myNode.setWaken();
    requests.add( msg.request );
    myNode.writeLogg("HEAD:" + requests.peek().time);
  }

  public void trigg() throws Exception
  {
    myNode.sendToAllOutlinks(new MyMessage(myNodeName, new Request(clock,myNodeName)));
    clock++;
  }

  private class MyMessage implements Message
  {
    String sender;
    Request request;
    private MyMessage(String sender, Request request)
    {
      this.sender = sender;
      this.request = request;
    }

    public Message clone()
    {
      return new MyMessage(sender, request);
    }

    public String getTag()
    {
      return sender;
    }
  }

  private class Request implements Comparable<Request>
  {
    private Request(int time, String sender)
    {
      this.time = time;
      this.sender = sender;
    }

    public int compareTo(Request req)
    {
      return this.time - req.time;
    }
    
    int time;
    String sender;
  }

}


