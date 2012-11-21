package lab3;


import netsim.protocol.*;
import java.util.Hashtable;
import java.util.PriorityQueue;

public class ResourceAllocation extends netsim.protocol.ProtocolAdapter
{
  private int clock;
  private Hashtable table = new Hashtable();
  private PriorityQueue<MyMessage> queue = new PriorityQueue<MyMessage>();

  public String toString()
  {
    return "ResourceAllocation";
  }

  protected void receiveMessage(Message message, InLink inLink) throws Exception
  {
    MyMessage msg = (MyMessage)message;

    if( msg.type == Type.REQUEST )
    {
      recvRequest(msg);
    }
    else if( msg.type == Type.ACK )
    {
      recvAck(msg);
    }
  }

  public void trigg() throws Exception
  {
    sendRequest();
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

  private void sendRequest() throws NetworkBroken
  {
    clock++;
    MyMessage msg = new MyMessage(myNodeName, clock, Type.REQUEST);
    myNode.sendToAllOutlinks(msg);
    queue.add(msg);
    myNode.writeLogg("Size:" + queue.size());
  }

  private void recvRequest(MyMessage msg) throws NetworkBroken, NotFound
  {
    clock = Math.max(clock,msg.time);
    queue.add( msg );
    table.put( msg.sender, msg.time );
    clock++;
    sendAck(msg.sender);
    myNode.writeLogg("HEAD:" + queue.peek().time);
  }

  private void recvAck(MyMessage msg) 
  {
    clock = Math.max(clock,msg.time);
    table.put( msg.sender, msg.time );
    clock++;
    myNode.writeLogg("ACK HEAD:" + queue.peek().time);
  }

  private void sendAck(String sender) throws NetworkBroken, NotFound
  {
    myNode.sendTo(sender,new MyMessage(myNodeName, clock, Type.ACK ));
    clock++;
  }

}

//myNode.setWaken();

