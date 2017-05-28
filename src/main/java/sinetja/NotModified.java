package sinetja;

import io.netty.handler.codec.http.HttpHeaders;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Values.*;

public class NotModified {
    private static final int SECS_IN_A_YEAR = 365 * 24 * 60 * 60;

    // SimpleDateFormat is locale dependent
    // Avoid the case when Sinetja is run on for example Japanese platform
    private static final SimpleDateFormat RFC_2822;

    static {
        RFC_2822 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        RFC_2822.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static String formatRfc2822(Long timestamp) {
        return RFC_2822.format(timestamp);
    }

    /**
     * Tells the browser to cache static files for a long time.
     * This works well even when this is a cluster of web servers behind a load balancer
     * because the URL created by urlForResource is in the form: resource?etag
     *
     * <p>Don't worry that browsers do not pick up new files after you modified them,
     * see the doc about static files.
     *
     * <p>Google recommends 1 year:
     * http://code.google.com/speed/page-speed/docs/caching.html
     *
     * <p>Both Max-age and Expires header are set because IEs use Expires, not max-age:
     * http://mrcoles.com/blog/cookies-max-age-vs-expires/
     */
    public static void setClientCacheAggressively(HttpHeaders headers) {
        if (!headers.contains(CACHE_CONTROL)) {
            headers.set(CACHE_CONTROL, "public, " + MAX_AGE + "=" + SECS_IN_A_YEAR);
        }

        // CORS:
        // http://sockjs.github.com/sockjs-protocol/sockjs-protocol-0.3.3.html#section-7
        if (!headers.contains(ACCESS_CONTROL_MAX_AGE)) {
            headers.set(ACCESS_CONTROL_MAX_AGE, SECS_IN_A_YEAR);
        }

        // Note that SECS_IN_A_YEAR * 1000 is different from SECS_IN_A_YEAR * 1000L
        // because of integer overflow!
        if (!headers.contains(EXPIRES)) {
            headers.set(EXPIRES, formatRfc2822(System.currentTimeMillis() + SECS_IN_A_YEAR * 1000L));
        }
    }

    /**
     * Prevents client cache.
     * Note that "pragma: no-cache" is linked to requests, not responses:
     * http://palizine.plynt.com/issues/2008Jul/cache-control-attributes/
     */
    public static void setNoClientCache(HttpHeaders headers) {
        // http://sockjs.github.com/sockjs-protocol/sockjs-protocol-0.3.html#section-11
        headers.remove(EXPIRES);
        headers.remove(LAST_MODIFIED);
        headers.set(CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
    }
}
