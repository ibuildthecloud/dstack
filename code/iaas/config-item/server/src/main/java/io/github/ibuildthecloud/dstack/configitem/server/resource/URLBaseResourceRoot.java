package io.github.ibuildthecloud.dstack.configitem.server.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class URLBaseResourceRoot extends AbstractCachingResourceRoot {

    List<Resource> resources;

    public URLBaseResourceRoot(Map<String,URL> resourceMap) {
        super();

        resources = new ArrayList<Resource>(resourceMap.size());
        for ( Map.Entry<String, URL> entry : resourceMap.entrySet() ) {
            resources.add(new URLResource(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    protected Collection<Resource> scanResources() throws IOException {
        return resources;
    }

}
