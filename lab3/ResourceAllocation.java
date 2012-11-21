package lab3;


import netsim.protocol.*;
import java.util.Hashtable;
import java.util.PriorityQueue;

public class ResourceAllocation extends netsim.protocol.ProtocolAdapter
{
  private int clock;
  private Hashtable<String,Integer> table = new Hashtable<String,Integer>();
  private PriorityQueue<MyMessage> queue = new PriorityQueue<MyMessage>();
  private boolean hasResource = false;

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
    else if( msg.type == Type.RELEASE )
    {
      recvRelease(msg);
    }

    hasResource = checkResource();
    if ( hasResource ) myNode.setWaken();

  }

  public void trigg() throws Exception
  {
    if( hasResource )
    {
      sendRelease();
      myNode.setIdle();
      //hasResource = false;
      hasResource = checkResource();
      if ( hasResource ) myNode.setWaken();
    }
    else
    {
      sendRequest();
    }
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
      if(this.time == req.time) 
      {
        return this.sender.compareTo(req.sender);
      }
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
    myNode.writeLogg("SendRequest");
    clock++;
    MyMessage msg = new MyMessage(myNodeName, clock, Type.REQUEST);
    myNode.sendToAllOutlinks(msg);
    queue.add(msg);
  }

  private void recvRequest(MyMessage msg) throws NetworkBroken, NotFound
  {
    clock = Math.max(clock,msg.time);
    queue.add( msg );
    table.put( msg.sender, msg.time );
    clock++;
    sendAck(msg.sender);
  }

  private void recvAck(MyMessage msg) 
  {
    myNode.writeLogg("First req:" + queue.peek().time + " Sender: " + queue.peek().sender);
    clock = Math.max(clock,msg.time);
    table.put( msg.sender, msg.time );
    clock++;
  }

  private void sendAck(String sender) throws NetworkBroken, NotFound
  {
    myNode.writeLogg("SendAck");
    myNode.sendTo(sender,new MyMessage(myNodeName, clock, Type.ACK ));
    clock++;
  }

  private void sendRelease() throws NetworkBroken
  {
    myNode.writeLogg("SendRelease");
    clock++;
    MyMessage msg = new MyMessage(myNodeName, clock, Type.RELEASE);
    myNode.sendToAllOutlinks(msg);
    queue.poll();
  }

  private void recvRelease(MyMessage msg) throws NetworkBroken, NotFound
  {
    clock = Math.max(clock,msg.time);
    queue.poll();
    table.put( msg.sender, msg.time );
    clock++;
  }

  private boolean checkResource()
  {
    try
    {
      if( myNodeName.equals(queue.peek().sender) &&
          myNode.getOutLinks().length == table.size() )
      {
        for (int i : table.values()) 
        {
          myNode.writeLogg("i:" + i + " Our req:" + queue.peek().time );
          if (queue.peek().time >= i) 
          {
            return false;
          }
        }
        return true;
      }
      return false;
    }
    catch(Exception e)
    {
      return false;
    }
  }
  
}

