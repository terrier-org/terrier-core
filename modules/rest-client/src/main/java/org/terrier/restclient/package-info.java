/** Provides a proxy Manager implementation that can access remotely provided 
 * Managers over a HTTP REST connection.
 * 
 *  To use, ensure that your terrier-rest-client is included on your classpath, 
 *  and then use a ManagerFactory as normal, on an IndexRef referring to a remote
 *  REST server.
 *  <code>
 *  Manager m = Manager.from(IndexRef.of("http://host/of/rest/"))
 *  </code>
 */
package org.terrier.restclient;

