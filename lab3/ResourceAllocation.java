package lab3;

import java.util.Arrays;
import netsim.protocol.*;
import java.util.Hashtable;
import java.util.PriorityQueue;

public class ResourceAllocation extends netsim.protocol.ProtocolAdapter
{
  private int clock;
  private Hashtable<String,Integer> table = new Hashtable<String,Integer>();
  private PriorityQueue<MyMessage> queue = new PriorityQueue<MyMessage>();
  private boolean hasResource = false;
  VisibleString[] visibleQueue = new VisibleString[3];
  VisibleInteger visibleClock;

  public void initiate(NodeInterface myNode)
  {
    super.initiate(myNode);
    visibleClock = myNode.createVisibleInteger("Clock",clock);
    visibleClock.setEditable(true);
    for ( int c = 0; c < visibleQueue.length ; ++c )
    {
      visibleQueue[c] = myNode.createVisibleString("Element" + (c+1),"");
    }
  }

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
    if ( hasResource ) 
    {
      myNode.setActive();
    }

  }

  public void trigg() throws Exception
  {
    if( hasResource )
    {
      sendRelease();
      myNode.setIdle();
      hasResource = checkResource();
      if ( hasResource )
      {
        myNode.setActive();
      }
    }
    else
    {
      sendRequest();
      myNode.setWaken();
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
      String str = "";
      if( type == Type.REQUEST )
      {
        str = "Request,";
      }
      else if( type == Type.ACK )
      {
        str = "Ack,";
      }
      else if( type == Type.RELEASE )
      {
        str = "Release,";
      }

      return str + sender + "," + time ;
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
    myNode.writeLogg("Send Request");
    tickClock();
    MyMessage msg = new MyMessage(myNodeName, clock, Type.REQUEST);
    myNode.sendToAllOutlinks(msg);
    queue.add(msg);
    updateQueue();
  }

  private void recvRequest(MyMessage msg) throws NetworkBroken, NotFound
  {
    myNode.writeLogg("Receive Request");
    clock = Math.max(clock,msg.time);
    queue.add( msg );
    updateQueue();
    table.put( msg.sender, msg.time );
    tickClock();
    sendAck(msg.sender);
  }

  private void recvAck(MyMessage msg) 
  {
    myNode.writeLogg("Receive Ack");
    clock = Math.max(clock,msg.time);
    table.put( msg.sender, msg.time );
    tickClock();
  }

  private void sendAck(String sender) throws NetworkBroken, NotFound
  {
    myNode.writeLogg("Send Ack");
    myNode.sendTo(sender,new MyMessage(myNodeName, clock, Type.ACK ));
    tickClock();
  }

  private void sendRelease() throws NetworkBroken
  {
    myNode.writeLogg("Send Release");
    tickClock();
    MyMessage msg = new MyMessage(myNodeName, clock, Type.RELEASE);
    myNode.sendToAllOutlinks(msg);
    queue.poll();
    updateQueue();
  }

  private void recvRelease(MyMessage msg) throws NetworkBroken, NotFound
  {
    myNode.writeLogg("Receive Release");
    clock = Math.max(clock,msg.time);
    queue.poll();
    updateQueue();
    table.put( msg.sender, msg.time );
    tickClock();
  }

  private boolean checkResource()
  {
    if( queue.peek() != null &&
        myNodeName.equals(queue.peek().sender) &&
        myNode.getOutLinks().length == table.size() )
    {
      for (int i : table.values()) 
      {
        if (queue.peek().time >= i) 
        {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  private void tickClock()
  {
    clock++;
    visibleClock.setValue(clock);
  }


  private void updateQueue()
  {
    for( VisibleString v : visibleQueue ) 
    {
      v.setValue("");
    }

    MyMessage[] reqArray = queue.toArray( new MyMessage[0] );
    Arrays.sort(reqArray);
    for (int i = 0; i < visibleQueue.length; ++i ) 
    {
      if( reqArray.length > i )
      {
        String str = reqArray[i].sender + ":" + reqArray[i].time;
        visibleQueue[i].setValue(str);
      }
    }
  }
}

