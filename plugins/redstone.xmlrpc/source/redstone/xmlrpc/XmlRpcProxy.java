/*
    Copyright (c) 2005 Redstone Handelsbolag

    This library is free software; you can redistribute it and/or modify it under the terms
    of the GNU Lesser General Public License as published by the Free Software Foundation;
    either version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License along with this
    library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
    Boston, MA  02111-1307  USA
*/

package redstone.xmlrpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 *  An XmlRpcProxy lets you use the services of an XML-RPC server through
 *  Java interfaces. It uses the Dynamic Proxy API introduced in JDK 1.3 to
 *  dynamically convert calls through Java interfaces to XML-RPC messages. This
 *  may be an improvement over the XmlRpcClient since using a server through Java
 *  interfaces allows compilation-time type checking, IDE code completion, and
 *  prevents typos and other errors.
 *
 *  @author Greger Olsson
 */

public class XmlRpcProxy implements InvocationHandler
{
    /**
     *  Creates a new dynamic proxy object that implements all the
     *  supplied interfaces.  This object may be type cast to any of
     *  the interface supplied in the call.  Method calls through the
     *  interfaces will be translated to XML-RPC calls to the server
     *  in the supplied url.
     *
     *  @param url The XML-RPC server that will receive calls through
     *             the interfaces.
     *
     *  @param interfaces The list of interfaces the proxy should
     *                    implement.
     *
     *  @return An object implementing the supplied interfaces with
     *          XML-RPC support.
     */

    public static Object createProxy(Class[] interfaces, XmlRpcClient c)
    {
        return createProxy(null, interfaces, c);
    }


    /**
     *  Creates a new dynamic proxy object that implements all
     *  supplied interfaces.  This object may be type cast to any of
     *  the interface supplied in the call.  Method calls through the
     *  interfaces will be translated to XML-RPC calls to the server
     *  in the supplied url.
     *
     *  @param url The XML-RPC server that will receive calls through
     *             the interfaces
     *
     *  @param interfaces The list of interfaces the proxy should
     *                    implement
     *
     *  @param objectName The name under which the handler is
     *                    reachable
     *
     *  @return An object implementing the supplied interfaces with
     *          XML-RPC support
     */

    public static Object createProxy(String objectName,Class[] interfaces,XmlRpcClient c)
    {
        return Proxy.newProxyInstance(
            interfaces[ 0 ].getClassLoader(),
            interfaces,
            new XmlRpcProxy(objectName, c));
    }


    /**
     *  Sets the HTTP request properties that the proxy will use for the next invocation,
     *  and any invocations that follow until setRequestProperties() is invoked again. Null
     *  is accepted and means that no special HTTP request properties will be used in any
     *  future XML-RPC invocations using this XmlRpcProxy instance.
     *
     *  @param requestProperties The HTTP request properties to use for future invocations
     *                           made using this XmlRpcProxy instance. These will replace
     *                           any previous properties set using this method or the
     *                           setRequestProperty() method.
     */

    /*public void setRequestProperties( Map requestProperties )
    {
        client.setRequestProperties( requestProperties );
    }*/
    

    /**
     *  Sets a single HTTP request property to be used in future invocations.
     *  @see setRequestProperties()
     *
     *  @param name Name of the property to set
     *  @param value The value of the property
     */

    /*public void setRequestProperty( String name, String value )
    {
        client.setRequestProperty( name, value );
    }*/

    
    /**
     *  Handles method calls invoked on the proxy object. This is not used by the
     *  application but has to be public so that the dynamic proxy has access to it.
     *  It just hands the call over to the performCall method.
     *
     *  @see The Dynamic Proxy API in JDK 1.3
     *
     *  @return Any of the values returned by an XmlRpcClient.
     */

    public Object invoke(
        Object proxy,
        Method method,
        Object[] args )
        throws Exception
    {
        String handlerName;

        if ( objectName == null )
        {
            // The recommended way to use XmlRpcProxy is to tie the proxy to a specific
            // handler name. If not we must extract the name, given the name of the interface
            // we're calling through, which may contain '.' and '$'.

            String declaringClass = method.getDeclaringClass().getName();

            if ( declaringClass.indexOf( '$' ) != -1 )
            {
                handlerName = declaringClass.substring( declaringClass.lastIndexOf( '$' ) + 1 );
            }
            else if ( declaringClass.indexOf( '.' ) != -1 )
            {
                handlerName = declaringClass.substring( declaringClass.lastIndexOf( '.' ) + 1 );
            }
            else
            {
                handlerName = declaringClass;
            }
        }
        else
        {
            handlerName = objectName;
        }

        // Let the basic XmlRpcClient perform the call. This may result in an XmlRpcException
        // which will be propagated out from this method.

        if(handlerName.equals(""))
        	return client.invoke( method.getName(), args );
        
        return client.invoke( handlerName + "." + method.getName(), args );
    }


    /**
     *  Creates a new XmlRpcProxy which is a dynamic proxy invocation handler with
     *  an encapsulated XmlRpcClient. Not for pubilc usage -- use createProxy()
     */

    protected XmlRpcProxy(String objectName,XmlRpcClient c)
    {
        client = c;
        this.objectName = objectName;
    }

    
    /** The encapsulated XmlRpcClient receiving the converted dynamic calls */
    protected XmlRpcClient client;

    /** The name of the handler that will handle the converted dynamic calls */
    protected String objectName;
}