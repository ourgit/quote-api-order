import play.filters.gzip.GzipFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * This class configures filters that run on every request. This
 * class is queried by Play to get a list of filters.
 * <p>
 * Play will automatically use filters from any class called
 * <code>Filters</code> that is placed the root package. You can load filters
 * from a different class by adding a `play.http.filters` setting to
 * the <code>application.conf</code> configuration file.
 */
@Singleton
public class Filters implements HttpFilters {
    private List<EssentialFilter> filters = new ArrayList<>();

    //routedLoggingFilter.asJava(), 日志
    @Inject
    public Filters(GzipFilter gzipFilter) {
        filters.add(gzipFilter.asJava());
    }

    @Override
    public List<EssentialFilter> getFilters() {
        return filters;
    }
}
