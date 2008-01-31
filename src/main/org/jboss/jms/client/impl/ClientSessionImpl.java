/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package org.jboss.jms.client.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.TransactionInProgressException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.jms.client.SelectorTranslator;
import org.jboss.jms.client.api.ClientBrowser;
import org.jboss.jms.client.api.ClientConnection;
import org.jboss.jms.client.api.ClientConsumer;
import org.jboss.jms.client.api.ClientProducer;
import org.jboss.jms.client.api.ClientSession;
import org.jboss.jms.client.remoting.MessagingRemotingConnection;
import org.jboss.jms.destination.JBossDestination;
import org.jboss.jms.destination.JBossQueue;
import org.jboss.jms.destination.JBossTopic;
import org.jboss.messaging.core.Destination;
import org.jboss.messaging.core.Message;
import org.jboss.messaging.core.remoting.PacketDispatcher;
import org.jboss.messaging.core.remoting.wireformat.AbstractPacket;
import org.jboss.messaging.core.remoting.wireformat.AddTemporaryDestinationMessage;
import org.jboss.messaging.core.remoting.wireformat.CloseMessage;
import org.jboss.messaging.core.remoting.wireformat.ClosingMessage;
import org.jboss.messaging.core.remoting.wireformat.CreateBrowserRequest;
import org.jboss.messaging.core.remoting.wireformat.CreateBrowserResponse;
import org.jboss.messaging.core.remoting.wireformat.CreateConsumerRequest;
import org.jboss.messaging.core.remoting.wireformat.CreateConsumerResponse;
import org.jboss.messaging.core.remoting.wireformat.CreateDestinationRequest;
import org.jboss.messaging.core.remoting.wireformat.CreateDestinationResponse;
import org.jboss.messaging.core.remoting.wireformat.DeleteTemporaryDestinationMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionAcknowledgeMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionCancelMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionCommitMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionRollbackMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionSendMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXACommitMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXAEndMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXAForgetMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXAGetInDoubtXidsRequest;
import org.jboss.messaging.core.remoting.wireformat.SessionXAGetInDoubtXidsResponse;
import org.jboss.messaging.core.remoting.wireformat.SessionXAGetTimeoutMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXAGetTimeoutResponse;
import org.jboss.messaging.core.remoting.wireformat.SessionXAJoinMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXAPrepareMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXAResponse;
import org.jboss.messaging.core.remoting.wireformat.SessionXAResumeMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXARollbackMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXASetTimeoutMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXASetTimeoutResponse;
import org.jboss.messaging.core.remoting.wireformat.SessionXAStartMessage;
import org.jboss.messaging.core.remoting.wireformat.SessionXASuspendMessage;
import org.jboss.messaging.core.remoting.wireformat.UnsubscribeMessage;
import org.jboss.messaging.util.ClearableQueuedExecutor;
import org.jboss.messaging.util.Logger;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;

/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 *
 * @version <tt>$Revision: 3603 $</tt>
 *
 * $Id: ClientSessionImpl.java 3603 2008-01-21 18:49:20Z timfox $
 */
public class ClientSessionImpl implements ClientSession
{
   // Constants ------------------------------------------------------------------------------------

   private static final Logger log = Logger.getLogger(ClientSessionImpl.class);

   private boolean trace = log.isTraceEnabled();

   // Attributes -----------------------------------------------------------------------------------

   private String id;
   
   private boolean xa;

   private int lazyAckBatchSize;
   
   private volatile boolean closed;
      
   private boolean acked = true;
   
   private boolean broken;
   
   private long toAckCount;
   
   private long lastID = -1;
   
   private long deliverID;      
   
   private boolean deliveryExpired;   

   // Executor used for executing onMessage methods
   private ClearableQueuedExecutor executor;

   private MessagingRemotingConnection remotingConnection;
         
   private ClientConnection connection;
   
   private Set<ClientBrowser> browsers = new HashSet<ClientBrowser>();
   
   private Set<ClientProducer> producers = new HashSet<ClientProducer>();
   
   private Map<String, ClientConsumer> consumers = new HashMap<String, ClientConsumer>();
   
   //For testing only
   private boolean setForceNotSameRM;
      
   // Constructors ---------------------------------------------------------------------------------
   
   public ClientSessionImpl(ClientConnection connection, String id,
                            int lazyAckBatchSize, boolean xa)
   {
      this.id = id;
      
      this.connection = connection;
      
      this.remotingConnection = connection.getRemotingConnection();
      
      this.xa = xa;
 
      executor = new ClearableQueuedExecutor(new LinkedQueue());
      
      this.lazyAckBatchSize = lazyAckBatchSize;         
   }
   
   // ClientSession implementation ----------------------------------------------------
   
   public String getID()
   {
      return id;
   }

