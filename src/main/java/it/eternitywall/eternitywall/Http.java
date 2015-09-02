package it.eternitywall.eternitywall;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by Riccardo Casatta @RCasatta on 08/02/15.
 */
public class Http {
    private final static Logger log      = Logger.getLogger(Http.class.getName());

    public static Optional<String> get(String urlString) {
        return get(urlString, 10000);
    }
    public static Optional<String> get(String urlString, int timeout) {

        try {
            URL url = new URL(urlString);

            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", String.valueOf( System.currentTimeMillis() ) );
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            connection.setReadTimeout(timeout);
            connection.setConnectTimeout(timeout);

            final String contentEncoding = connection.getContentEncoding();
            final InputStream inputStream = getProperInputStream(connection);
            //System.out.println("connection.getContentEncoding() =" + contentEncoding);

            BufferedReader reader = new BufferedReader(new InputStreamReader( inputStream , Charsets.UTF_8));
            String line;
            StringBuffer sb=new StringBuffer();

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            int result=connection.getResponseCode();
            if(result>=200 && result<300) {
                String s = sb.toString();
                if(!"null".equals(s))
                    return Optional.of(s);
            } else {
                log.warning("result is not 2XX : " + result);
            }
        } catch (Exception e) {
            log.warning("Http.get() error " + e.toString() + " " + e.getMessage());
        }
        return Optional.absent();
    }

    private static InputStream getProperInputStream(HttpURLConnection connection) throws IOException {
        String enc = connection.getContentEncoding();
        if("gzip".equals(enc)) {
            return new GZIPInputStream(connection.getInputStream());
        } else if("deflate".equals(enc)) {
            return new InflaterInputStream(connection.getInputStream());
        }
        return connection.getInputStream();
    }

    public static Optional<String> post(String urlString, String postDataString, String contentType) {
        return req("POST", urlString, postDataString, contentType);
    }
    public static Optional<String> put(String urlString, String postDataString, String contentType) {
        return req("PUT", urlString, postDataString, contentType);
    }

    public static Optional<String> patchJson(String urlString, String jsonString) {
        Map<String,String> m = new HashMap<>();
        m.put("X-HTTP-Method-Override","PATCH");
        return req("POST", urlString, jsonString, "application/json", m, null);
    }

    public static Optional<String> postJson(String urlString, String jsonString) {
        return req("POST", urlString, jsonString, "application/json");
    }

    public static Optional<String> postJson(String urlString, String jsonString,Map<String,String> headers ) {
        return req("POST", urlString, jsonString, "application/json",headers, null);
    }

    public static Optional<String> putJson(String urlString, String jsonString) {
        return req("PUT", urlString, jsonString, "application/json");
    }

    public static Optional<String> putForm(String urlString,  Map<String,String> params) {
        return req("PUT", urlString,  urlEncodeUTF8(params) , "application/x-www-form-urlencoded");
    }
    public static Optional<String> postForm(String urlString, Map<String,String> params) {
        return req("POST", urlString, urlEncodeUTF8(params), "application/x-www-form-urlencoded");
    }

    public static Optional<String> postForm(String urlString, Map<String,String> params, Integer timeout) {
        return req("POST", urlString, urlEncodeUTF8(params), "application/x-www-form-urlencoded",null,timeout);
    }

    public static Optional<String> postP8G(String urlString, String data, String contentType, String token ) {
        Map<String,String> m = new HashMap<>();
        m.put("token",token);
        return req("POST", urlString, data, contentType, m, null);
    }

    public static Optional<String> req(String type, String urlString, String dataString, String contentType) {
        return req(type, urlString, dataString, contentType, null, null);
    }

    public static Optional<String> req(String type, String urlString, String dataString, String contentType, Map<String,String> headers, Integer timeout ) {

        try {

            byte[] postData       = dataString.getBytes( Charset.forName("UTF-8"));
            int    postDataLength = postData.length;
            log.info("posting to " + urlString + " data=" + dataString + " postDataLength=" + postDataLength + " contenttype=" + contentType );

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if(timeout!=null) {
                connection.setReadTimeout(timeout);
                connection.setConnectTimeout(timeout);
            }
            connection.setDoOutput(true);
            connection.setRequestMethod(type);
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Content-Length", Integer.toString( postDataLength ));
            connection.setRequestProperty("charset", "utf-8");
            //connection.setRequestProperty("User-Agent", "python-requests/2.4.1 CPython/2.7.8 Darwin/14.3.0");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            if(headers!=null) {
                for(String current : headers.keySet()) {
                    connection.setRequestProperty( current , headers.get(current) );
                }
            }
            try {
                DataOutputStream writer = new DataOutputStream( connection.getOutputStream() );
                writer.write( postData );
            } catch (Exception e) {log.severe(e.getMessage());}
            int result=connection.getResponseCode();
            if(result>=200 && result<300) {
                final InputStream stream = getProperInputStream(connection);
                String content = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
                String logContent = new String(content);
                Closeables.closeQuietly(stream);
                if (logContent != null) {
                    logContent = logContent.replaceAll("\n", "");
                    if (logContent.length() > 20)
                        logContent = logContent.substring(0, 20);
                }
                log.info("Http." + type + " respCode=" + result + " content=" + logContent);
                return Optional.of(content);
            } else {
                log.warning("result not 200 = " + result);
                try {
                    final InputStream stream = connection.getInputStream();
                    String content = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
                    log.warning(content);
                } catch(Exception e) {
                    log.warning("msg=" + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warning("Http." + type + " error " + e.getMessage());
        }
        return Optional.absent();
    }

    public static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }
    public static String urlEncodeUTF8(Map<?,?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?,?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    urlEncodeUTF8(entry.getKey().toString()),
                    urlEncodeUTF8(entry.getValue().toString())
            ));
        }
        return sb.toString();
    }






}
