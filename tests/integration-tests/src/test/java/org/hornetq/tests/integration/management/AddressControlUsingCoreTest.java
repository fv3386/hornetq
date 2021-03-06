/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.tests.integration.management;

import static org.hornetq.tests.util.RandomUtil.randomString;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.api.core.management.AddressControl;
import org.hornetq.api.core.management.ResourceNames;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.security.CheckType;
import org.hornetq.core.security.Role;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.tests.util.RandomUtil;

/**
 *
 * A AddressControlUsingCoreTest
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 *
 */
public class AddressControlUsingCoreTest extends ManagementTestBase
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private HornetQServer server;

   protected ClientSession session;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testGetAddress() throws Exception
   {
      SimpleString address = RandomUtil.randomSimpleString();
      SimpleString queue = RandomUtil.randomSimpleString();

      session.createQueue(address, queue, false);

      CoreMessagingProxy proxy = createProxy(address);

      Assert.assertEquals(address.toString(), proxy.retrieveAttributeValue("address"));

      session.deleteQueue(queue);
   }

   public void testGetQueueNames() throws Exception
   {
      SimpleString address = RandomUtil.randomSimpleString();
      SimpleString queue = RandomUtil.randomSimpleString();
      SimpleString anotherQueue = RandomUtil.randomSimpleString();

      session.createQueue(address, queue, true);

      CoreMessagingProxy proxy = createProxy(address);
      Object[] queueNames = (Object[])proxy.retrieveAttributeValue("queueNames");
      Assert.assertEquals(1, queueNames.length);
      Assert.assertEquals(queue.toString(), queueNames[0]);

      session.createQueue(address, anotherQueue, false);
      queueNames = (Object[])proxy.retrieveAttributeValue("queueNames");
      Assert.assertEquals(2, queueNames.length);

      session.deleteQueue(queue);

      queueNames = (Object[])proxy.retrieveAttributeValue("queueNames");
      Assert.assertEquals(1, queueNames.length);
      Assert.assertEquals(anotherQueue.toString(), queueNames[0]);

      session.deleteQueue(anotherQueue);
   }

   public void testGetBindingNames() throws Exception
   {
      SimpleString address = RandomUtil.randomSimpleString();
      SimpleString queue = RandomUtil.randomSimpleString();
      String divertName = RandomUtil.randomString();

      session.createQueue(address, queue, false);

      CoreMessagingProxy proxy = createProxy(address);
      Object[] bindingNames = (Object[])proxy.retrieveAttributeValue("bindingNames");
      assertEquals(1, bindingNames.length);
      assertEquals(queue.toString(), bindingNames[0]);

      server.getHornetQServerControl().createDivert(divertName, randomString(), address.toString(), RandomUtil.randomString(), false, null, null);

      bindingNames = (Object[])proxy.retrieveAttributeValue("bindingNames");
      assertEquals(2, bindingNames.length);

      session.deleteQueue(queue);

      bindingNames = (Object[])proxy.retrieveAttributeValue("bindingNames");
      assertEquals(1, bindingNames.length);
      assertEquals(divertName.toString(), bindingNames[0]);
   }

   public void testGetRoles() throws Exception
   {
      SimpleString address = RandomUtil.randomSimpleString();
      SimpleString queue = RandomUtil.randomSimpleString();
      Role role = new Role(RandomUtil.randomString(),
                           RandomUtil.randomBoolean(),
                           RandomUtil.randomBoolean(),
                           RandomUtil.randomBoolean(),
                           RandomUtil.randomBoolean(),
                           RandomUtil.randomBoolean(),
                           RandomUtil.randomBoolean(),
                           RandomUtil.randomBoolean());

      session.createQueue(address, queue, true);

      CoreMessagingProxy proxy = createProxy(address);
      Object[] roles = (Object[])proxy.retrieveAttributeValue("roles");
      for (Object role2 : roles)
      {
         System.out.println(((Object[])role2)[0]);
      }
      Assert.assertEquals(0, roles.length);

      Set<Role> newRoles = new HashSet<Role>();
      newRoles.add(role);
      server.getSecurityRepository().addMatch(address.toString(), newRoles);

      roles = (Object[])proxy.retrieveAttributeValue("roles");
      Assert.assertEquals(1, roles.length);
      Object[] r = (Object[])roles[0];
      Assert.assertEquals(role.getName(), r[0]);
      Assert.assertEquals(CheckType.SEND.hasRole(role), r[1]);
      Assert.assertEquals(CheckType.CONSUME.hasRole(role), r[2]);
      Assert.assertEquals(CheckType.CREATE_DURABLE_QUEUE.hasRole(role), r[3]);
      Assert.assertEquals(CheckType.DELETE_DURABLE_QUEUE.hasRole(role), r[4]);
      Assert.assertEquals(CheckType.CREATE_NON_DURABLE_QUEUE.hasRole(role), r[5]);
      Assert.assertEquals(CheckType.DELETE_NON_DURABLE_QUEUE.hasRole(role), r[6]);
      Assert.assertEquals(CheckType.MANAGE.hasRole(role), r[7]);

      session.deleteQueue(queue);
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      Configuration conf = createBasicConfig();
      conf.setSecurityEnabled(false);
      conf.setJMXManagementEnabled(true);
      conf.getAcceptorConfigurations().add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
      server = createServer(false, conf, mbeanServer);
      server.start();

      ServerLocator locator = createInVMNonHALocator();
      locator.setBlockOnNonDurableSend(true);
      locator.setBlockOnNonDurableSend(true);
      ClientSessionFactory sf = createSessionFactory(locator);
      session = sf.createSession(false, true, false);
      session.start();
   }

   protected CoreMessagingProxy createProxy(final SimpleString address) throws Exception
   {
      CoreMessagingProxy proxy = new CoreMessagingProxy(session, ResourceNames.CORE_ADDRESS + address);

      return proxy;
   }

   @Override
   protected void tearDown() throws Exception
   {
      session.close();
      session = null;
      super.tearDown();
   }

   protected AddressControl createManagementControl(final SimpleString address) throws Exception
   {
      return ManagementControlHelper.createAddressControl(address, mbeanServer);
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