   public synchronized void close() throws JMSException
   {
      if (closed)
      {
         return;
      }

      try
      {
         remotingConnection.send(id, new CloseMessage());
   
         executor.shutdownNow();
      }
      finally
      {
         connection.removeChild(id);
         
         closed = true;
      }
   }
  
   public void closing() throws JMSException
   {
      if (closed)
      {
         return;
      }
      
      closeChildren();
      
      //Make sure any remaining acks make it to the server
      
      acknowledgeInternal(false);      
                 
      ClosingMessage request = new ClosingMessage();
      
      remotingConnection.send(id, request);
   }

   public ClientConnection getConnection()
   {
      return connection;
   }

   public void addTemporaryDestination(Destination destination) throws JMSException
   {
      checkClosed();
      
      remotingConnection.send(id, new AddTemporaryDestinationMessage(destination), false);
   }

   public void commit() throws JMSException
   {
      checkClosed();
        
      if (isXA())
      {
         throw new TransactionInProgressException("Cannot call commit on an XA session");
      }

      //Before committing we must make sure the acks make it to the server
      //instead of this we could possibly add the lastDeliveryID in the SessionCommitMessage
      acknowledgeInternal(false);
      
      remotingConnection.send(id, new SessionCommitMessage());
   }
   
   public void rollback() throws JMSException
   {
      checkClosed();
            
      if (isXA())
      {
         throw new TransactionInProgressException("Cannot call rollback on an XA session");
      }
      
      //First we tell each consumer to clear it's buffers and ignore any deliveries with
      //delivery id > last delivery id
      
      for (ClientConsumer consumer: consumers.values())
      {
         consumer.recover(lastID + 1);
      }
      
      //Before rolling back we must make sure the acks make it to the server
      //instead of this we could possibly add the lastDeliveryID in the SessionRollbackMessage
      acknowledgeInternal(false);      

      remotingConnection.send(id, new SessionRollbackMessage());
   }

   public ClientBrowser createClientBrowser(Destination queue, String messageSelector)
      throws JMSException
   {
      checkClosed();
      
      String coreSelector = SelectorTranslator.convertToJBMFilterString(messageSelector);
      
      CreateBrowserRequest request = new CreateBrowserRequest(queue, coreSelector);
      
      CreateBrowserResponse response = (CreateBrowserResponse)remotingConnection.send(id, request);
      
      ClientBrowser browser = new ClientBrowserImpl(remotingConnection, this, response.getBrowserID());  
      
      browsers.add(browser);
      
      return browser;
   }
   
   public ClientConsumer createClientConsumer(Destination destination, String selector,
                                              boolean noLocal, String subscriptionName) throws JMSException
   {
      checkClosed();
      
      String coreSelector = SelectorTranslator.convertToJBMFilterString(selector);
      
      CreateConsumerRequest request =
         new CreateConsumerRequest(destination, coreSelector, noLocal, subscriptionName, false);
      
      CreateConsumerResponse response = (CreateConsumerResponse)remotingConnection.send(id, request);
      
      ClientConsumer consumer =
         new ClientConsumerImpl(this, response.getConsumerID(), response.getBufferSize(),             
                                destination,
                                selector, noLocal,
                                executor, remotingConnection);

      consumers.put(response.getConsumerID(), consumer);

      PacketDispatcher.client.register(new ClientConsumerPacketHandler(consumer, response.getConsumerID()));

      //Now we have finished creating the client consumer, we can tell the SCD
      //we are ready
      consumer.changeRate(1);
      
      return consumer;
   }
   
   public ClientProducer createClientProducer(JBossDestination destination) throws JMSException
   {
      checkClosed();
      
      ClientProducer producer = new ClientProducerImpl(this, destination);
  
      producers.add(producer);
      
      return producer;
   }

   public JBossQueue createQueue(String queueName) throws JMSException
   {
      checkClosed();
      
      CreateDestinationRequest request = new CreateDestinationRequest(queueName, true);  
      
      CreateDestinationResponse response = (CreateDestinationResponse)remotingConnection.send(id, request);
      
      return (JBossQueue) response.getDestination();
   }
   
   public JBossTopic createTopic(String topicName) throws JMSException
   {
      checkClosed();
      
      CreateDestinationRequest request = new CreateDestinationRequest(topicName, false); 
      
      CreateDestinationResponse response = (CreateDestinationResponse)remotingConnection.send(id, request);
      
      return (JBossTopic) response.getDestination();
   }

   public void deleteTemporaryDestination(Destination destination) throws JMSException
   {
      checkClosed();
      
      remotingConnection.send(id, new DeleteTemporaryDestinationMessage(destination));
   }
   
   //Internal method to be called from consumerImpl - should not expose this publicly
   public void delivered(long deliverID, boolean expired)
   {
      this.deliverID = deliverID;
      
      this.deliveryExpired = expired;
   }
   
