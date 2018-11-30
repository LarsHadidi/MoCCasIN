package webservice;

import io.helidon.common.CollectionsHelper;
import moccasin.moccasin.controller.TreeBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.io.File;
import java.util.Set;

@ApplicationScoped
@ApplicationPath("/")
public class MoccasinServiceProvider extends Application {
    private final TreeBuilder TREE_BUILDER;

    @Inject
    public MoccasinServiceProvider(@ConfigProperty(name="app.catalogpath") String catalogPath) {
        TREE_BUILDER = new TreeBuilder(new File(catalogPath));
    }

    @Override
    public Set<Class<?>> getClasses() {
        return CollectionsHelper.setOf(MoccasinService.class);
    }

    public TreeBuilder getTreeBuilder() {
        return TREE_BUILDER;
    }
}
