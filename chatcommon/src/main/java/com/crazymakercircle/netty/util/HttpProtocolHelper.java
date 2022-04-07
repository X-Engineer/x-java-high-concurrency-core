package com.crazymakercircle.netty.util;

import com.crazymakercircle.config.SystemConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpProtocolHelper {
    public static final int HTTP_CACHE_SECONDS = 60;

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    public static final AttributeKey<HttpVersion> PROTOCOL_VERSION_KEY =
            AttributeKey.valueOf("PROTOCOL_VERSION");
    public static final AttributeKey<Boolean> KEEP_ALIVE_KEY =
            AttributeKey.valueOf("KEEP_ALIVE_KEY");


    /**
     * 通过channel 缓存 Http 的协议版本，以及是否为长连接
     *
     * @param ctx     上下文
     * @param request 报文
     */
    public static void cacheHttpProtocol(ChannelHandlerContext ctx, final FullHttpRequest request) {
        //每一个连接设置一次即可，不需要重复设置
        if (ctx.channel().attr(KEEP_ALIVE_KEY).get() == null) {
            ctx.channel().attr(PROTOCOL_VERSION_KEY).set(request.protocolVersion());
            final boolean keepAlive = HttpUtil.isKeepAlive(request);
            ctx.channel().attr(KEEP_ALIVE_KEY).set(keepAlive);
        }
    }


    public static void setKeepAlive(ChannelHandlerContext ctx, boolean val) {
        ctx.channel().attr(KEEP_ALIVE_KEY).set(val);
    }

    public static String sanitizeUri(String uri, String dir) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
                uri.contains('.' + File.separator) ||
                uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
                INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        // Convert to absolute path.
        return dir + File.separator + uri;
    }

    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[^-\\._]?[^<>&\\\"]*");

    public static void sendListing(ChannelHandlerContext ctx, final FullHttpRequest request,
                                   File dir, String dirPath) {
        StringBuilder buf = new StringBuilder()
                .append("<!DOCTYPE html>\r\n")
                .append("<html><head><meta charset='utf-8' /><title>")
                .append("Listing of: ")
                .append(dirPath)
                .append("</title></head><body>\r\n")

                .append("<h3>Listing of: ")
                .append(dirPath)
                .append("</h3>\r\n")

                .append("<ul>")
                .append("<li><a href=\"../\">..</a></li>\r\n");

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isHidden() || !f.canRead()) {
                    continue;
                }

                String name = f.getName();
                if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                    continue;
                }

                buf.append("<li><a href=\"")
                        .append(name)
                        .append("\">")
                        .append(name)
                        .append("</a></li>\r\n");
            }
        }

        buf.append("</ul></body></html>\r\n");

        ByteBuf buffer = ctx.alloc().buffer(buf.length());
        buffer.writeCharSequence(buf.toString(), CharsetUtil.UTF_8);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, buffer);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

        sendAndCleanupConnection(ctx, response);
    }

    public static void sendRedirect(ChannelHandlerContext ctx, final FullHttpRequest request, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND, Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);

        sendAndCleanupConnection(ctx, response);
    }

    public static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpVersion version = getHttpVersion(ctx);
        FullHttpResponse response = new DefaultFullHttpResponse(
                version, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        sendAndCleanupConnection(ctx, response);
    }

    /**
     * 发送普通文本响应
     *
     * @param ctx     上下文
     * @param content 响应内容
     */
    public static void sendContent(ChannelHandlerContext ctx, String content) {
        HttpVersion version = getHttpVersion(ctx);
        FullHttpResponse response = new DefaultFullHttpResponse(
                version, OK, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        sendAndCleanupConnection(ctx, response);
    }

    /**
     * 发送html页面响应
     *
     * @param ctx     上下文
     * @param content 响应内容
     */
    public static void sendWebPage(ChannelHandlerContext ctx, String content) {
        HttpVersion version = getHttpVersion(ctx);
        FullHttpResponse response = new DefaultFullHttpResponse(
                version, OK, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

        sendAndCleanupConnection(ctx, response);
    }

    /**
     * 发送Json格式的响应
     *
     * @param ctx     上下文
     * @param content 响应内容
     */
    public static void sendJsonContent(ChannelHandlerContext ctx, String content) {
        HttpVersion version = getHttpVersion(ctx);
        /**
         * 构造一个默认的FullHttpResponse实例
         */
        FullHttpResponse response = new DefaultFullHttpResponse(
                version, OK, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        /**
         * 设置响应头
         */
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        /**
         * 发送响应内容
         */
        sendAndCleanupConnection(ctx, response);
    }

    /**
     * 发送响应
     */
    public static void sendAndCleanupConnection(ChannelHandlerContext ctx, FullHttpResponse response) {
        final boolean keepAlive = ctx.channel().attr(KEEP_ALIVE_KEY).get();
        HttpUtil.setContentLength(response, response.content().readableBytes());
        if (!keepAlive) {
            // 如果不是长连接，设置 connection:close 头部
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (isHTTP_1_0(ctx)) {
            // 如果是1.0版本的长连接，设置 connection:keep-alive 头部
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        //发送内容
        ChannelFuture flushPromise = ctx.writeAndFlush(response);

        if (!keepAlive) {
            // 如果不是长连接，发送完成之后，关闭连接
            flushPromise.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static HttpVersion getHttpVersion(ChannelHandlerContext ctx) {
        HttpVersion version;
        if (isHTTP_1_0(ctx)) {
            version = HTTP_1_0;
        } else {
            version = HTTP_1_1;
        }
        return version;
    }

    /**
     * When file timestamp is the same as what the browser is sending up, send a "304 Not Modified"
     *
     * @param ctx Context
     */
    public static void sendNotModified(ChannelHandlerContext ctx) {
        HttpVersion version = getHttpVersion(ctx);
        FullHttpResponse response = new DefaultFullHttpResponse(version, NOT_MODIFIED, Unpooled.EMPTY_BUFFER);
        setDateHeader(response);

        sendAndCleanupConnection(ctx, response);
    }


    public static boolean isHTTP_1_0(ChannelHandlerContext ctx) {

        HttpVersion protocol_version =
                ctx.channel().attr(PROTOCOL_VERSION_KEY).get();
        if (null == protocol_version) {
            return false;
        }
        if (protocol_version.equals(HTTP_1_0)) {
            return true;
        }
        return false;
    }

    /**
     * Sets the Date header for the HTTP response
     *
     * @param response HTTP response
     */
    public static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response    HTTP response
     * @param fileToCache file to extract content type
     */
    public static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        //设置缓存过期时间
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);

        //最近修改时间
        String lastModified = dateFormatter.format(new Date(fileToCache.lastModified()));
        response.headers().set(HttpHeaderNames.LAST_MODIFIED, lastModified);
    }

    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response HTTP response
     * @param file     file to extract content type
     */
    public static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                mimeTypesMap.getContentType(file.getPath()));
    }


    public static void setKeepAlive(ChannelHandlerContext ctx, HttpResponse response) {
        final boolean keepAlive = ctx.channel().attr(KEEP_ALIVE_KEY).get();

        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (isHTTP_1_0(ctx)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

    }

    public static boolean isKeepAlive(ChannelHandlerContext ctx) {
        boolean keepAlive = ctx.channel().attr(KEEP_ALIVE_KEY).get();
        return keepAlive;
    }

    /**
     * 发送目录或者错误信息，如果是文件，则返回
     *
     * @param ctx     上下文
     * @param request 请求
     * @return 文件对象
     */
    public static File sendErrorOrDirectory(ChannelHandlerContext ctx, FullHttpRequest request) {
        /**
         * 路径不对
         */
        final String uri = request.uri();
        final String path = HttpProtocolHelper.sanitizeUri(uri, SystemConfig.getFileServerDir());
        if (path == null) {
            HttpProtocolHelper.sendError(ctx, FORBIDDEN);
            return null;
        }
        File file = new File(path);

        /**
         * 文件不存在
         */
        if (!file.exists()) {
            HttpProtocolHelper.sendError(ctx, NOT_FOUND);
            return null;
        }


        /**
         * 发送文件目录
         */
        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                HttpProtocolHelper.sendListing(ctx, request, file, uri);
            } else {
                HttpProtocolHelper.sendRedirect(ctx, request, uri + '/');
            }
            return null;
        }
        /**
         * 文件不可用访问
         */
        if (!file.isFile()) {
            HttpProtocolHelper.sendError(ctx, FORBIDDEN);
            return null;
        }

        return file;
    }

    /**
     * 根据文件，获取只读的随机访问文件实例
     *
     * @param ctx  上下文
     * @param file 文件
     * @return 随机访问文件实例
     */
    public static RandomAccessFile openFile(ChannelHandlerContext ctx, File file) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            HttpProtocolHelper.sendError(ctx, NOT_FOUND);
            return null;
        }
        return raf;
    }
}