   //Called after a message has been delivered
   public void delivered() throws JMSException
   {                        
      if (lastID + 1 != deliverID)
      {
         broken = true;
      }
            
      lastID = deliverID;
            
      toAckCount++;
      
      acked = false;
       
      if (deliveryExpired)
      {
         remotingConnection.send(id, new SessionCancelMessage(lastID, true), true);
         
         toAckCount = 0;
      }
      else if (broken)
      {
         //Must always ack now
         acknowledgeInternal(false);
         
         toAckCount = 0;
      }
      else
      {
         if (toAckCount == lazyAckBatchSize)
         {
            acknowledgeInternal(false);
            
            toAckCount = 0;
         }                       
      }            
   }
   
   private void acknowledgeInternal(boolean block) throws JMSException
   {
      if (acked)
      {
         return;
      }
      
      SessionAcknowledgeMessage message = new SessionAcknowledgeMessage(lastID, !broken);
      remotingConnection.send(id, message, !block);
      
      acked = true;
   }
      
   public void unsubscribe(String subscriptionName) throws JMSException
   {
      checkClosed();
      
      remotingConnection.send(id, new UnsubscribeMessage(subscriptionName));
   }

   public XAResource getXAResource()
   {
      return this;
   }

   public void send(Message m) throws JMSException
   {
      checkClosed();
      
      SessionSendMessage message = new SessionSendMessage(m);
      
      remotingConnection.send(id, message, !m.isDurable());
   }
   
   public void removeConsumer(ClientConsumer consumer) throws JMSException
   {
      consumers.remove(consumer.getID());
            
      //1. flush any unacked message to the server
      
      acknowledgeInternal(false);
      
      //2. cancel all deliveries on server but not in tx
            
      remotingConnection.send(id, new SessionCancelMessage(-1, false));      
   }
   
   public void removeProducer(ClientProducer producer)
   {
      producers.remove(producer);
   }
   
   public void removeBrowser(ClientBrowser browser)
   {
      browsers.remove(browser);
   }
     
   public boolean isXA() throws JMSException
   {
      checkClosed();
      
      return xa;
   }
   
//   public boolean isTransacted() throws JMSException
//   {
//      checkClosed();
//      
//      return transacted;
//   }
   
   public boolean isClosed()
   {
      return closed;
   }
   
   public void flushAcks() throws JMSException
   {
      this.acknowledgeInternal(false);
   }
   
