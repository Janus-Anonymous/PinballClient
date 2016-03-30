/**
 * This original Volley httpURLConnection module has been modified,
 * by adding some Network Exception Mandling methods.
 * All the
 */

package com.android.pinball.toolbox;

import android.util.Log;

import com.android.pinball.AuthFailureError;
import com.android.pinball.Request;
import com.android.pinball.Request.Method;
import com.android.pinball.Response;
import com.android.pinball.Response.ProgressListener;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * An {@link HttpStack} based on {@link HttpURLConnection}.
 */
public class HurlStack implements HttpStack {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private final UrlRewriter mUrlRewriter;
    private final SSLSocketFactory mSslSocketFactory;
    private String sPath = "/data/data/com.mobicom.pinballframework/download";
    private String TAG = "HurlStack";

    public static String HurlStackThreadName;
    public static MyThread HurlStackThread;
    public static boolean isNetworkConnected = false;

    private final int BLOCKSIZE = 2048;
    private final int timeout = 100000;
    private final int NUMLIMIT_FAIL = 3;


    public HurlStack() {
        this(null);
    }

    /**
     * @param urlRewriter Rewriter to use for request URLs
     */
    public HurlStack(UrlRewriter urlRewriter) {
        this(urlRewriter, null);
    }

    /**
     * @param urlRewriter      Rewriter to use for request URLs
     * @param sslSocketFactory SSL factory to use for HTTPS connections
     */
    public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
        mUrlRewriter = urlRewriter;
        mSslSocketFactory = sslSocketFactory;
    }

    /**
     * Checks if a response message contains a body.
     *
     * @param requestMethod request method
     * @param responseCode  response status code
     * @return whether the response has a body
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3">RFC 7230 section 3.3</a>
     */
    private static boolean hasResponseBody(int requestMethod, int responseCode) {
        return requestMethod != Method.HEAD
                && !(HttpStatus.SC_CONTINUE <= responseCode && responseCode < HttpStatus.SC_OK)
                && responseCode != HttpStatus.SC_NO_CONTENT
                && responseCode != HttpStatus.SC_NOT_MODIFIED;
    }

    /**
     * Initializes an {@link HttpEntity} from the given {@link HttpURLConnection}.
     *
     * @param connection
     * @return an HttpEntity populated with data from <code>connection</code>.
     */
    private HttpEntity entityFromConnection(HttpURLConnection connection, URL parsedUrl, Request request) {
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(connection.getInputStream());

        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }

        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        return entity;
    }

    @SuppressWarnings("deprecation")
    /* package */ static void setConnectionParametersForRequest(HttpURLConnection connection,
                                                                Request<?> request) throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Method.DEPRECATED_GET_OR_POST:
                // This is the deprecated way that needs to be handled for backwards compatibility.
                // If the request's post body is null, then the assumption is that the request is
                // GET.  Otherwise, it is assumed that the request is a POST.
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    // Prepare output. There is no need to set Content-Length explicitly,
                    // since this is handled by HttpURLConnection using the size of the prepared
                    // output stream.
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty(HEADER_CONTENT_TYPE,
                            request.getPostBodyContentType());
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.write(postBody);
                    out.close();
                }
                break;
            case Method.GET:
                // Not necessary to set the request method because connection defaults to GET but
                // being explicit here.
                connection.setRequestMethod("GET");
                break;
            case Method.DELETE:
                connection.setRequestMethod("DELETE");
                break;
            case Method.POST:
                connection.setRequestMethod("POST");
                addBodyIfExists(connection, request);
                break;
            case Method.PUT:
                connection.setRequestMethod("PUT");
                addBodyIfExists(connection, request);
                break;
            case Method.HEAD:
                connection.setRequestMethod("HEAD");
                break;
            case Method.OPTIONS:
                connection.setRequestMethod("OPTIONS");
                break;
            case Method.TRACE:
                connection.setRequestMethod("TRACE");
                break;
            case Method.PATCH:
                connection.setRequestMethod("PATCH");
                addBodyIfExists(connection, request);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static void addBodyIfExists(HttpURLConnection connection, Request<?> request)
            throws IOException, AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            connection.setDoOutput(true);
            connection.addRequestProperty(HEADER_CONTENT_TYPE, request.getBodyContentType());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(body);
            out.close();
        }
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        String url = request.getUrl(),sProperty;
        HashMap<String, String> map = new HashMap<String, String>();
        FileOutputStream f = new FileOutputStream(new File(sPath));
        int nStartPos = 0, nRead, failedCnt;
        long nEndPos;

        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        if (mUrlRewriter != null) {
            String rewritten = mUrlRewriter.rewriteUrl(url);
            if (rewritten == null) {
                throw new IOException("URL blocked by rewriter: " + url);
            }
            url = rewritten;
        }
        HurlStackThread = new MyThread();
        HurlStackThreadName = HurlStackThread.getName();
        synchronized (HurlStackThread) {
            HurlStackThread.start();
        }
        URL parsedUrl = new URL(url);
        HttpURLConnection connection = openConnection(parsedUrl, request);
        connection.setRequestProperty("RANGE", request.sProperty);

        connection.setUseCaches(false);
        setConnectionParametersForRequest(connection, request);
        InputStream inputStream ;
        ProgressListener progressListener = null;
        if (request instanceof Response.ProgressListener) {
            progressListener = (ProgressListener) request;
        }
        while(true) {
            try {
                connection.disconnect();
                connection = openConnection(parsedUrl, request);
                connection.setRequestProperty("RANGE", request.sProperty);
                inputStream = connection.getInputStream();
                break;
            } catch (Exception e) {
                continue;
            }
        }
        /**
         * split() is used here to get End Position from sProperty.
         * e.g.
         *      sProperty = "bytes=0-1024"
         *      then nEndPos = 1024
         */
        if(request.sProperty == null) {
            nEndPos = connection.getContentLength();
        }
        else {
            nEndPos = Long.parseLong(request.sProperty.split("-")[1]);
        }

        byte[] b = new byte[BLOCKSIZE];
        while (true) {
            failedCnt = 1;
            try {
                while (nStartPos < nEndPos && failedCnt <= NUMLIMIT_FAIL) {
                    try {
                        nRead = inputStream.read(b, 0, BLOCKSIZE);
                        if(nRead>0) {
                            f.write(b, 0, nRead);
                            nStartPos += nRead;
                            if (null != progressListener) {
                                progressListener.onProgress(nStartPos, nEndPos);
                            }
                        }else
                        {
                            /**
                             * download successfully
                             */
                            break;
                        }
                    } catch (Exception e) {
                        failedCnt++;
                        while(true) {
                            try {
                                connection.disconnect();
                                connection = openConnection(parsedUrl, request);
                                connection.setRequestProperty("RANGE", request.sProperty);
                                connection.setConnectTimeout(timeout);
                                connection.setReadTimeout(timeout);
                                sProperty = "bytes="  + nStartPos +  "-" +nEndPos;
                                connection.setRequestProperty("RANGE" , sProperty);
                                inputStream = connection.getInputStream();
                                break;
                            } catch (Exception e2) {
                                continue;
                            }
                        }
                    }
                }

                if (failedCnt > NUMLIMIT_FAIL) {
                    while (isNetworkConnected == false) ;
                    synchronized (HurlStackThread) {
                        try {
                            HurlStackThread.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    Log.d(TAG,"Download successfully!");
                    f.close();
                    break;
                }
            }catch (Exception e1){
                while (isNetworkConnected == false) ;
                synchronized (HurlStackThread) {
                    try {
                        HurlStackThread.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // Initialize HttpResponse with data from the HttpURLConnection.
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        int responseCode = 200;
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }
        inputStream = new FileInputStream(sPath);
        BasicHttpEntity entity = new BasicHttpEntity();
        StatusLine responseStatus = new BasicStatusLine(protocolVersion, responseCode, "OK");
        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        response.setEntity(entity);
        //        if (hasResponseBody(request.getMethod(), responseStatus.getStatusCode())) {
        //            response.setEntity(entityFromConnection(connection, parsedUrl, request));
        //        }

        for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
                response.addHeader(h);
            }
        }
        connection.disconnect();
        return response;
    }


    /**
     * Create an {@link HttpURLConnection} for the specified {@code url}.
     */
    protected HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    /**
     * Opens an {@link HttpURLConnection} with parameters.
     *
     * @param url
     * @return an open connection
     * @throws IOException
     */
    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
        HttpURLConnection connection = createConnection(url);
        connection.setRequestProperty("RANGE", request.sProperty);

        int timeoutMs = request.getTimeoutMs();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
        }

        return connection;
    }

    /**
     * An interface for transforming URLs before use.
     */
    public interface UrlRewriter {
        /**
         * Returns a URL to use instead of the provided one, or null to indicate
         * this URL should not be used at all.
         */
        String rewriteUrl(String originalUrl);
    }
}
