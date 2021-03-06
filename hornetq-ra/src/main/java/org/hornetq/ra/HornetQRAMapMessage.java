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

package org.hornetq.ra;

import java.util.Arrays;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.MapMessage;


/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: 71554 $
 */
public class HornetQRAMapMessage extends HornetQRAMessage implements MapMessage
{
   /** Whether trace is enabled */
   private static boolean trace = HornetQRALogger.LOGGER.isTraceEnabled();

   /**
    * Create a new wrapper
    *
    * @param message the message
    * @param session the session
    */
   public HornetQRAMapMessage(final MapMessage message, final HornetQRASession session)
   {
      super(message, session);

      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("constructor(" + message + ", " + session + ")");
      }
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public boolean getBoolean(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getBoolean(" + name + ")");
      }

      return ((MapMessage)message).getBoolean(name);
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public byte getByte(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getByte(" + name + ")");
      }

      return ((MapMessage)message).getByte(name);
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public byte[] getBytes(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getBytes(" + name + ")");
      }

      return ((MapMessage)message).getBytes(name);
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public char getChar(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getChar(" + name + ")");
      }

      return ((MapMessage)message).getChar(name);
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public double getDouble(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getDouble(" + name + ")");
      }

      return ((MapMessage)message).getDouble(name);
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public float getFloat(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getFloat(" + name + ")");
      }

      return ((MapMessage)message).getFloat(name);
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public int getInt(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getInt(" + name + ")");
      }

      return ((MapMessage)message).getInt(name);
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public long getLong(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getLong(" + name + ")");
      }

      return ((MapMessage)message).getLong(name);
   }

   /**
    * Get the map names
    * @return The values
    * @exception JMSException Thrown if an error occurs
    */
   public Enumeration getMapNames() throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getMapNames()");
      }

      return ((MapMessage)message).getMapNames();
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public Object getObject(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getObject(" + name + ")");
      }

      return ((MapMessage)message).getObject(name);
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public short getShort(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getShort(" + name + ")");
      }

      return ((MapMessage)message).getShort(name);
   }

   /**
    * Get
    * @param name The name
    * @return The value
    * @exception JMSException Thrown if an error occurs
    */
   public String getString(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("getString(" + name + ")");
      }

      return ((MapMessage)message).getString(name);
   }

   /**
    * Does the item exist
    * @param name The name
    * @return True / false
    * @exception JMSException Thrown if an error occurs
    */
   public boolean itemExists(final String name) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("itemExists(" + name + ")");
      }

      return ((MapMessage)message).itemExists(name);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setBoolean(final String name, final boolean value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setBoolean(" + name + ", " + value + ")");
      }

      ((MapMessage)message).setBoolean(name, value);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setByte(final String name, final byte value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setByte(" + name + ", " + value + ")");
      }

      ((MapMessage)message).setByte(name, value);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @param offset The offset
    * @param length The length
    * @exception JMSException Thrown if an error occurs
    */
   public void setBytes(final String name, final byte[] value, final int offset, final int length) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setBytes(" + name + ", " + Arrays.toString(value) + ", " + offset + ", " +
                  length + ")");
      }

      ((MapMessage)message).setBytes(name, value, offset, length);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setBytes(final String name, final byte[] value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setBytes(" + name + ", " + Arrays.toString(value) + ")");
      }

      ((MapMessage)message).setBytes(name, value);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setChar(final String name, final char value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setChar(" + name + ", " + value + ")");
      }

      ((MapMessage)message).setChar(name, value);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setDouble(final String name, final double value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setDouble(" + name + ", " + value + ")");
      }

      ((MapMessage)message).setDouble(name, value);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setFloat(final String name, final float value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setFloat(" + name + ", " + value + ")");
      }

      ((MapMessage)message).setFloat(name, value);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setInt(final String name, final int value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setInt(" + name + ", " + value + ")");
      }

      ((MapMessage)message).setInt(name, value);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setLong(final String name, final long value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setLong(" + name + ", " + value + ")");
      }

      ((MapMessage)message).setLong(name, value);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setObject(final String name, final Object value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setObject(" + name + ", " + value + ")");
      }

      ((MapMessage)message).setObject(name, value);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setShort(final String name, final short value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setShort(" + name + ", " + value + ")");
      }

      ((MapMessage)message).setShort(name, value);
   }

   /**
    * Set
    * @param name The name
    * @param value The value
    * @exception JMSException Thrown if an error occurs
    */
   public void setString(final String name, final String value) throws JMSException
   {
      if (HornetQRAMapMessage.trace)
      {
         HornetQRALogger.LOGGER.trace("setString(" + name + ", " + value + ")");
      }

      ((MapMessage)message).setString(name, value);
   }
}