   // XAResource implementation --------------------------------------------------------------------
   
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      try
      { 
         SessionXACommitMessage packet = new SessionXACommitMessage(xid, onePhase);
                  
         SessionXAResponse response = (SessionXAResponse)remotingConnection.send(id, packet);
         
         if (response.isError())
         {
            throw new XAException(response.getResponseCode());
         }
      }
      catch (JMSException e)
      {
         log.error("Caught jmsexecptione ", e);
         //This should never occur
         throw new XAException(XAException.XAER_RMERR);
      }
   }

   public void end(Xid xid, int flags) throws XAException
   {
      try
      {
         AbstractPacket packet;
         
         if (flags == XAResource.TMSUSPEND)
         {
            packet = new SessionXASuspendMessage();                  
         }
         else if (flags == XAResource.TMSUCCESS)
         {
            packet = new SessionXAEndMessage(xid, false);
         }
         else if (flags == XAResource.TMFAIL)
         {
            packet = new SessionXAEndMessage(xid, true);
         }
         else
         {
            throw new XAException(XAException.XAER_INVAL);
         }
               
         //Need to flush any acks to server first
         acknowledgeInternal(false);
         
         SessionXAResponse response = (SessionXAResponse)remotingConnection.send(id, packet);
         
         if (response.isError())
         {
            throw new XAException(response.getResponseCode());
         }
      }
      catch (JMSException e)
      {
         log.error("Caught jmsexecptione ", e);
         //This should never occur
         throw new XAException(XAException.XAER_RMERR);
      }
   }

   public void forget(Xid xid) throws XAException
   {
      try
      {                              
         SessionXAResponse response = (SessionXAResponse)remotingConnection.send(id, new SessionXAForgetMessage(xid));
         
         if (response.isError())
         {
            throw new XAException(response.getResponseCode());
         }
      }
      catch (JMSException e)
      {
         //This should never occur
         throw new XAException(XAException.XAER_RMERR);
      }
   }

   public int getTransactionTimeout() throws XAException
   {
      try
      {                              
         SessionXAGetTimeoutResponse response =
            (SessionXAGetTimeoutResponse)remotingConnection.send(id, new SessionXAGetTimeoutMessage());
         
         return response.getTimeoutSeconds();
      }
      catch (JMSException e)
      {
         //This should never occur
         throw new XAException(XAException.XAER_RMERR);
      }
   }

   public boolean isSameRM(XAResource xares) throws XAException
   {
      if (!(xares instanceof ClientSessionImpl))
      {
         return false;
      }
      
      if (forceNotSameRM)
      {
         return false;
      }
      
      ClientSessionImpl other = (ClientSessionImpl)xares;
      
      return this.connection.getServerID() == other.getConnection().getServerID();
   }

   public int prepare(Xid xid) throws XAException
   {
      try
      {
         SessionXAPrepareMessage packet = new SessionXAPrepareMessage(xid);
         
         SessionXAResponse response = (SessionXAResponse)remotingConnection.send(id, packet);
         
         if (response.isError())
         {
            throw new XAException(response.getResponseCode());
         }
         else
         {
            return response.getResponseCode();
         }
      }
      catch (JMSException e)
      {
         log.error("Caught jmsexecptione ", e);
         //This should never occur
         throw new XAException(XAException.XAER_RMERR);
      }
   }

   public Xid[] recover(int flag) throws XAException
   {
      try
      {
         SessionXAGetInDoubtXidsRequest packet = new SessionXAGetInDoubtXidsRequest();
         
         SessionXAGetInDoubtXidsResponse response = (SessionXAGetInDoubtXidsResponse)remotingConnection.send(id, packet);
         
         List<Xid> xids = response.getXids();
         
         Xid[] xidArray = xids.toArray(new Xid[xids.size()]);
         
         return xidArray;
      }
      catch (JMSException e)
      {
         //This should never occur
         throw new XAException(XAException.XAER_RMERR);
      }
   }

   public void rollback(Xid xid) throws XAException
   {
      try
      {

         SessionXARollbackMessage packet = new SessionXARollbackMessage(xid);
         
         SessionXAResponse response = (SessionXAResponse)remotingConnection.send(id, packet);
         
         if (response.isError())
         {
            throw new XAException(response.getResponseCode());
         }
      }
      catch (JMSException e)
      {
         log.error("Caught jmsexecptione ", e);
         //This should never occur
         throw new XAException(XAException.XAER_RMERR);
      }
   }

   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      try
      {                              
         SessionXASetTimeoutResponse response =
            (SessionXASetTimeoutResponse)remotingConnection.send(id, new SessionXASetTimeoutMessage(seconds));
         
         return response.isOK();
      }
      catch (JMSException e)
      {
         //This should never occur
         throw new XAException(XAException.XAER_RMERR);
      }
   }

   public void start(Xid xid, int flags) throws XAException
   {
      try
      {
         AbstractPacket packet;
         
         if (flags == XAResource.TMJOIN)
         {
            packet = new SessionXAJoinMessage(xid);                  
         }
         else if (flags == XAResource.TMRESUME)
         {
            packet = new SessionXAResumeMessage(xid);
         }
         else if (flags == XAResource.TMNOFLAGS)
         {
            packet = new SessionXAStartMessage(xid);
         }
         else
         {
            throw new XAException(XAException.XAER_INVAL);
         }
                     
         SessionXAResponse response = (SessionXAResponse)remotingConnection.send(id, packet);
         
         if (response.isError())
         {
            log.error("XA operation failed " + response.getMessage() +" code:" + response.getResponseCode());
            throw new XAException(response.getResponseCode());
         }
      }
      catch (JMSException e)
      {
         log.error("Caught jmsexecptione ", e);
         //This should never occur
         throw new XAException(XAException.XAER_RMERR);
      }
   }

   // Public ---------------------------------------------------------------------------------------
  
   private boolean forceNotSameRM;
   
   public void setForceNotSameRM(boolean force)
   {
      this.forceNotSameRM = force;
   }
   
   // Protected ------------------------------------------------------------------------------------

   // Package Private ------------------------------------------------------------------------------

   // Private --------------------------------------------------------------------------------------

   private void checkClosed() throws IllegalStateException
   {
      if (closed)
      {
         throw new IllegalStateException("Session is closed");
      }
   }
        
   private void closeChildren() throws JMSException
   {

      Set<ClientConsumer> consumersClone = new HashSet<ClientConsumer>(consumers.values());
      
      for (ClientConsumer consumer: consumersClone)
      {
         consumer.closing();
         
         consumer.close();
      }
      
      Set<ClientProducer> producersClone = new HashSet<ClientProducer>(producers);
      
      for (ClientProducer producer: producersClone)
      {
         producer.closing();
         
         producer.close();
      }
      
      Set<ClientBrowser> browsersClone = new HashSet<ClientBrowser>(browsers);
      
      for (ClientBrowser browser: browsersClone)
      {
         browser.closing();
         
         browser.close();
      }
   }
   
   // Inner Classes --------------------------------------------------------------------------------

}
