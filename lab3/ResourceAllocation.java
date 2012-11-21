package lab3;

import netsim.protocol.*;

public class ResourceAllocation extends netsim.protocol.ProtocolAdapter
{
  public String toString()
  {
    return "ResourceAllocation";
  }

  protected void receiveMessage(Message message, InLink inLink) throws Exception
  {
    myNode.setWaken();
  }

  public void trigg() throws Exception
  {
    myNode.sendToAllOutlinks(new MyMessage(myNodeName, "Pa aterseende!"));
  }

  private class MyMessage implements Message
  {
    String sender;
    String message;
    private MyMessage(String sender, String message)
    {
      this.sender = sender;
      this.message = message;
    }

    public Message clone()
    {
      return new MyMessage(sender, message);
    }

    public String getTag()
    {
      return sender;
    }
  }
}
