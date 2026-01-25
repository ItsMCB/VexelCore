package me.itsmcb.vexelcore.bukkit;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

class VexelCoreLoader implements PluginLoader {
    @Override
    public void classloader(final PluginClasspathBuilder builder) {
        final MavenLibraryResolver maven = new MavenLibraryResolver();
        // Configure Paper's default mirror (using the direct repo is against their TOS)
        maven.addRepository(new RemoteRepository.Builder("maven-central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());

        // Runtime dependencies
        maven.addDependency(new Dependency(new DefaultArtifact("org.mariadb.jdbc:mariadb-java-client:3.5.7"), null));
        maven.addDependency(new Dependency(new DefaultArtifact("dev.dejvokep:boosted-yaml-spigot:1.5"), null));
        maven.addDependency(new Dependency(new DefaultArtifact("dev.dejvokep:boosted-yaml:1.3.7"), null));
        // Remember: DO NOT add plugin dependencies here; they go in the paper-plugin.yml server dependencies section. Add non-plugin dependencies here, like MariaDB.

        builder.addLibrary(maven);
    }
}