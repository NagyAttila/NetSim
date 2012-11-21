package lab3;


import netsim.protocol.*;
import java.util.Hashtable;
import java.util.PriorityQueue;

public class ResourceAllocation extends netsim.protocol.ProtocolAdapter
{
  private int clock;
  private Hashtable timestamps = new Hashtable();
  private PriorityQueue<MyMessage> requests = new PriorityQueue<MyMessage>();

  public String toString()
  {
    return "ResourceAllocation";
  }

  protected void receiveMessage(Message message, InLink inLink) throws Exception
  {
    MyMessage msg = (MyMessage)message;

    if( msg.type == Type.REQUEST )
    {
      myNode.setWaken();
      requests.add( msg );
      myNode.writeLogg("HEAD:" + requests.peek().time);
    }
  }

  public void trigg() throws Exception
  {
    myNode.sendToAllOutlinks(new MyMessage(myNodeName, clock, Type.REQUEST));
    clock++;
  }

  private class MyMessage implements Message, Comparable<MyMessage>
  {
    String sender;
    int time;
    Type type;
    private MyMessage(String sender, int time, Type type)
    {
      this.sender = sender;
      this.time = time;
      this.type = type;
    }

    public Message clone()
    {
      return new MyMessage(sender, time, type);
    }

    public String getTag()
    {
      return sender;
    }

    public int compareTo(MyMessage req)
    {
      return this.time - req.time;
    }
  }


  public enum Type 
  {
    REQUEST,
      ACK,
      RELEASE
  }
}


