package project.fitnessapplication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                // Convert PostgreSQL URL format to JDBC format
                URI uri = new URI(databaseUrl);
                String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
                
                // Create HikariCP DataSource
                com.zaxxer.hikari.HikariDataSource dataSource = new com.zaxxer.hikari.HikariDataSource();
                dataSource.setJdbcUrl(jdbcUrl);
                dataSource.setUsername(uri.getUserInfo().split(":")[0]);
                dataSource.setPassword(uri.getUserInfo().split(":")[1]);
                
                return dataSource;
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure database connection", e);
            }
        }
        return null;
    }
}
