package ink.markidea.note.db.sqlite;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataBuilderInitializer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.DialectResolverSet;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hansanshi
 */
public class SQLiteMetadataBuilderInitializer implements MetadataBuilderInitializer {

    private final static Logger logger = LoggerFactory.getLogger(SQLiteMetadataBuilderInitializer.class);

    @Override
    public void contribute(MetadataBuilder metadataBuilder, StandardServiceRegistry serviceRegistry) {
        DialectResolver dialectResolver = serviceRegistry.getService(DialectResolver.class);

        if (!(dialectResolver instanceof DialectResolverSet)) {
            logger.warn("DialectResolver {} is not an instance of DialectResolverSet, not registering SQLiteDialect",
                    dialectResolver);
            return;
        }

        ((DialectResolverSet) dialectResolver).addResolver(resolver);
    }

    static private final SQLiteDialect dialect = new SQLiteDialect();

    static private final DialectResolver resolver = new DialectResolver() {

        @Override
        public Dialect resolveDialect(DialectResolutionInfo info) {
            if ("SQLite".equalsIgnoreCase(info.getDatabaseName())){
                return dialect;
            }

            return null;
        }

    };
}